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

package io.cdap.google.directives;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.cdap.wrangler.api.RecipePipeline;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.test.TestingRig;
import io.cdap.wrangler.test.api.TestRecipe;
import io.cdap.wrangler.test.api.TestRows;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Tests for the google nlp directives.
 *
 * To run the tests the path to a service account key must be provided.
 * The service account key can be found on the Dashboard in the Cloud Platform Console.
 * Make sure the account key has permission to access Natural Language API.
 *
 * mvn clean test -Dservice.account.file=<path-to-service-account-key-json>
 *
 */
public class TestDirectives {
  private static final Logger LOG = LoggerFactory.getLogger(TestDirectives.class);

  private static final JsonParser PARSER = new JsonParser();
  private static final String AUTHENTICATION_FILE = System.getProperty("service.account.file");

  @BeforeClass
  public static void initializeTests() {
    try {
      Assume.assumeNotNull(AUTHENTICATION_FILE);
    } catch (AssumptionViolatedException e) {
      LOG.warn("ETL tests are skipped. Please find the instructions on enabling it at README.md");
      throw e;
    }
  }

  @Test
  public void testAnalyzeSyntax() throws Exception {
    String text = "Time is the indefinite continued progress of existence and events that occur in an apparently " +
      "irreversible succession from the past, through the present, to the future.";
    TestRecipe recipe = new TestRecipe();

    recipe.add(String.format("nlp-analyze-syntax :body :result '%s'", AUTHENTICATION_FILE));

    TestRows rows = new TestRows();
    rows.add(new Row("body", text));

    RecipePipeline pipeline = TestingRig.pipeline(AnalyzeSyntax.class, recipe);
    List<Row> actuals = pipeline.execute(rows.toList());
    Assert.assertEquals(1, actuals.size());

    JsonObject jsonObject = PARSER.parse(actuals.get(0).getValue("result").toString()).getAsJsonObject();
    Assert.assertTrue(jsonObject.has("sentences"));
    Assert.assertTrue(jsonObject.has("tokens"));
    Assert.assertTrue(jsonObject.has("language"));

    Assert.assertEquals(1, jsonObject.getAsJsonArray("sentences").size());
    Assert.assertEquals(text, jsonObject.getAsJsonArray("sentences").get(0).
      getAsJsonObject().getAsJsonObject("text").getAsJsonPrimitive("content").getAsString());
    Assert.assertEquals("en", jsonObject.getAsJsonPrimitive("language").getAsString());

    // currently there is 29 tokens detected. Since response can change check that there are at least 5 tokens.
    Assert.assertTrue(jsonObject.getAsJsonArray("tokens").size() > 5);
  }

  @Test
  public void testAnalyzeEntities() throws Exception {
    String text = "Washington is the 18th largest state, with an area of 71,362 square miles (184,827 square km).";
    TestRecipe recipe = new TestRecipe();

    recipe.add(String.format("nlp-analyze-entities :body :result '%s'", AUTHENTICATION_FILE));

    TestRows rows = new TestRows();
    rows.add(new Row("body", text));

    RecipePipeline pipeline = TestingRig.pipeline(AnalyzeSyntax.class, recipe);
    List<Row> actuals = pipeline.execute(rows.toList());
    Assert.assertEquals(1, actuals.size());

    JsonObject jsonObject = PARSER.parse(actuals.get(0).getValue("result").toString()).getAsJsonObject();
    Assert.assertTrue(jsonObject.has("language"));
    Assert.assertTrue(jsonObject.has("entities"));

    Assert.assertEquals("en", jsonObject.getAsJsonPrimitive("language").getAsString());

    // There are 5 entities: Washington ; area ; 71,362 ; 18 ; 184,827. Check that there are at least two since API
    // results can change
    Assert.assertTrue(jsonObject.getAsJsonArray("entities").size() > 1);
  }

  @Test
  public void testAnalyzeSentiment() throws Exception {
    String text = "This test is so awesome!";
    TestRecipe recipe = new TestRecipe();

    recipe.add(String.format("nlp-analyze-sentiment :body :result '%s'", AUTHENTICATION_FILE));

    TestRows rows = new TestRows();
    rows.add(new Row("body", text));

    RecipePipeline pipeline = TestingRig.pipeline(AnalyzeSyntax.class, recipe);
    List<Row> actuals = pipeline.execute(rows.toList());
    Assert.assertEquals(1, actuals.size());

    JsonObject jsonObject = PARSER.parse(actuals.get(0).getValue("result").toString()).getAsJsonObject();
    Assert.assertTrue(jsonObject.has("language"));
    Assert.assertTrue(jsonObject.has("sentences"));
    Assert.assertTrue(jsonObject.has("documentSentiment"));

    Assert.assertEquals("en", jsonObject.getAsJsonPrimitive("language").getAsString());

    JsonObject documentSentiment = jsonObject.getAsJsonObject("documentSentiment");
    Assert.assertTrue(documentSentiment.has("magnitude"));
    Assert.assertTrue(documentSentiment.has("score"));
    Assert.assertTrue(documentSentiment.getAsJsonPrimitive("magnitude").isNumber());
    Assert.assertTrue(documentSentiment.getAsJsonPrimitive("score").isNumber());

    Assert.assertEquals(1, jsonObject.getAsJsonArray("sentences").size());

    JsonObject sentence = jsonObject.getAsJsonArray("sentences").get(0).getAsJsonObject();
    Assert.assertEquals(text, sentence.getAsJsonObject("text").getAsJsonPrimitive("content").getAsString());
    Assert.assertEquals(documentSentiment, sentence.getAsJsonObject("sentiment"));
  }


  @Test
  public void testAnalyzeEntititySentiment() throws Exception {
    String text = "This test is so awesome!";
    TestRecipe recipe = new TestRecipe();

    recipe.add(String.format("nlp-analyze-entity-sentiment :body :result '%s'", AUTHENTICATION_FILE));

    TestRows rows = new TestRows();
    rows.add(new Row("body", text));

    RecipePipeline pipeline = TestingRig.pipeline(AnalyzeSyntax.class, recipe);
    List<Row> actuals = pipeline.execute(rows.toList());
    Assert.assertEquals(1, actuals.size());

    JsonObject jsonObject = PARSER.parse(actuals.get(0).getValue("result").toString()).getAsJsonObject();
    Assert.assertTrue(jsonObject.has("language"));
    Assert.assertTrue(jsonObject.has("entities"));

    Assert.assertEquals("en", jsonObject.getAsJsonPrimitive("language").getAsString());

    // There is only one entity in the sentence "test".
    Assert.assertTrue(jsonObject.getAsJsonArray("entities").size() > 0);

    JsonObject entity = jsonObject.getAsJsonArray("entities").get(0).getAsJsonObject();
    Assert.assertTrue(entity.has("name"));
    Assert.assertTrue(entity.getAsJsonObject("sentiment").has("magnitude"));
    Assert.assertTrue(entity.getAsJsonObject("sentiment").has("score"));
  }

  @Test
  public void testClassifyText() throws Exception {
    String text = "In physics, acceleration is the rate of change of velocity of an object with respect to time. " +
      "An object's acceleration is the net result of all forces acting on the object, " +
      "as described by Newton's Second Law.";
    TestRecipe recipe = new TestRecipe();

    recipe.add(String.format("nlp-classify-text :body :result '%s'", AUTHENTICATION_FILE));

    TestRows rows = new TestRows();
    rows.add(new Row("body", text));

    RecipePipeline pipeline = TestingRig.pipeline(AnalyzeSyntax.class, recipe);
    List<Row> actuals = pipeline.execute(rows.toList());
    Assert.assertEquals(1, actuals.size());

    JsonObject jsonObject = PARSER.parse(actuals.get(0).getValue("result").toString()).getAsJsonObject();
    Assert.assertTrue(jsonObject.has("categories"));

    Assert.assertTrue(jsonObject.getAsJsonArray("categories").size() > 0);
    JsonObject category = jsonObject.getAsJsonArray("categories").get(0).getAsJsonObject();
    Assert.assertTrue(StringUtils.containsIgnoreCase(category.getAsJsonPrimitive("name").getAsString(),
                                                     "physics"));
    Assert.assertTrue(category.getAsJsonPrimitive("confidence").isNumber());
  }

  @Test
  public void testAnotateText() throws Exception {
    String text = "A military is a heavily-armed, highly organised force primarily intended for warfare, also known " +
      "collectively as armed forces. It is typically officially authorized and maintained by a sovereign state, " +
      "with its members identifiable by their distinct military uniform.";
    TestRecipe recipe = new TestRecipe();

    recipe.add(String.format("nlp-anotate-text :body :result '%s'", AUTHENTICATION_FILE));

    TestRows rows = new TestRows();
    rows.add(new Row("body", text));

    RecipePipeline pipeline = TestingRig.pipeline(AnalyzeSyntax.class, recipe);
    List<Row> actuals = pipeline.execute(rows.toList());
    Assert.assertEquals(1, actuals.size());

    JsonObject jsonObject = PARSER.parse(actuals.get(0).getValue("result").toString()).getAsJsonObject();
    Assert.assertTrue(jsonObject.has("sentences"));
    Assert.assertTrue(jsonObject.has("tokens"));
    Assert.assertTrue(jsonObject.has("entities"));
    Assert.assertTrue(jsonObject.has("documentSentiment"));
    Assert.assertTrue(jsonObject.has("language"));
    Assert.assertTrue(jsonObject.has("categories"));

    Assert.assertEquals("en", jsonObject.getAsJsonPrimitive("language").getAsString());

    Assert.assertTrue(jsonObject.getAsJsonArray("categories").size() > 0);
    JsonObject category = jsonObject.getAsJsonArray("categories").get(0).getAsJsonObject();
    Assert.assertTrue(category.getAsJsonPrimitive("confidence").isNumber());

    JsonObject documentSentiment = jsonObject.getAsJsonObject("documentSentiment");
    Assert.assertTrue(documentSentiment.has("magnitude"));
    Assert.assertTrue(documentSentiment.has("score"));
    Assert.assertTrue(documentSentiment.getAsJsonPrimitive("magnitude").isNumber());
    Assert.assertTrue(documentSentiment.getAsJsonPrimitive("score").isNumber());

    // there are currently 7 entities
    Assert.assertTrue(jsonObject.getAsJsonArray("entities").size() > 2);

    JsonObject entity = jsonObject.getAsJsonArray("entities").get(0).getAsJsonObject();
    Assert.assertTrue(entity.has("name"));
    Assert.assertTrue(entity.getAsJsonObject("sentiment").has("magnitude"));
    Assert.assertTrue(entity.getAsJsonObject("sentiment").has("score"));

    Assert.assertEquals(2, jsonObject.getAsJsonArray("sentences").size());

    JsonObject sentence = jsonObject.getAsJsonArray("sentences").get(0).getAsJsonObject();
    Assert.assertTrue(text.contains(sentence.getAsJsonObject("text")
                                      .getAsJsonPrimitive("content").getAsString()));

    // currently there is 45 tokens detected. Since response can change check that there are at least 5 tokens.
    Assert.assertTrue(jsonObject.getAsJsonArray("tokens").size() > 5);
  }

  @Test
  public void testLanguageAndEncodingArguments() throws Exception {
    String text = "I was really excited about visiting this place, and the mains were just fantastic, " +
      "but the rest of the experience was really disappointing.";

    TestRecipe recipe = new TestRecipe();

    recipe.add(String.format("nlp-analyze-sentiment :body :result '%s' 'UTF8' 'en'", AUTHENTICATION_FILE));

    TestRows rows = new TestRows();
    rows.add(new Row("body", text));

    RecipePipeline pipeline = TestingRig.pipeline(AnalyzeSyntax.class, recipe);
    List<Row> actuals = pipeline.execute(rows.toList());
    Assert.assertEquals(1, actuals.size());

    JsonObject jsonObject = PARSER.parse(actuals.get(0).getValue("result").toString()).getAsJsonObject();
    Assert.assertTrue(jsonObject.has("language"));
    Assert.assertTrue(jsonObject.has("sentences"));
    Assert.assertTrue(jsonObject.has("documentSentiment"));

    Assert.assertEquals("en", jsonObject.getAsJsonPrimitive("language").getAsString());

    JsonObject documentSentiment = jsonObject.getAsJsonObject("documentSentiment");
    Assert.assertTrue(documentSentiment.has("magnitude"));
    Assert.assertTrue(documentSentiment.has("score"));
    Assert.assertTrue(documentSentiment.getAsJsonPrimitive("magnitude").isNumber());
    Assert.assertTrue(documentSentiment.getAsJsonPrimitive("score").isNumber());

    Assert.assertEquals(1, jsonObject.getAsJsonArray("sentences").size());

    JsonObject sentence = jsonObject.getAsJsonArray("sentences").get(0).getAsJsonObject();
    Assert.assertEquals(text, sentence.getAsJsonObject("text").getAsJsonPrimitive("content").getAsString());
    Assert.assertEquals(documentSentiment, sentence.getAsJsonObject("sentiment"));
  }
}
