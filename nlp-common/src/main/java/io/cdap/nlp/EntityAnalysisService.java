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

import com.google.cloud.language.v1beta2.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1beta2.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1beta2.Document;
import com.google.cloud.language.v1beta2.EncodingType;
import com.google.cloud.language.v1beta2.Entity;
import com.google.cloud.language.v1beta2.EntityMention;
import com.google.cloud.language.v1beta2.LanguageServiceClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.cdap.wrangler.api.Row;

import java.util.Map;
import javax.annotation.Nullable;

/**
 *
 */
public class EntityAnalysisService implements LanguageService<Row, String> {
  private final LanguageServiceClient client;
  private String project;
  private String lang;

  public EntityAnalysisService(LanguageServiceClient client, String project) {
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
   * @return a <code>JsonArray</code> instance consisting of categories and confidence representing categories.
   */
  @Override
  @Nullable
  public Row getResult(String text) throws Exception {
    Row result = new Row();
    Document.Builder docBuilder = Document.newBuilder();
    if (lang != null) {
        docBuilder.setLanguage(lang);
    } else {
      return result;
    }

    Document doc = docBuilder.setContent(text)
      .setType(Document.Type.PLAIN_TEXT)
      .build();

    AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder()
      .setDocument(doc)
      .setEncodingType(EncodingType.UTF16)
      .build();
    
    // detect categories in the given text
    AnalyzeEntitiesResponse response = client.analyzeEntities(request);
    for (Entity entity : response.getEntitiesList()) {
      JsonObject value = new JsonObject();
      result.add("entity", entity.getName());
      result.add("type", entity.getType().getDescriptorForType().getFullName());
      result.add("salience", entity.getSalience());
      JsonObject metadata = new JsonObject();
      for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
        metadata.add(entry.getKey(), new JsonPrimitive(entity.getTypeValue()));
      }
      result.add("metadata", metadata);
      JsonArray mentions = new JsonArray();
      for (EntityMention mention : entity.getMentionsList()) {
        JsonObject mentionObject = new JsonObject();
        mentionObject.add("begin_offset", new JsonPrimitive(mention.getText().getBeginOffset()));
        mentionObject.add("content", new JsonPrimitive(mention.getText().getContent()));
        mentionObject.add("type", new JsonPrimitive(mention.getType().getDescriptorForType().getFullName()));
        mentions.add(mentionObject);
      }
      result.add("mentions", mentions);
    }
    return result;
  }
}


