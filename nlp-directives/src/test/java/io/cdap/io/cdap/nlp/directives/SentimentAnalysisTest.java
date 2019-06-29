package io.cdap.io.cdap.nlp.directives;

import com.google.auth.oauth2.ServiceAccountCredentials;
import io.cdap.nlp.SentimentService;
import io.cdap.nlp.directive.SentimentAnalysis;
import io.cdap.wrangler.api.Pair;
import io.cdap.wrangler.api.RecipePipeline;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.test.TestingRig;
import io.cdap.wrangler.test.api.TestRecipe;
import io.cdap.wrangler.test.api.TestRows;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests <code>SentimentAnalysis</code>
 */
public class SentimentAnalysisTest {

  @Ignore
  @Test
  public void testBasic() throws Exception {
    TestRows rows = new TestRows();
    rows.add(new Row("body", "I am so happy"));

    TestRecipe recipe = new TestRecipe();
    recipe.add("text-sentiment :body");

    SentimentService mock = mock(SentimentService.class);
    ServiceAccountCredentials mock1 = mock(ServiceAccountCredentials.class);

    when(mock.getResult("I am so happy")).thenReturn(new Pair<Float, Float>(0.2f, 0.12f));

    RecipePipeline pipeline = TestingRig.pipeline(SentimentAnalysis.class, recipe);
    List<Row> actual = pipeline.execute(rows.toList());
    Assert.assertEquals(0.2f, actual.get(0).getValue("body_magnitude"));
  }

}
