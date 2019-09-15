# NLP Analyze Entity Sentiment

Description
-----------
Transforms input text into an information provided by Google Natural Language API.

Sentiment analysis will provide the prevailing emotional opinion within a provided text. The API returns two values:
The score describes the emotional leaning of the text from -1 (negative) to +1 (positive), with 0 being neutral.
The magnitude measures the strength of the emotion.

See [official documentation](https://cloud.google.com/natural-language/docs/basics) for more information.

Credentials
-----------
If the plugin is run on a Google Cloud Dataproc cluster, the service account key does not need to be
provided and can be set to 'auto-detect'.
Credentials will be automatically read from the cluster environment.

If the plugin is not run on a Dataproc cluster, the path to a service account key must be provided.
The service account key can be found on the Dashboard in the Cloud Platform Console.
Make sure the account key has permission to access Natural Language API.
The service account key file needs to be available on every node in your cluster and
must be readable by all users running the job.

Properties
----------
**Source Field:** Field which contains an input text.

**Encoding**: Text encoding. Providing it is recommended because the API provides the beginning offsets for various 
outputs, such as tokens and mentions, and languages that natively use different text encodings may access offsets 
differently.

**Language Code**: Code of the language of the text data. E.g. en, jp, etc. If not provided
Google Natural Language API will autodetect the language.

See [supported languages](https://cloud.google.com/natural-language/docs/languages).

**Error Handling:** Error handling strategy to use when API request to Google Natural Language API fails

Possible values are:<br>
Stop on error - Fails pipeline due to erroneous record.

Send to error - Sends erroneous record's text to error port and continues.

Skip on error - Ignores erroneous records.

**Service Account File Path**: Path on the local file system of the service account key used for
authorization. Can be set to 'auto-detect' when running on a Dataproc cluster.
When running on other clusters, the file must be present on every node in the cluster.

Examples
----------

In the examples below. The records are presented in a form of json where:

{} represent schema records (including nested one).                       
[] represent are schema arrays.

```
{
   "entities":[
      {
         "mentions":[
            {
               "magnitude":0.9,
               "score":0.9
               "beginOffset":7,
               "content":"R&B music"
               "type":"COMMON"
            }
         ],
         "metadata":{

         },
         "name":"R&B music",
         "salience":0.5597628,
         "magnitude":0.9,
         "score":0.9,
         "type":"WORK_OF_ART"
      },
      ...
   ],
   "language":"en"
}
```