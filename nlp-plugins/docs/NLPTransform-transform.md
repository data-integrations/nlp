# Google NLP Transform

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

**Method Name**: Name of Google Natural Language API Method.

Possible values are:

_Entity Analysis_ - finds named entities (currently proper names and common nouns) in the text along with entity types, 
salience, mentions for each entity, and other properties.

_Entity Sentiment Analysis_ - finds entities, similar to AnalyzeEntities in the text and analyzes sentiment associated 
with each entity and its mentions.

_Sentiment Analysis_ - analyzes the sentiment of the provided text.

_Syntax Analysis_ - analyzes the syntax of the text and provides sentence boundaries and tokenization along with part 
of speech tags, dependency trees, and other properties.

_ALL (Anotate text)_ - a convenience method that provides all the features that analyzeSentiment, analyzeEntities, 
and analyzeSyntax provide in one call.

_Text Classification_ - classifies a document into categories.

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

_Example 1. Syntax analysis._


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

_Example 2. Sentiment analysis._
```
{
  "magnitude": 0.8,
  "score": 0.8
  "language": "en",
  "sentences": [
    {
      "content": "Enjoy your vacation!",
      "beginOffset": 0
      "magnitude": 0.8,
      "score": 0.8
    }
  ]
}
```

_Example 3. Entity analysis._
```
{
  "entities": [
    {
      "name": "1600 Pennsylvania Ave NW, Washington, DC",
      "type": "ADDRESS",
      "metadata": {
        "country": "US",
        "sublocality": "Fort Lesley J. McNair",
        "locality": "Washington",
        "street_name": "Pennsylvania Avenue Northwest",
        "broad_region": "District of Columbia",
        "narrow_region": "District of Columbia",
        "street_number": "1600"
      },
      "salience": 0,
      "mentions": [
        {
          "content": "1600 Pennsylvania Ave NW, Washington, DC",
          "beginOffset": 60
          "type": "TYPE_UNKNOWN"
        }
      ]
    },
    ...
  ],
  "language": "en"
}
```

_Example 4. Entity sentiment analysis._
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


_Example 5. Classify content._

```
{
   "categories":[
      {
         "confidence":0.61,
         "name":"/Computers & Electronics"
      },
      {
         "confidence":0.53,
         "name":"/Internet & Telecom/Mobile & Wireless"
      },
      {
         "confidence":0.53,
         "name":"/News"
      }
   ]
}
```