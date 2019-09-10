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

import com.google.cloud.language.v1.AnnotateTextRequest;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.gson.JsonObject;
import com.google.protobuf.MessageOrBuilder;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;

/**
 * A directive that provides all the features that
 * nlp-analyze-entities, nlp-analyze-entity-sentiment, nlp-analyze-sentiment,
 * nlp-analyze-syntax, nlp-classify-text provide in one call.
 */
public class AnotateText extends NLPMethodExecutor {
  public AnotateText(String languageCode, EncodingType encoding, LanguageServiceClient language) {
    super(languageCode, encoding, language);
  }

  @Override
  protected StructuredRecord getRecordFromJson(String json) {
    Schema schema = Schema.recordOf(AnotateText.class.getName(),
                                    Schema.Field.of("language",
                                                    Schema.of(Schema.Type.STRING)),
                                    Schema.Field.of("score", Schema.of(Schema.Type.DOUBLE)),
                                    Schema.Field.of("magnitude", Schema.of(Schema.Type.DOUBLE)),

                                    Schema.Field.of("tokens", Schema.arrayOf(Schema.recordOf(
                                      "tokensRecord",
                                      Schema.Field.of("content", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("beginOffset", Schema.of(Schema.Type.LONG)),
                                      Schema.Field.of("tag", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("aspect", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("case", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("speechForm", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("gender", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("mood", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("number", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("person", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("proper", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("reciprocity", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("tense", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("voice", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("headTokenIndex",
                                                      Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("label", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("lemma", Schema.of(Schema.Type.STRING))
                                    ))),
                                    Schema.Field.of("sentences", Schema.arrayOf(Schema.recordOf(
                                      "sentencesRecord",
                                      Schema.Field.of("content", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("beginOffset", Schema.of(Schema.Type.LONG)),
                                      Schema.Field.of("score", Schema.of(Schema.Type.DOUBLE)),
                                      Schema.Field.of("magnitude", Schema.of(Schema.Type.DOUBLE))
                                    ))),
                                    Schema.Field.of("entities", Schema.arrayOf(Schema.recordOf(
                                      "entitiesRecord",
                                      Schema.Field.of("name", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("type", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("metadata", Schema.mapOf(
                                        Schema.of(Schema.Type.STRING), Schema.of(Schema.Type.STRING))),
                                      Schema.Field.of("salience", Schema.of(Schema.Type.DOUBLE)),
                                      Schema.Field.of("magnitude", Schema.of(Schema.Type.DOUBLE)),
                                      Schema.Field.of("score", Schema.of(Schema.Type.DOUBLE)),
                                      Schema.Field.of("mentions", Schema.arrayOf(Schema.recordOf(
                                        "mentionsRecord",
                                        Schema.Field.of("content", Schema.of(Schema.Type.STRING)),
                                        Schema.Field.of("beginOffset", Schema.of(Schema.Type.LONG)),
                                        Schema.Field.of("type", Schema.of(Schema.Type.STRING)),
                                        Schema.Field.of("magnitude", Schema.of(Schema.Type.DOUBLE)),
                                        Schema.Field.of("score", Schema.of(Schema.Type.DOUBLE))
                                      )))))),
                                    Schema.Field.of("categories", Schema.arrayOf(Schema.recordOf(
                                      "categoriesRecord",
                                      Schema.Field.of("name", Schema.of(Schema.Type.STRING)),
                                      Schema.Field.of("confidence", Schema.of(Schema.Type.DOUBLE))
                                    )))
    );
    JsonObject jsonObject = PARSER.parse(json).getAsJsonObject();

    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    builder.set("language", jsonObject.getAsJsonPrimitive("language").getAsString());
    builder.set("tokens", flattenJsonObjects(jsonObject.getAsJsonArray("tokens")));
    builder.set("sentences", flattenJsonObjects(jsonObject.getAsJsonArray("sentences")));
    builder.set("entities", flattenJsonObjects(jsonObject.getAsJsonArray("entities")));
    builder.set("score", jsonObject.getAsJsonObject("documentSentiment").get("score").getAsDouble());
    builder.set("magnitude", jsonObject.getAsJsonObject("documentSentiment").get("magnitude").getAsDouble());
    builder.set("categories", flattenJsonObjects(jsonObject.getAsJsonArray("categories")));
    return builder.build();
  }

  @Override
  protected MessageOrBuilder executeRequest(LanguageServiceClient language, Document document) {
    AnnotateTextRequest.Features features = AnnotateTextRequest.Features.newBuilder()
      .setClassifyText(true)
      .setExtractDocumentSentiment(true)
      .setExtractEntities(true)
      .setExtractEntitySentiment(true)
      .setExtractSyntax(true)
      .build();

    AnnotateTextRequest request = AnnotateTextRequest.newBuilder()
      .setDocument(document)
      .setFeatures(features)
      .setEncodingType(encoding)
      .build();

    return language.annotateText(request);
  }
}

