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

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.cdap.cdap.api.data.format.StructuredRecord;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class that executes a method of Google Language API.
 */
public abstract class NLPMethodExecutor implements Closeable {
  protected static final JsonParser PARSER = new JsonParser();
  // metadata is dynamic and should be map<string,string>. Do not flatten it.
  protected static final String NOT_FLATTENED_FIELD = "metadata";

  private final String languageCode;
  private final LanguageServiceClient language;
  protected final EncodingType encoding;

  public NLPMethodExecutor(String languageCode, EncodingType encoding, LanguageServiceClient language) {
    this.languageCode = languageCode;
    this.encoding = encoding;
    this.language = language;
  }

  public String execute(String text) {
    Document.Builder documentBuilder = Document.newBuilder()
      .setContent(text)
      .setType(Document.Type.PLAIN_TEXT);

    if (languageCode != null) {
      documentBuilder.setLanguage(languageCode);
    }

    MessageOrBuilder response = executeRequest(language, documentBuilder.build());
    try {
      String resultJson = JsonFormat.printer().print(response);
      return resultJson;
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalArgumentException("Cannot convert Google NLP API response to a json", e);
    }
  }

  public StructuredRecord executeAndReturnStructuredRecord(String text) {
    return getRecordFromJson(execute(text));
  }

  @Override
  public void close() {
    if (language != null) {
      language.close();
    }
  }

  protected List<String> jsonArrayToList(JsonArray jsonArray) {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      result.add(jsonArray.get(i).getAsJsonObject().toString());
    }
    return result;
  }

  protected abstract StructuredRecord getRecordFromJson(String json);
  protected abstract MessageOrBuilder executeRequest(LanguageServiceClient language, Document document);

  protected static List<Map<String, Object>> flattenJsonObjects(JsonArray jsonArray) {
    List<Map<String, Object>> results = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      results.add(flattenJsonObject(jsonArray.get(i).getAsJsonObject()));
    }
    return results;
  }

  protected static Map<String, Object> flattenJsonObject(JsonObject jsonObject) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
      String key = entry.getKey();
      JsonElement valueElement = entry.getValue();

      if (valueElement.isJsonPrimitive()) {
        JsonPrimitive jsonPrimitive = valueElement.getAsJsonPrimitive();
        if (jsonPrimitive.isNumber()) {
          // CDAP structured record builder does not understand type "Number", so we have to convert this
          // into double. Which seems like than gets correctly casted to other types (int, long, float).
          result.put(key, jsonPrimitive.getAsNumber().doubleValue());
        } else if (jsonPrimitive.isString()) {
          result.put(key, jsonPrimitive.getAsString());
        } else if (jsonPrimitive.isBoolean()) {
          result.put(key, jsonPrimitive.getAsBoolean());
        }
      } else if (valueElement.isJsonArray()) {
        result.put(key, flattenJsonObjects(valueElement.getAsJsonArray()));
      } else if (valueElement.isJsonObject()) {
        JsonObject valueObject = valueElement.getAsJsonObject();
        if (key.equals(NOT_FLATTENED_FIELD)) {
          result.put(key, flattenJsonObject(valueObject));
        } else {
          result.putAll(flattenJsonObject(valueObject));
        }
      }
    }
    return result;
  }

  public static LanguageServiceClient createLanguageServiceClient(String serviceFilePath) {
    LanguageServiceSettings.Builder languageServiceSettingsBuilder = LanguageServiceSettings.newBuilder();
    try {
      if (serviceFilePath != null) {
          GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(serviceFilePath));
          languageServiceSettingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
      }
      return LanguageServiceClient.create(languageServiceSettingsBuilder.build());
    } catch (IOException e) {
      throw new IllegalArgumentException(
        String.format("Cannot read credentials from service account key file '%s' or create a language client",
                      serviceFilePath), e);
    }
  }
}
