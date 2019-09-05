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
import com.google.protobuf.MessageOrBuilder;

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

