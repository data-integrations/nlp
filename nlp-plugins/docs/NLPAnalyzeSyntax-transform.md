# NLP Analyze Syntax

Description
-----------
Transforms input text into an information provided by Google Natural Language API.
For a given text, Google’s syntax analysis will return a breakdown of all words with a rich 
set of linguistic information for each token.

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
  "sentences": [
    {
      "content": "Google, headquartered in Mountain View, unveiled the new Android phone at the Consumer Electronic Show.",
      "beginOffset": 0
    },
    {
      "content": "Sundar Pichai said in his keynote that users love their new Android phones.",
      "beginOffset": 105
    }
  ],
  "tokens": [
    {
      "content": "Google",
      "beginOffset": 0
      "tag": "NOUN",
      "apect": "ASPECT_UNKNOWN",
      "case": "CASE_UNKNOWN",
      "speechForm": "FORM_UNKNOWN",
      "gender": "GENDER_UNKNOWN",
      "mood": "MOOD_UNKNOWN",
      "number": "SINGULAR",
      "person": "PERSON_UNKNOWN",
      "proper": "PROPER",
      "reciprocity": "RECIPROCITY_UNKNOWN",
      "tense": "TENSE_UNKNOWN",
      "voice": "VOICE_UNKNOWN"
      "dependencyEdgeHeadTokenIndex": 7,
      "dependencyEdgeLabel": "NSUBJ"
      "lemma": "Google"
    },
    ...
  ],
  "language": "en"
}
```