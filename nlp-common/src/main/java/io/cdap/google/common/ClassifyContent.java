/*
 *  Copyright © 2017-2019 Cask Data, Inc.
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

import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.protobuf.MessageOrBuilder;

import java.io.IOException;

/**
 * Classifies the input documents into a large set of categories. The categories are structured hierarchically.
 */
public class ClassifyContent extends NLPMethodExecutor {

  public ClassifyContent(String authenticationFile, String languageCode, EncodingType encoding) throws IOException {
    super(authenticationFile, languageCode, encoding);
  }

  @Override
  protected MessageOrBuilder executeRequest(LanguageServiceClient language, Document document) {
    ClassifyTextRequest request = ClassifyTextRequest.newBuilder()
      .setDocument(document)
      .build();

    return language.classifyText(request);
  }
}

