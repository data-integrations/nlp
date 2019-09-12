# NLP Anotate Text

Description
-----------
Transforms input text into an information provided by Google Natural Language API.
The information includes syntax, sentiment, entities, classification of the text data.

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

In the examples below. The records are shown in a form of json where:

{} represent schema records (including nested one).                       
[] represent are schema arrays.

```
{
   "sentences":[
      {
         "content":"A military is a heavily-armed, highly organised force primarily intended for warfare, also known collectively as armed forces.",
         "beginOffset":-1,
         "magnitude":0.7,
         "score":0.7
      },
      ...
   ],
   "tokens":[
      {
         "content":"A",
         "beginOffset":-1,
         "tag":"DET",
         "headTokenIndex":1,
         "label":"DET",
         "lemma":"A"
      },
      ...
   ],
   "entities":[
      {
         "name":"military",
         "type":"ORGANIZATION",
         "salience":0.43371573,
         "mentions":[
            {
               "content":"military",
               "beginOffset":-1,
               "type":"COMMON",
               "magnitude":0.3,
               "score":-0.3
            }
         ],
         "magnitude":0.3,
         "score":-0.3
      },
      ...
   ],
   "magnitude":1.0,
   "score":0.5,
   "language":"en",
   "categories":[
      {
         "name":"/Law \u0026 Government/Military",
         "confidence":0.98
      },
      ...
   ]
}
```