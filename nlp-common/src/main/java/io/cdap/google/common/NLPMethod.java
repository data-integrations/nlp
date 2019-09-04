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

/**
 * Represents a Google NLP method to run.
 */
public enum NLPMethod {
  ANALYZE_ENTITIES("Entity Analysis"),
  ANALYZE_ENTITY_SENTIMENT("Entity Sentiment Analysis"),
  ANALYZE_SENTIMENT("Sentiment Analysis"),
  ANALYZE_SYNTAX("Syntax Analysis"),
  ANOTATE_TEXT("ALL (Anotate text)"),
  CLASSIFY_CONTENT("Text Classification");

  private final String value;

  NLPMethod(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
