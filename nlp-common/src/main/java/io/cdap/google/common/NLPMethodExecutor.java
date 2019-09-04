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

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Abstract class that executes a method of Google Language API.
 */
public abstract class NLPMethodExecutor implements Closeable {
  private final String languageCode;
  private final LanguageServiceClient language;

  protected final EncodingType encoding;

  public NLPMethodExecutor(String serviceFilePath, String languageCode, EncodingType encoding) throws IOException {
    this.languageCode = languageCode;
    this.encoding = encoding;

    LanguageServiceSettings.Builder languageServiceSettingsBuilder = LanguageServiceSettings.newBuilder();
    if (serviceFilePath != null) {
      try {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(serviceFilePath));
        languageServiceSettingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
      } catch (IOException e) {
        throw new IllegalArgumentException(
          String.format("Cannot read credentials from service account key file '%s'",
                        serviceFilePath), e);
      }
    }

    language = LanguageServiceClient.create(languageServiceSettingsBuilder.build());
  }

  public String execute(String text) {
    Document.Builder documentBuilder = Document.newBuilder()
      .setContent(text)
      .setType(Document.Type.PLAIN_TEXT);

    if (languageCode != null) {
      documentBuilder.setLanguage(languageCode);
    }

    MessageOrBuilder response = executeRequest(language, documentBuilder.build());
    try {
      String resultJson = JsonFormat.printer().print(response);
      return resultJson;
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalArgumentException("Cannot convert Google NLP API response to a json", e);
    }
  }

  @Override
  public void close() {
    if (language != null) {
      language.close();
    }
  }

  protected abstract MessageOrBuilder executeRequest(LanguageServiceClient language, Document document);
}
