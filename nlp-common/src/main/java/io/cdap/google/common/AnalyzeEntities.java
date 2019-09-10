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

package io.cdap.google.common;

import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.gson.JsonObject;
import com.google.protobuf.MessageOrBuilder;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;

/**
 * Detects known entities like public figures or landmarks from a given text.
 */
public class AnalyzeEntities extends NLPMethodExecutor {
  public AnalyzeEntities(String languageCode, EncodingType encoding, LanguageServiceClient language) {
    super(languageCode, encoding, language);
  }

  @Override
  protected StructuredRecord getRecordFromJson(String json) {
    Schema schema = Schema.recordOf(AnalyzeEntities.class.getName(),
                           Schema.Field.of("language",
                                           Schema.of(Schema.Type.STRING)),
                          Schema.Field.of("entities", Schema.arrayOf(Schema.recordOf(
                            "entitiesRecord",
                            Schema.Field.of("name", Schema.of(Schema.Type.STRING)),
                            Schema.Field.of("type", Schema.of(Schema.Type.STRING)),
                            Schema.Field.of("metadata", Schema.mapOf(
                              Schema.of(Schema.Type.STRING), Schema.of(Schema.Type.STRING))),
                            Schema.Field.of("salience", Schema.of(Schema.Type.DOUBLE)),
                            Schema.Field.of("mentions", Schema.arrayOf(Schema.recordOf(
                              "mentionsRecord",
                              Schema.Field.of("content", Schema.of(Schema.Type.STRING)),
                              Schema.Field.of("beginOffset", Schema.of(Schema.Type.LONG)),
                              Schema.Field.of("type", Schema.of(Schema.Type.STRING))
                          ))))))

    );
    JsonObject jsonObject = PARSER.parse(json).getAsJsonObject();

    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    builder.set("language", jsonObject.getAsJsonPrimitive("language").getAsString());
    builder.set("entities", flattenJsonObjects(jsonObject.getAsJsonArray("entities")));
    return builder.build();
  }

  @Override
  protected MessageOrBuilder executeRequest(LanguageServiceClient language, Document document) {
    AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder()
      .setDocument(document)
      .setEncodingType(encoding)
      .build();

    return language.analyzeEntities(request);
  }
}

