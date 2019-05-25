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

package io.cdap.nlp.directives.internal;

import com.google.cloud.language.v1beta2.ClassificationCategory;
import com.google.cloud.language.v1beta2.ClassifyTextRequest;
import com.google.cloud.language.v1beta2.ClassifyTextResponse;
import com.google.cloud.language.v1beta2.Document;
import com.google.cloud.language.v1beta2.LanguageServiceClient;
import io.cdap.nlp.directives.LanguageService;
import io.cdap.wrangler.api.Pair;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 *
 */
public class TextClassificationService implements LanguageService<List<Pair<String, Float>>, String> {
  private final LanguageServiceClient client;
  private String project;
  private String lang;

  public TextClassificationService(LanguageServiceClient client, String project) {
    this.client = client;
    this.project = project;
  }

  @Nullable
  public void initialize(String lang) {
    this.lang = lang;
  }

  @Override
  @Nullable
  public List<Pair<String, Float>> getResult(String text) {
    Document.Builder docBuilder = Document.newBuilder();
    if (lang != null) {
        docBuilder.setLanguage(lang);
    }
    Document doc = docBuilder.setContent(text)
      .setType(Document.Type.PLAIN_TEXT)
      .build();

    ClassifyTextRequest request = ClassifyTextRequest.newBuilder()
      .setDocument(doc)
      .build();
    
    // detect categories in the given text
    List<Pair<String, Float>> results = new ArrayList<>();
    ClassifyTextResponse response = client.classifyText(request);
    for (ClassificationCategory category : response.getCategoriesList()) {
      results.add(new Pair<>(category.getName(), category.getConfidence()));
    }
    return results;
  }
}


