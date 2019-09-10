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

package io.cdap.google.plugins;

import com.google.cloud.language.v1.EncodingType;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.validation.InvalidConfigPropertyException;
import io.cdap.google.common.NLPMethod;

import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * A config for {@link NLPTransform} plugin
 */
public class NLPConfig extends PluginConfig {
  public static final String AUTO_DETECT = "auto-detect";

  public static final String PROPERTY_SOURCE_FIELD = "sourceField";
  public static final String PROPERTY_METHOD_NAME = "methodName";
  public static final String PROPERTY_ENCODING = "encoding";
  public static final String PROPERTY_LANGUAGE_CODE = "languageCode";
  public static final String PROPERTY_ERROR_HANDLING = "errorHandling";
  public static final String PROPERTY_SERVICE_ACCOUNT_FILE_PATH = "serviceFilePath";

  @Name(PROPERTY_SOURCE_FIELD)
  @Description("Field which contains an input text")
  @Macro
  private String sourceField;

  @Name(PROPERTY_METHOD_NAME)
  @Description("Name of Google Natural Language API Method")
  @Macro
  private String methodName;

  @Name(PROPERTY_ENCODING)
  @Description("Text encoding. Providing it is recommended because the API provides the beginning offsets for" +
    "various outputs, such as tokens and mentions, and languages that natively use different text encodings may" +
    "access offsets differently.")
  @Macro
  @Nullable
  private String encoding;


  @Name(PROPERTY_LANGUAGE_CODE)
  @Description("Code of the language of the text data. E.g. en, jp, etc. If not provided" +
    "Google Natural Language API will autodetect the language.")
  @Macro
  @Nullable
  private String languageCode;

  @Name(PROPERTY_ERROR_HANDLING)
  @Description("Error handling strategy to use when there is an during NLP API call.")
  private String errorHandling;

  @Name(PROPERTY_SERVICE_ACCOUNT_FILE_PATH)
  @Description("Path on the local file system of the service account key used "
    + "for authorization. Can be set to 'auto-detect' when running on a Dataproc cluster. "
    + "When running on other clusters, the file must be present on every node in the cluster.")
  @Macro
  @Nullable
  protected String serviceFilePath;

  public String getSourceField() {
    return sourceField;
  }

  public NLPMethod getMethod() {
    return Stream.of(NLPMethod.class.getEnumConstants())
      .filter(keyType -> keyType.getValue().equalsIgnoreCase(methodName))
      .findAny()
      .orElseThrow(() -> new InvalidConfigPropertyException(
        String.format("Unsupported value for '%s': '%s'", PROPERTY_METHOD_NAME, methodName), PROPERTY_METHOD_NAME));
  }

  public EncodingType getEncodingType() {
    if (encoding == null) {
      return EncodingType.NONE;
    }

    switch (encoding) {
      case "UTF8":
        return EncodingType.UTF8;
      case "UTF16":
        return EncodingType.UTF16;
      case "UTF32":
        return EncodingType.UTF32;
      default:
        throw new InvalidConfigPropertyException(String.format(
          "Type of encoding specified '%s' is not supported. " +
            "Supported values are NONE, UTF8, UTF16, UTF32.", encoding), PROPERTY_ENCODING
        );
    }
  }

  @Nullable
  public String getLanguageCode() {
    return languageCode;
  }

  public ErrorHandling getErrorHandling() {
    return Stream.of(ErrorHandling.class.getEnumConstants())
      .filter(keyType -> keyType.getValue().equalsIgnoreCase(errorHandling))
      .findAny()
      .orElseThrow(() -> new InvalidConfigPropertyException(
        String.format("Unsupported value for '%s': '%s'", PROPERTY_ERROR_HANDLING, errorHandling),
        PROPERTY_ERROR_HANDLING));
  }

  @Nullable
  public String getServiceAccountFilePath() {
    if (containsMacro(PROPERTY_SERVICE_ACCOUNT_FILE_PATH) || serviceFilePath == null ||
      serviceFilePath.isEmpty() || serviceFilePath.equals(AUTO_DETECT)) {
      return null;
    }
    return serviceFilePath;
  }

  public void validate(FailureCollector failureCollector, Schema inputSchema) {
    if (inputSchema.getField(sourceField) == null) {
      failureCollector.addFailure(String.format("Field '%s' does not exist in input schema", sourceField), null)
        .withConfigProperty(PROPERTY_SOURCE_FIELD);
    }

    // trigger getters, so that they fail if value cannot be converted to enum.
    try {
      getErrorHandling();
      getMethod();
      getEncodingType();
    } catch (InvalidConfigPropertyException ex) {
      failureCollector.addFailure(ex.getMessage(), null)
        .withConfigProperty(ex.getProperty());
    }
  }
}
