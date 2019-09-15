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

import com.google.cloud.language.v1.AnalyzeEntitySentimentResponse;
import com.google.protobuf.MessageOrBuilder;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.Transform;
import io.cdap.google.common.NLPMethod;

/**
 * Sentiment analysis will provide the prevailing emotional opinion within a provided text. The API returns two values:
 * The "score" describes the emotional leaning of the text from -1 (negative) to +1 (positive), with 0 being neutral.
 *
 * The "magnitude" measures the strength of the emotion.
 *
 * The json returned by API:
 * {
 *    "entities":[
 *       {
 *          "mentions":[
 *             {
 *                "sentiment":{
 *                   "magnitude":0.9,
 *                   "score":0.9
 *                },
 *                "text":{
 *                   "beginOffset":7,
 *                   "content":"R&B music"
 *                },
 *                "type":"COMMON"
 *             }
 *          ],
 *          "metadata":{
 *
 *          },
 *          "name":"R&B music",
 *          "salience":0.5597628,
 *          "sentiment":{
 *             "magnitude":0.9,
 *             "score":0.9
 *          },
 *          "type":"WORK_OF_ART"
 *       }
 *    ],
 *    "language":"en"
 * }
 *
 * Is translated into:
 *
 * { # record
 *    "entities":[ # array
 *       { # record
 *          "mentions":[
 *             { # record
 *                "magnitude":0.9,
 *                "score":0.9
 *                "beginOffset":7,
 *                "content":"R&B music"
 *                "type":"COMMON"
 *             }
 *          ],
 *          "metadata":{ # map<string,string>
 *
 *          },
 *          "name":"R&B music",
 *          "salience":0.5597628,
 *          "magnitude":0.9,
 *          "score":0.9,
 *          "type":"WORK_OF_ART"
 *       }
 *    ],
 *    "language":"en"
 * }
 *
 */
@Plugin(type = Transform.PLUGIN_TYPE)
@Name("NLPAnalyzeEntitySentiment")
@Description("Sentiment analysis will provide the prevailing emotional opinion within a provided text. " +
  "The API returns two values:\n The \"score\" describes the emotional leaning of the text from -1 (negative) " +
  "to +1 (positive), with 0 being neutral.\n The \"magnitude\" measures the strength of the emotion.")
public class AnalyzeEntitySentimentTransform extends NLPTransform {
  private static final Schema SCHEMA =
    Schema.recordOf(AnalyzeEntitySentimentTransform.class.getSimpleName(),
                    Schema.Field.of("language", Schema.of(Schema.Type.STRING)),
                    Schema.Field.of("entities", Schema.arrayOf(ENTITY_SCORED)));

  public AnalyzeEntitySentimentTransform(NLPConfig config) {
    super(config);
  }

  @Override
  protected StructuredRecord getRecordFromResponse(MessageOrBuilder message) {
    AnalyzeEntitySentimentResponse response = (AnalyzeEntitySentimentResponse) message;

    StructuredRecord.Builder builder = StructuredRecord.builder(SCHEMA);
    builder.set("language", response.getLanguage());
    builder.set("entities", getEntities(response.getEntitiesList(), ENTITY_SCORED, MENTION_SCORED));
    return builder.build();
  }

  protected NLPMethod getMethod() {
    return NLPMethod.ANALYZE_ENTITY_SENTIMENT;
  }
}
