/*
 * Copyright © 2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.directives.internal;

import com.google.cloud.language.v1beta2.AnalyzeSentimentResponse;
import com.google.cloud.language.v1beta2.Document;
import com.google.cloud.language.v1beta2.LanguageServiceClient;
import com.google.cloud.language.v1beta2.Sentiment;
import io.cdap.directives.LanguageService;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ColumnName;

import javax.annotation.Nullable;

/**
 *
 */
public class SentimentService implements LanguageService<Row, String> {
  private final LanguageServiceClient client;
  private String project;
  private String lang;

  public SentimentService(LanguageServiceClient client, String project) {
    this.client = client;
    this.project = project;
  }

  @Nullable
  public void initialize(String lang) {
    this.lang = lang;
  }

  @Override
  @Nullable
  public Row getResult(ColumnName name, String text) {
    Document.Builder docBuilder = Document.newBuilder();
    if (lang != null) {
        docBuilder.setLanguage(lang);
    }
    Document doc = docBuilder.setContent(text)
      .setType(Document.Type.PLAIN_TEXT)
      .build();
    AnalyzeSentimentResponse response = client.analyzeSentiment(doc);
    Sentiment sentiment = response.getDocumentSentiment();
    if (sentiment != null) {
      Row row = new Row();
      row.add(String.format("%s_magnitude", name.value()), sentiment.getMagnitude());
      row.add(String.format("%s_score", name.value()), sentiment.getScore());
      return row;
    }
    return null;
  }
}


