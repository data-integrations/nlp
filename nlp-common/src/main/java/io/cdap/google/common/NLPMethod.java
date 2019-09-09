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

import com.google.cloud.language.v1.EncodingType;

/**
 * Represents a Google NLP method to run.
 */
public enum NLPMethod {
  ANALYZE_ENTITIES("Entity Analysis") {
    @Override
    public NLPMethodExecutor createExecutor(String serviceFilePath, String languageCode,
                                            EncodingType encoding) {
      return new AnalyzeEntities(languageCode, encoding,
                                 NLPMethodExecutor.createLanguageServiceClient(serviceFilePath));
    }
  },
  ANALYZE_ENTITY_SENTIMENT("Entity Sentiment Analysis") {
    @Override
    public NLPMethodExecutor createExecutor(String serviceFilePath, String languageCode,
                                            EncodingType encoding) {
      return new AnalyzeEntitySentiment(languageCode, encoding,
                                        NLPMethodExecutor.createLanguageServiceClient(serviceFilePath));
    }
  },
  ANALYZE_SENTIMENT("Sentiment Analysis") {
    @Override
    public NLPMethodExecutor createExecutor(String serviceFilePath, String languageCode,
                                            EncodingType encoding) {
      return new AnalyzeSentiment(languageCode, encoding,
                                  NLPMethodExecutor.createLanguageServiceClient(serviceFilePath));
    }
  },
  ANALYZE_SYNTAX("Syntax Analysis") {
    @Override
    public NLPMethodExecutor createExecutor(String serviceFilePath, String languageCode,
                                            EncodingType encoding) {
      return new AnalyzeSyntax(languageCode, encoding, NLPMethodExecutor.createLanguageServiceClient(serviceFilePath));
    }
  },
  ANOTATE_TEXT("ALL (Anotate text)") {
    @Override
    public NLPMethodExecutor createExecutor(String serviceFilePath, String languageCode,
                                            EncodingType encoding) {
      return new AnotateText(languageCode, encoding, NLPMethodExecutor.createLanguageServiceClient(serviceFilePath));
    }
  },
  CLASSIFY_CONTENT("Text Classification") {
    @Override
    public NLPMethodExecutor createExecutor(String serviceFilePath, String languageCode,
                                            EncodingType encoding) {
      return new ClassifyContent(languageCode, encoding,
                                 NLPMethodExecutor.createLanguageServiceClient(serviceFilePath));
    }
  };

  private final String value;

  NLPMethod(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public abstract NLPMethodExecutor createExecutor(String serviceFilePath, String languageCode,
                                                   EncodingType encoding);
}
