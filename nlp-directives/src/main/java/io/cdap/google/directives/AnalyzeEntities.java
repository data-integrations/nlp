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
 * Detects known entities like public figures or landmarks from a given text.
 */
@Plugin(type = Directive.TYPE)
@Name(AnalyzeEntities.NAME)
@Categories(categories = { "nlp"})
@Description("Entity Analysis is the process of detecting known entities " +
  "like public figures or landmarks from a given text. \n" +
  "Entity detection is very helpful for all kinds of classification " +
  "and topic modeling tasks.\n" +
  "A salience score is calculated. This score for an entity provides information about the importance or centrality" +
  "of that entity to the entire document text. ")
public class AnalyzeEntities extends BaseGoogleLanguageDirective implements Directive {
  public static final String NAME = "nlp-analyze-entities";

  @Override
  protected NLPMethod getNLPMethod() {
    return NLPMethod.ANALYZE_ENTITIES;
  }

  @Override
  protected String getName() {
    return NAME;
  }
}

