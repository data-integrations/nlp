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

package io.cdap.google.directives;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.google.common.NLPMethod;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.annotations.Categories;

/**
 * For a given text, Google’s syntax analysis will return a breakdown of all words with a rich set of
 * linguistic information for each token
 */
@Plugin(type = Directive.TYPE)
@Name(AnalyzeSyntax.NAME)
@Categories(categories = { "nlp"})
@Description("For a given text, Google’s syntax analysis will return a breakdown of all words with a rich set of " +
  "linguistic information for each token")
public class AnalyzeSyntax extends BaseGoogleLanguageDirective implements Directive {
  public static final String NAME = "nlp-analyze-syntax";

  @Override
  protected NLPMethod getNLPMethod() {
    return NLPMethod.ANALYZE_SYNTAX;
  }

  @Override
  protected String getName() {
    return NAME;
  }
}

