package io.cdap.nlp.plugin;

import com.google.cloud.ServiceOptions;
import com.google.cloud.language.v1beta2.LanguageServiceClient;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.Emitter;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.StageSubmitterContext;
import io.cdap.cdap.etl.api.Transform;
import io.cdap.cdap.etl.api.TransformContext;
import io.cdap.nlp.LanguageService;
import io.cdap.nlp.LanguageServiceProvider;
import io.cdap.wrangler.api.Pair;
import io.cdap.wrangler.api.Row;

import javax.annotation.Nullable;

public abstract class NLPTransform extends Transform<StructuredRecord, StructuredRecord> {
  private Schema input;
  private NLPConfig config;
  private LanguageService service;

  protected abstract Schema getOutputSchema();

  protected Schema getInputSchema() {
    return input;
  };

  protected abstract LanguageService initializeService(LanguageServiceClient client, String project) throws Exception;

  protected NLPConfig getConfig() {
    return config;
  }

  @Override
  public void transform(StructuredRecord input, Emitter<StructuredRecord> emitter) throws Exception {
    StructuredRecord.Builder builder = StructuredRecord.builder(getOutputSchema());
    for (Schema.Field field : input.getSchema().getFields()) {
      builder.set(field.getName(), input.get(field.getName()));
    }
    String value = input.get(config.getField());
    if (value != null) {
      try {
        Row results = (Row) service.getResult(value);
        for (Pair<String, Object> field : results.getFields()) {
          builder.set(field.getFirst(), field.getSecond());
        }
        emitter.emit(builder.build());
      } catch (Exception e) {

      }
    }
  }

  @Override
  public void initialize(TransformContext context) throws Exception {
    super.initialize(context);
    input = context.getInputSchema();
    LanguageServiceProvider provider = LanguageServiceProvider.instance(
      getConfig().getProject() != null ? getConfig().getProject() : null,
      getConfig().getServiceAccountFilePath() != null ? getConfig().getServiceAccountFilePath() : null
    );
    service = initializeService(provider.getClient(), provider.getProject());
  }

  @Override
  public void configurePipeline(PipelineConfigurer configurer) throws IllegalArgumentException {
    super.configurePipeline(configurer);
    input = configurer.getStageConfigurer().getInputSchema();
  }

  @Override
  public void destroy() {
    super.destroy();
  }

  @Override
  public void prepareRun(StageSubmitterContext context) throws Exception {
    super.prepareRun(context);
  }

  public static class NLPConfig extends PluginConfig {
    public static final String AUTO_DETECT = "auto-detect";

    @Name("field")
    @Description("Field to used for text analysis")
    @Macro
    String field;

    @Name("language")
    @Description("Language of text")
    @Macro
    @Nullable
    String language;

    @Name("type")
    @Description("Type of document")
    @Macro
    @Nullable
    String type;

    @Description("Google Cloud Project ID, which uniquely identifies a project. "
      + "It can be found on the Dashboard in the Google Cloud Platform Console.")
    @Macro
    @Nullable
    protected String project;

    @Description("Path on the local file system of the service account key used "
      + "for authorization. Can be set to 'auto-detect' when running on a Dataproc cluster. "
      + "When running on other clusters, the file must be present on every node in the cluster.")
    @Macro
    @Nullable
    protected String serviceFilePath;

    public String getField() {
      return field;
    }

    public String getLanguage() {
      return language;
    }

    public String getType() {
      return type;
    }

    public String getProject() {
      String projectId = project;
      if (project == null || project.isEmpty() || AUTO_DETECT.equals(project)) {
        projectId = ServiceOptions.getDefaultProjectId();
      }
      if (projectId == null) {
        throw new IllegalArgumentException(
          "Could not detect Google Cloud project id from the environment. Please specify a project id.");
      }
      return projectId;
    }

    @Nullable
    public String getServiceAccountFilePath() {
      if (containsMacro("serviceFilePath") || serviceFilePath == null ||
        serviceFilePath.isEmpty() || AUTO_DETECT.equals(serviceFilePath)) {
        return null;
      }
      return serviceFilePath;
    }
  }
}
