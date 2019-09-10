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

import com.google.cloud.language.v1.AnalyzeSentimentRequest;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.gson.JsonObject;
import com.google.protobuf.MessageOrBuilder;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;

/**
 * Provides the prevailing emotional opinion within a provided text. The API returns two values: The "score" describes
 * the emotional leaning of the text from -1 (negative) to +1 (positive), with 0 being neutral.
 *
 * The "magnitude" measures the strength of the emotion.
 */
public class AnalyzeSentiment extends NLPMethodExecutor {
  public AnalyzeSentiment(String languageCode, EncodingType encoding, LanguageServiceClient language) {
    super(languageCode, encoding, language);
  }

  @Override
  protected StructuredRecord getRecordFromJson(String json) {
    Schema schema = Schema.recordOf(AnalyzeSentiment.class.getName(),
                                    Schema.Field.of("language", Schema.of(Schema.Type.STRING)),
                                    Schema.Field.of("score", Schema.of(Schema.Type.DOUBLE)),
                                    Schema.Field.of("magnitude", Schema.of(Schema.Type.DOUBLE)),
                                    Schema.Field.of("sentences", Schema.arrayOf(Schema.recordOf(
                                      "sentencesRecord",
                                      Schema.Field.of("content", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("beginOffset", Schema.of(Schema.Type.LONG)),
                                      Schema.Field.of("score", Schema.of(Schema.Type.DOUBLE)),
                                      Schema.Field.of("magnitude", Schema.of(Schema.Type.DOUBLE))
                                    )))

    );
    JsonObject jsonObject = PARSER.parse(json).getAsJsonObject();

    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    builder.set("language", jsonObject.getAsJsonPrimitive("language").getAsString());
    builder.set("score", jsonObject.getAsJsonObject("documentSentiment").get("score").getAsDouble());
    builder.set("magnitude", jsonObject.getAsJsonObject("documentSentiment").get("magnitude").getAsDouble());
    builder.set("sentences", flattenJsonObjects(jsonObject.getAsJsonArray("sentences")));
    return builder.build();
  }

  @Override
  protected MessageOrBuilder executeRequest(LanguageServiceClient language, Document document) {
    AnalyzeSentimentRequest request = AnalyzeSentimentRequest.newBuilder()
      .setDocument(document)
      .setEncodingType(encoding)
      .build();

    return language.analyzeSentiment(request);
  }
}

