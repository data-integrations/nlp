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

package io.cdap.google.directives;

import com.google.cloud.language.v1.EncodingType;
import io.cdap.google.common.NLPMethod;
import io.cdap.google.common.NLPMethodExecutor;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * A base class for nlp directives. The class is responsible for most directive related work as well as
 * Getting the json response from Google NLP API.
 */
public abstract class BaseGoogleLanguageDirective {
  protected EncodingType encoding = EncodingType.UTF8;
  private ColumnName source;
  private ColumnName destination;
  private String serviceFilePath;
  private String languageCode;

  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(getName());
    builder.define("source", TokenType.COLUMN_NAME);
    builder.define("destination", TokenType.COLUMN_NAME);
    builder.define("authentication-file", TokenType.TEXT, Optional.TRUE);
    builder.define("encoding", TokenType.TEXT, Optional.TRUE);
    builder.define("language", TokenType.TEXT, Optional.TRUE);
    return builder.build();
  }

  public void initialize(Arguments args) throws DirectiveParseException {
    this.source = args.value("source");
    this.destination = args.value("destination");

    if (args.contains("authentication-file")) {
      this.serviceFilePath = ((Text) args.value("authentication-file")).value();
    }

    if (args.contains("encoding")) {
      String encodingString = ((Text) args.value("encoding")).value();

      switch (encodingString.toUpperCase()) {
        case "NONE":
          encoding = EncodingType.NONE;
          break;
        case "UTF8":
          encoding = EncodingType.UTF8;
          break;
        case "UTF16":
          encoding = EncodingType.UTF16;
          break;
        case "UTF32":
          encoding = EncodingType.UTF32;
          break;
        default:
          throw new DirectiveParseException(String.format(
            "Type of encoding specified '%s' is not supported. " +
              "Supported values are NONE, UTF8, UTF16, UTF32.", encodingString)
          );
      }
    }

    if (args.contains("language")) {
      languageCode = ((Text) args.value("language")).value();
    }
  }

  public void destroy() {
    // no-op
  }

  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    try (NLPMethodExecutor nlpMethodExecutor = getNLPMethod().createExecutor(serviceFilePath, languageCode, encoding)) {
      for (Row row : rows) {
        int sidx = row.find(source.value());
        if (sidx == -1) {
          throw new DirectiveExecutionException(String.format(
            "Error encountered while executing '%s' : Column '%s' not found", getName(), source.value()));
        }

        String text = (String) row.getValue(sidx);
        String resultJson = nlpMethodExecutor.execute(text);
        row.addOrSet(destination.value(), resultJson);
      }
      return rows;
    }
  }

  protected abstract String getName();
  protected abstract NLPMethod getNLPMethod();
}

