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
 * Provides the prevailing emotional opinion within a provided text. The API returns two values: The "score" describes
 * the emotional leaning of the text from -1 (negative) to +1 (positive), with 0 being neutral.
 *
 * The "magnitude" measures the strength of the emotion.
 */
@Plugin(type = Directive.TYPE)
@Name(AnalyzeSentiment.NAME)
@Categories(categories = { "nlp"})
@Description("Provides the prevailing emotional opinion within a provided text. The API returns two values: " +
  "The \"score\" describes the emotional leaning of the text from -1 (negative) to +1 (positive), " +
  "with 0 being neutral. The \"magnitude\" measures the strength of the emotion.")
public class AnalyzeSentiment extends BaseGoogleLanguageDirective implements Directive {
  public static final String NAME = "nlp-analyze-sentiment";

  @Override
  protected NLPMethod getNLPMethod() {
    return NLPMethod.ANALYZE_SENTIMENT;
  }

  @Override
  protected String getName() {
    return NAME;
  }
}

