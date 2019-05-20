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

package io.cdap.io.cdap.nlp.directives.internal;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.language.v1beta2.LanguageServiceClient;
import com.google.cloud.language.v1beta2.LanguageServiceSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class <code>LanguageServiceProvider</code> provides a singleton instance of <code>LanguageServiceClient</code>.
 *
 * <p>
 *
 * </p>
 */
public final class LanguageServiceProvider {
  private static final Logger LOG = LoggerFactory.getLogger(LanguageServiceProvider.class);
  private static LanguageServiceProvider singletonInstance = null;
  private final LanguageServiceClient client;
  private String project;

  private LanguageServiceProvider(LanguageServiceClient client, String project) {
    this.client = client;
    this.project = project;
  }

  /**
   *
   * @param projectId
   * @param saPath
   * @return
   */
  public static LanguageServiceProvider instance(String projectId, String saPath) throws Exception {
    if (singletonInstance == null) {
      synchronized (LanguageServiceProvider.class) {
        if (singletonInstance == null) {
          LanguageServiceSettings.Builder builder = LanguageServiceSettings.newBuilder();
          if (saPath != null) {
            File credentialsPath = new File(saPath);
            try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
              ServiceAccountCredentials serviceAccountCredentials =
                ServiceAccountCredentials.fromStream(serviceAccountStream);
              builder.setCredentialsProvider(new CredentialsProvider() {
                @Override
                public Credentials getCredentials() throws IOException {
                  return serviceAccountCredentials;
                }
              });
            }
          }
          LanguageServiceClient client = LanguageServiceClient.create(builder.build());
          if (projectId == null || projectId.isEmpty()) {
            projectId = ServiceOptions.getDefaultProjectId();
          }
          singletonInstance = new LanguageServiceProvider(client, projectId);
        }
      }
    }
    return singletonInstance;
  }

  public LanguageServiceClient getClient() {
    return client;
  }

  public String getProject() {
    return project;
  }
}
