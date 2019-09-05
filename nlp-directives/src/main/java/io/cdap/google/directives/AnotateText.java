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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.google.common.NLPMethod;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.annotations.Categories;

/**
 * A directive that provides all the features that
 * nlp-analyze-entities, nlp-analyze-entity-sentiment, nlp-analyze-sentiment,
 * nlp-analyze-syntax, nlp-classify-text provide in one call.
 */
@Plugin(type = Directive.TYPE)
@Name(AnotateText.NAME)
@Categories(categories = { "nlp"})
@Description("A directive that provides all the features that \n" +
  "nlp-analyze-entities, nlp-analyze-entity-sentiment, nlp-analyze-sentiment,\n" +
  "nlp-analyze-syntax, nlp-classify-text provide in one call.")
public class AnotateText extends BaseGoogleLanguageDirective implements Directive {
  public static final String NAME = "nlp-anotate-text";

  @Override
  protected NLPMethod getNLPMethod() {
    return NLPMethod.ANOTATE_TEXT;
  }

  @Override
  protected String getName() {
    return NAME;
  }
}

