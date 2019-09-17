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

package io.cdap.google.plugins;

import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.mock.common.MockEmitter;
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
 * Tests for the google nlp transform plugins.
 *
 * To run the tests the path to a service account key must be provided.
 * The service account key can be found on the Dashboard in the Cloud Platform Console.
 * Make sure the account key has permission to access Natural Language API.
 *
 * mvn clean test -Dservice.account.file=<path-to-service-account-key-json>
 *
 */
public class NLPTransformTest {
  private static final Logger LOG = LoggerFactory.getLogger(NLPTransformTest.class);

  private static final String AUTHENTICATION_FILE = System.getProperty("service.account.file");

  private static final String ERROR_SCHEMA_BODY_PROPERTY = "body";
  private static final Schema INPUT_SCHEMA = Schema.recordOf("stringInput",
                                                                    Schema.Field.of(ERROR_SCHEMA_BODY_PROPERTY,
                                                                                    Schema.of(Schema.Type.STRING)));

  @BeforeClass
  public static void initializeTests() {
    try {
      Assume.assumeNotNull(AUTHENTICATION_FILE);
    } catch (AssumptionViolatedException e) {
      LOG.warn("ETL tests are skipped. Please find the instructions on enabling it at " +
                 "README.md");
      throw e;
    }
  }

  @Test
  public void testAnalyzeSyntax() {
    String text = "Time is the indefinite continued progress of existence and events that occur in an apparently " +
      "irreversible succession from the past, through the present, to the future.";

    StructuredRecord record = StructuredRecord.builder(INPUT_SCHEMA).set("body", text).build();
    NLPConfig config = new NLPConfig("body", null, null,
                                     "stopOnError", AUTHENTICATION_FILE);
    MockEmitter<StructuredRecord> emitter = new MockEmitter<>();

    NLPTransform transform = new AnalyzeSyntaxTransform(config);
    transform.transform(record, emitter);

    StructuredRecord result = emitter.getEmitted().get(0);

    List<StructuredRecord> sentences = result.get("sentences");
    List<StructuredRecord> tokens = result.get("tokens");

    Assert.assertEquals(1, sentences.size());
    Assert.assertEquals(text, sentences.get(0).get("content"));
    Assert.assertEquals("en", result.get("language"));

    // currently there is 29 tokens detected. Since response can change check that there are at least 5 tokens.
    Assert.assertTrue(tokens.size() > 5);
  }

  @Test
  public void testAnalyzeEntities() {
    String text = "Washington is the 18th largest state, with an area of 71,362 square miles (184,827 square km).";

    StructuredRecord record = StructuredRecord.builder(INPUT_SCHEMA).set("body", text).build();
    NLPConfig config = new NLPConfig("body", null, null,
                                     "stopOnError", AUTHENTICATION_FILE);
    MockEmitter<StructuredRecord> emitter = new MockEmitter<>();

    NLPTransform transform = new AnalyzeEntitiesTransform(config);
    transform.transform(record, emitter);

    StructuredRecord result = emitter.getEmitted().get(0);

    Assert.assertEquals("en", result.get("language"));

    List<StructuredRecord> entities = result.get("entities");

    // There are 5 entities: Washington ; area ; 71,362 ; 18 ; 184,827. Check that there are at least two since API
    // results can change
    Assert.assertTrue(entities.size() > 1);
  }

  @Test
  public void testAnalyzeSentiment() {
    String text = "This test is so awesome!";
    StructuredRecord record = StructuredRecord.builder(INPUT_SCHEMA).set("body", text).build();
    NLPConfig config = new NLPConfig("body", null, null,
                                     "stopOnError", AUTHENTICATION_FILE);
    MockEmitter<StructuredRecord> emitter = new MockEmitter<>();

    NLPTransform transform = new AnalyzeSentimentTransform(config);
    transform.transform(record, emitter);

    StructuredRecord result = emitter.getEmitted().get(0);

    List<StructuredRecord> sentences = result.get("sentences");

    Assert.assertEquals("en", result.get("language"));

    Assert.assertTrue(result.get("magnitude") instanceof Number);
    Assert.assertTrue(result.get("score") instanceof Number);

    Assert.assertEquals(1, sentences.size());
    Assert.assertEquals(text, sentences.get(0).get("content"));
  }

  @Test
  public void testAnalyzeEntititySentiment() {
    String text = "This test is so awesome!";
    StructuredRecord record = StructuredRecord.builder(INPUT_SCHEMA).set("body", text).build();
    NLPConfig config = new NLPConfig("body",  null, null,
                                     "stopOnError", AUTHENTICATION_FILE);
    MockEmitter<StructuredRecord> emitter = new MockEmitter<>();

    NLPTransform transform = new AnalyzeEntitySentimentTransform(config);
    transform.transform(record, emitter);

    StructuredRecord result = emitter.getEmitted().get(0);

    Assert.assertEquals("en", result.get("language"));

    List<StructuredRecord> entities = result.get("entities");
    // There is only one entity in the sentence "test".
    Assert.assertTrue(entities.size() > 0);

    StructuredRecord entity = entities.get(0);
    Assert.assertTrue(entity.get("name") instanceof String);
    Assert.assertTrue(entity.get("magnitude") instanceof Number);
    Assert.assertTrue(entity.get("score") instanceof Number);
  }

  @Test
  public void testClassifyText() {
    String text = "In physics, acceleration is the rate of change of velocity of an object with respect to time. " +
      "An object's acceleration is the net result of all forces acting on the object, " +
      "as described by Newton's Second Law.";
    StructuredRecord record = StructuredRecord.builder(INPUT_SCHEMA).set("body", text).build();
    NLPConfig config = new NLPConfig("body", null, null,
                                     "stopOnError", AUTHENTICATION_FILE);
    MockEmitter<StructuredRecord> emitter = new MockEmitter<>();

    NLPTransform transform = new ClassifyContentTransform(config);
    transform.transform(record, emitter);

    StructuredRecord result = emitter.getEmitted().get(0);
    List<StructuredRecord> categories = result.get("categories");

    Assert.assertTrue(categories.size() > 0);
    StructuredRecord category = categories.get(0);
    Assert.assertTrue(StringUtils.containsIgnoreCase(category.get("name").toString(), "physics"));
    Assert.assertTrue(category.get("confidence") instanceof Number);
  }

  @Test
  public void testAnotateText() {
    String text = "A military is a heavily-armed, highly organised force primarily intended for warfare, also known " +
      "collectively as armed forces. It is typically officially authorized and maintained by a sovereign state, " +
      "with its members identifiable by their distinct military uniform.";
    StructuredRecord record = StructuredRecord.builder(INPUT_SCHEMA).set("body", text).build();
    NLPConfig config = new NLPConfig("body", null, null,
                                     "stopOnError", AUTHENTICATION_FILE);
    MockEmitter<StructuredRecord> emitter = new MockEmitter<>();

    NLPTransform transform = new AnotateTextTransform(config);
    transform.transform(record, emitter);

    StructuredRecord result = emitter.getEmitted().get(0);

    List<StructuredRecord> sentences = result.get("sentences");
    List<StructuredRecord> tokens = result.get("tokens");
    List<StructuredRecord> entities = result.get("entities");
    List<StructuredRecord> categories = result.get("categories");

    Assert.assertEquals("en", result.get("language"));

    Assert.assertTrue(categories.size() > 0);
    StructuredRecord category = categories.get(0);
    Assert.assertTrue(category.get("confidence") instanceof Number);

    Assert.assertTrue(result.get("magnitude") instanceof Number);
    Assert.assertTrue(result.get("score") instanceof Number);

    // there are currently 7 entities
    Assert.assertTrue(entities.size() > 2);

    StructuredRecord entity = entities.get(0);
    Assert.assertTrue(entity.get("name") instanceof String);
    Assert.assertTrue(entity.get("magnitude") instanceof Number);
    Assert.assertTrue(entity.get("score") instanceof Number);

    Assert.assertEquals(2, sentences.size());

    Assert.assertTrue(text.contains(sentences.get(0).get("content").toString()));

    // currently there is 45 tokens detected. Since response can change check that there are at least 5 tokens.
    Assert.assertTrue(tokens.size() > 5);
  }

  @Test
  public void testLanguageAndEncodingArguments() {
    String text = "I was really excited about visiting this place, and the mains were just fantastic, " +
      "but the rest of the experience was really disappointing.";

    StructuredRecord record = StructuredRecord.builder(INPUT_SCHEMA).set("body", text).build();
    NLPConfig config = new NLPConfig("body", "UTF8", "en",
                                     "stopOnError", AUTHENTICATION_FILE);
    MockEmitter<StructuredRecord> emitter = new MockEmitter<>();

    NLPTransform transform = new AnalyzeSentimentTransform(config);
    transform.transform(record, emitter);

    StructuredRecord result = emitter.getEmitted().get(0);

    List<StructuredRecord> sentences = result.get("sentences");

    Assert.assertEquals("en", result.get("language"));

    Assert.assertTrue(result.get("magnitude") instanceof Number);
    Assert.assertTrue(result.get("score") instanceof Number);

    Assert.assertEquals(1, sentences.size());
    Assert.assertEquals(text, (sentences.get(0)).get("content"));
  }
}
