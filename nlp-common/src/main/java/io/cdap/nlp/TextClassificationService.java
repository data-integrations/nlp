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

package io.cdap.nlp;

import com.google.cloud.language.v1beta2.ClassificationCategory;
import com.google.cloud.language.v1beta2.ClassifyTextRequest;
import com.google.cloud.language.v1beta2.ClassifyTextResponse;
import com.google.cloud.language.v1beta2.Document;
import com.google.cloud.language.v1beta2.LanguageServiceClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.cdap.wrangler.api.Row;

import javax.annotation.Nullable;

/**
 *
 */
public class TextClassificationService implements LanguageService<Row, String> {
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

  /**
   * Invokes Classification API to extract categories representing the text document.
   *
   * @param text a <code>String</code> instance representing the text document.
   * @return a <code>Row</code> instance consisting of categories and confidence representing categories.
   */
  @Override
  @Nullable
  public Row getResult(String text) throws Exception {
    Row results = new Row();
    Document.Builder docBuilder = Document.newBuilder();
    if (lang != null) {
        docBuilder.setLanguage(lang);
    } else {
      return results;
    }

    Document doc = docBuilder.setContent(text)
      .setType(Document.Type.PLAIN_TEXT)
      .build();

    ClassifyTextRequest request = ClassifyTextRequest.newBuilder()
      .setDocument(doc)
      .build();
    
    // detect categories in the given text
    ClassifyTextResponse response = client.classifyText(request);

    JsonArray arrays = new JsonArray();
    for (ClassificationCategory category : response.getCategoriesList()) {
      JsonObject value = new JsonObject();
      value.add("name", new JsonPrimitive(category.getName()));
      value.add("confidence", new JsonPrimitive(category.getConfidence()));
      arrays.add(value);
    }
    results.add("categories", arrays);
    return results;
  }
}


