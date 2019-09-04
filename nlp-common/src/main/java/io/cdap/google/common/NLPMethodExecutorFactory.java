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
//
import com.google.cloud.language.v1.EncodingType;

import java.io.IOException;

/**
 * Creates an nlp method executor based on nlpMethod input arguments.
 */
public class NLPMethodExecutorFactory {
  public static NLPMethodExecutor createInstance(NLPMethod nlpMethod, String serviceFilePath, String languageCode,
                                         EncodingType encoding) throws IOException {
    switch (nlpMethod) {
      case ANALYZE_ENTITIES:
        return new AnalyzeEntities(serviceFilePath, languageCode, encoding);
      case ANALYZE_ENTITY_SENTIMENT:
        return new AnalyzeEntitySentiment(serviceFilePath, languageCode, encoding);
      case ANALYZE_SENTIMENT:
        return new AnalyzeSentiment(serviceFilePath, languageCode, encoding);
      case ANALYZE_SYNTAX:
        return new AnalyzeSyntax(serviceFilePath, languageCode, encoding);
      case ANOTATE_TEXT:
        return new AnotateText(serviceFilePath, languageCode, encoding);
      case CLASSIFY_CONTENT:
        return new ClassifyContent(serviceFilePath, languageCode, encoding);
      default:
        throw new IllegalArgumentException(String.format("Unsupported nlp method: '%s'", nlpMethod));
    }
  }
}
