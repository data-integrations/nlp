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

package io.cdap.nlp.directives;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.nlp.directives.internal.LanguageServiceProvider;
import io.cdap.nlp.directives.internal.SentimentService;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ErrorRowException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.Pair;
import io.cdap.wrangler.api.ReportErrorAndProceed;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Identifier;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class <code>TextSentiment</code> redacts sensitive data from a column of text.
 * Implementation uses Cloud Data Loss Prevention (DLP) APIs. This directive provides
 * a way for user to specify the column which needs to be inspected to indentify various
 * info types that user specifies and redacts them.
 *
 * The directive creates a new column with redacted data.
 *
 * @see <a href="Cloud DLP">https://cloud.google.com/dlp/</a>
 */
@Plugin(type = Directive.TYPE)
@Name(TextSentiment.NAME)
@Categories(categories = { "nlp", "sentiment"})
@Description("Analyse sentiment of text column")
public class TextSentiment implements Directive {
  private static final Logger LOG = LoggerFactory.getLogger(TextSentiment.class);
  public static final String NAME = "text-sentiment";
  private ColumnName column;
  private Identifier projectId;
  private Text saPath;
  private Identifier lang;
  private SentimentService service;

  /**
   * Returns a <code>UsageDefinition</code> that defines the argument this directive expects.
   * The directive requires column-name and one or more info-types. Optionally when used in
   * a non-gcp enviroment, the user would have to provide reference to project-id and service account
   * path file.
   *
   * @return an <code>UsageDefinition</code> object that defines the arguments to directive.
   */
  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("column", TokenType.COLUMN_NAME);
    builder.define("lang", TokenType.IDENTIFIER, Optional.TRUE);
    builder.define("project-id", TokenType.IDENTIFIER, Optional.TRUE);
    builder.define("service-account-file-path", TokenType.TEXT, Optional.TRUE);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.column = args.value("column");

    this.lang = null;
    if (args.contains("lang")) {
      this.lang = args.value("lang");
    }

    if (args.contains("project-id")) {
      this.projectId = args.value("project-id");
    }

    if (args.contains("service-account-file-path")) {
      this.saPath = args.value("service-account-file-path");
    }

    try {
      LanguageServiceProvider provider = LanguageServiceProvider.instance(
        projectId != null ? projectId.value() : null,
        saPath != null ? saPath.value() : null
      );
      service = new SentimentService(provider.getClient(), provider.getProject());
      service.initialize(lang.value());
    } catch (Exception e) {
      throw new DirectiveParseException(e.getMessage());
    }
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext ctx)
    throws DirectiveExecutionException, ErrorRowException, ReportErrorAndProceed {
    for (Row row : rows) {
      int idx = row.find(column.value());
      if (idx == -1) {
        continue; // skip row if column doesn't exist.
      }
      Object value = row.getValue(idx);
      if (value instanceof String) {
        Pair<Float, Float> result = service.getResult((String) row.getValue(idx));
        row.add(String.format("%s_magnitude", column.value()), result.getFirst());
        row.add(String.format("%s_score", column.value()), result.getSecond());
      } else {
        throw new DirectiveExecutionException(
          String.format("Sentiment analysis using this can only be applied on a text field")
        );
      }
    }
    return rows;
  }

  @Override
  public void destroy() {
    // nothing to be done.
  }

}
