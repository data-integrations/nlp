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

package io.cdap.google.plugins;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.format.UnexpectedFormatException;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.Emitter;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.InvalidEntry;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.Transform;
import io.cdap.google.common.NLPMethodExecutor;

/**
 * Analyses an input text via Google Language API. And returns the results of the analysis in form of a record.
 * All available Google NLP Methods are supported:
 * - Syntax Analysis
 * - Sentiment Analysis
 * - Entity Analysis
 * - Entity Sentiment Analysis
 * - Text Classification
 * - Anotate Text
 */
@Plugin(type = "transform")
@Name("NLPTransform")
@Description("Transforms input text into an information provided by Google Natural Language API." +
  "The information includes syntax, sentiment, entities, classification of the text data.")
public class NLPTransform extends Transform<StructuredRecord, StructuredRecord> {
  private static final String ERROR_SCHEMA_BODY_PROPERTY = "body";
  private static final Schema STRING_ERROR_SCHEMA = Schema.recordOf("stringError",
                                                                    Schema.Field.of(ERROR_SCHEMA_BODY_PROPERTY,
                                                                                    Schema.of(Schema.Type.STRING)));
  private final NLPConfig config;

  public NLPTransform(NLPConfig config) {
    this.config = config;
  }

  public void configurePipeline(PipelineConfigurer pipelineConfigurer) {
    Schema inputSchema = pipelineConfigurer.getStageConfigurer().getInputSchema();
    FailureCollector failureCollector = pipelineConfigurer.getStageConfigurer().getFailureCollector();
    config.validate(failureCollector, inputSchema);
    failureCollector.getOrThrowException();
  }

  @Override
  public void transform(StructuredRecord structuredRecord, Emitter<StructuredRecord> emitter) {
    String text = structuredRecord.get(config.getSourceField());
    try (NLPMethodExecutor nlpMethodExecutor = config.getMethod().createExecutor(
      config.getServiceAccountFilePath(), config.getLanguageCode(), config.getEncodingType())) {
      try {
        emitter.emit(nlpMethodExecutor.executeAndReturnStructuredRecord(text));
      } catch (Exception e) {
        switch (config.getErrorHandling()) {
          case SKIP:
            break;
          case SEND:
            StructuredRecord.Builder builder = StructuredRecord.builder(STRING_ERROR_SCHEMA);
            builder.set(ERROR_SCHEMA_BODY_PROPERTY, text);
            emitter.emitError(new InvalidEntry<>(400, e.getMessage(), builder.build()));
            break;
          case STOP:
            throw e;
          default:
            throw new UnexpectedFormatException(
              String.format("Unknown error handling strategy '%s'", config.getErrorHandling()));
        }
      }
    }
  }
}
