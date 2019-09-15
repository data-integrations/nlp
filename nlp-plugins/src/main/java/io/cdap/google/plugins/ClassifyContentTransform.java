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

import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.protobuf.MessageOrBuilder;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.Transform;
import io.cdap.google.common.NLPMethod;

/**
 * Classifies the input documents into a large set of categories. The categories are structured hierarchically.
 *
 * The json returned by API:
 * {
 *    "categories":[
 *       {
 *          "confidence":0.61,
 *          "name":"/Computers & Electronics"
 *       },
 *       {
 *          "confidence":0.53,
 *          "name":"/Internet & Telecom/Mobile & Wireless"
 *       },
 *       {
 *          "confidence":0.53,
 *          "name":"/News"
 *       }
 *    ]
 * }
 *
 * Is translated into:
 *
 * { # record
 *    "categories":[ # array
 *       { # record
 *          "confidence":0.61,
 *          "name":"/Computers & Electronics"
 *       },
 *       { # record
 *          "confidence":0.53,
 *          "name":"/Internet & Telecom/Mobile & Wireless"
 *       },
 *       { # record
 *          "confidence":0.53,
 *          "name":"/News"
 *       }
 *    ]
 * }
 *
 */
@Plugin(type = Transform.PLUGIN_TYPE)
@Name("NLPClassifyContent")
@Description("Classifies the input documents into a large set of categories. " +
  "The categories are structured hierarchically.")
public class ClassifyContentTransform extends NLPTransform {
  private static final Schema SCHEMA =
    Schema.recordOf(ClassifyContentTransform.class.getSimpleName(),
                    Schema.Field.of("categories", Schema.arrayOf(CATEGORY)));

  public ClassifyContentTransform(NLPConfig config) {
    super(config);
  }

  @Override
  protected StructuredRecord getRecordFromResponse(MessageOrBuilder message) {
    ClassifyTextResponse response = (ClassifyTextResponse) message;

    StructuredRecord.Builder builder = StructuredRecord.builder(SCHEMA);

    builder.set("categories", getCategories(response.getCategoriesList()));
    return builder.build();
  }

  protected NLPMethod getMethod() {
    return NLPMethod.CLASSIFY_CONTENT;
  }
}
