/*
 *  Copyright © 2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.google.plugins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.format.UnexpectedFormatException;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.Emitter;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.InvalidEntry;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.Transform;
import io.cdap.google.common.NLPMethod;
import io.cdap.google.common.NLPMethodExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyses an input text via Google Language API. And returns the results of the analysis in form of a record.
 * All available Google NLP Methods are supported:
 * - Syntax Analysis
 * - Sentiment Analysis
 * - Entity Analysis
 * - Entity Sentiment Analysis
 * - Text Classification
 * - Anotate Text
 */
public abstract class NLPTransform extends Transform<StructuredRecord, StructuredRecord> {
  private static final Schema MENTION_SCORED =
    Schema.recordOf("mentionsRecord",
                    Schema.Field.of("content",
                                    Schema.of(Schema.Type.STRING)),
                    Schema.Field.of("beginOffset",
                                    Schema.of(Schema.Type.LONG)),
                    Schema.Field.of("type",
                                    Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("magnitude",
                                    Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("score",
                                    Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))));

  protected static final Schema TOKEN =
    Schema.recordOf("tokensRecord",
                    Schema.Field.of("content", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("beginOffset", Schema.nullableOf(Schema.of(Schema.Type.LONG))),
                    Schema.Field.of("tag", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("aspect", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("case", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("speechForm", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("gender", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("mood", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("number", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("person", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("proper", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("reciprocity", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("tense", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("voice", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("headTokenIndex", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("label", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
                    Schema.Field.of("lemma", Schema.nullableOf(Schema.of(Schema.Type.STRING))));

  protected static final Schema ENTITY_SCORED =
    Schema.recordOf("entitiesRecord",
                    Schema.Field.of("name", Schema.of(Schema.Type.STRING)),
                    Schema.Field.of("type", Schema.of(Schema.Type.STRING)),
                    Schema.Field.of("metadata", Schema.nullableOf(Schema.mapOf(Schema.of(Schema.Type.STRING),
                                                                               Schema.of(Schema.Type.STRING)))),
                    Schema.Field.of("salience", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("magnitude", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("score", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("mentions", Schema.arrayOf(MENTION_SCORED)));

  protected static final Schema SENTENCE_SCORED =
    Schema.recordOf("sentencesRecord",
                    Schema.Field.of("content", Schema.of(Schema.Type.STRING)),
                    Schema.Field.of("beginOffset", Schema.nullableOf(Schema.of(Schema.Type.LONG))),
                    Schema.Field.of("score", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))),
                    Schema.Field.of("magnitude", Schema.nullableOf(Schema.of(Schema.Type.DOUBLE))));

  protected static final Schema CATEGORY =
    Schema.recordOf("categoriesRecord",
                    Schema.Field.of("name", Schema.of(Schema.Type.STRING)),
                    Schema.Field.of("confidence", Schema.of(Schema.Type.DOUBLE)));

  protected static final JsonParser PARSER = new JsonParser();
  // metadata is dynamic and should be map<string,string>. Do not flatten it.
  protected static final String NOT_FLATTENED_FIELD = "metadata";

  private static final String ERROR_SCHEMA_BODY_PROPERTY = "body";
  private static final Schema STRING_ERROR_SCHEMA = Schema.recordOf("stringError",
                                                                    Schema.Field.of(ERROR_SCHEMA_BODY_PROPERTY,
                                                                                    Schema.of(Schema.Type.STRING)));
  private final NLPConfig config;

  public NLPTransform(NLPConfig config) {
    this.config = config;
  }

  public void configurePipeline(PipelineConfigurer pipelineConfigurer) {
    Schema inputSchema = pipelineConfigurer.getStageConfigurer().getInputSchema();
    FailureCollector failureCollector = pipelineConfigurer.getStageConfigurer().getFailureCollector();
    config.validate(failureCollector, inputSchema);
    failureCollector.getOrThrowException();
  }

  @Override
  public void transform(StructuredRecord structuredRecord, Emitter<StructuredRecord> emitter) {
    String text = structuredRecord.get(config.getSourceField());
    try (NLPMethodExecutor nlpMethodExecutor = getMethod().createExecutor(
      config.getServiceAccountFilePath(), config.getLanguageCode(), config.getEncodingType())) {
      try {
        emitter.emit(getRecordFromJson(nlpMethodExecutor.execute(text)));
      } catch (Exception e) {
        switch (config.getErrorHandling()) {
          case SKIP:
            break;
          case SEND:
            StructuredRecord.Builder builder = StructuredRecord.builder(STRING_ERROR_SCHEMA);
            builder.set(ERROR_SCHEMA_BODY_PROPERTY, text);
            emitter.emitError(new InvalidEntry<>(400, e.getMessage(), builder.build()));
            break;
          case STOP:
            throw e;
          default:
            throw new UnexpectedFormatException(
              String.format("Unknown error handling strategy '%s'", config.getErrorHandling()));
        }
      }
    }
  }


  protected List<String> jsonArrayToList(JsonArray jsonArray) {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      result.add(jsonArray.get(i).getAsJsonObject().toString());
    }
    return result;
  }

  protected abstract NLPMethod getMethod();
  protected abstract StructuredRecord getRecordFromJson(String json);

  protected static List<StructuredRecord> flattenJsonObjects(JsonArray jsonArray, Schema objectSchema) {
    return objectsToStructuredRecords(flattenJsonObjectsToMaps(jsonArray), objectSchema);
  }

  private static List<StructuredRecord> objectsToStructuredRecords(List<Map<String, Object>> objects,
                                                                   Schema objectSchema) {
    List<StructuredRecord> records = new ArrayList<>();

    for (Map<String, Object> object : objects) {
      StructuredRecord.Builder builder = StructuredRecord.builder(objectSchema);
      for (Map.Entry<String, Object> objectEntry : object.entrySet()) {
        String entryKey = objectEntry.getKey();
        Object entryValue = objectEntry.getValue();
        if (entryValue instanceof List) {
          Schema innerSchema = objectSchema.getField(entryKey).getSchema().getComponentSchema();
          List<StructuredRecord> recordsList = objectsToStructuredRecords((List<Map<String, Object>>) entryValue,
                                                                          innerSchema);
          builder.set(entryKey, recordsList);
        } else {
          builder.set(entryKey, entryValue);
        }
      }
      records.add(builder.build());
    }

    return records;
  }

  private static List<Map<String, Object>> flattenJsonObjectsToMaps(JsonArray jsonArray) {
    List<Map<String, Object>> results = new ArrayList<>();
    if (jsonArray != null) {
      for (int i = 0; i < jsonArray.size(); i++) {
        results.add(flattenJsonObjectToMap(jsonArray.get(i).getAsJsonObject()));
      }
    }
    return results;
  }

  private static Map<String, Object> flattenJsonObjectToMap(JsonObject jsonObject) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
      String key = entry.getKey();
      JsonElement valueElement = entry.getValue();

      if (valueElement.isJsonPrimitive()) {
        JsonPrimitive jsonPrimitive = valueElement.getAsJsonPrimitive();
        if (jsonPrimitive.isNumber()) {
          result.put(key, jsonPrimitive.getAsNumber());
        } else if (jsonPrimitive.isString()) {
          result.put(key, jsonPrimitive.getAsString());
        } else if (jsonPrimitive.isBoolean()) {
          result.put(key, jsonPrimitive.getAsBoolean());
        }
      } else if (valueElement.isJsonArray()) {
        result.put(key, flattenJsonObjectsToMaps(valueElement.getAsJsonArray()));
      } else if (valueElement.isJsonObject()) {
        JsonObject valueObject = valueElement.getAsJsonObject();
        if (key.equals(NOT_FLATTENED_FIELD)) {
          result.put(key, flattenJsonObjectToMap(valueObject));
        } else {
          result.putAll(flattenJsonObjectToMap(valueObject));
        }
      }
    }
    return result;
  }
}
