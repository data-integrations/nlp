# nlp-analyze-entity-sentiment

Sentiment analysis will provide the prevailing emotional opinion within a provided text. The API returns two values: 
The “score” describes the emotional leaning of the text from -1 (negative) to +1 (positive), with 0 being neutral.

The “magnitude” measures the strength of the emotion.

## Syntax
```
nlp-analyze-entity-sentiment <source-column> <destination-column> [authentication-file] [<encoding>] [<language>]```
```

`<source column>` a column which contains input text.<br>
`<destination-column>` a string column which will contain result of the json.<br>
`<authentication-file>` (optional) a local path to a service account key file. 
If not specified the path is retrieved from GOOGLE_APPLICATION_CREDENTIALS environment variable<br>
`<encoding>` (optional) Represents the text encoding that the caller uses to process the output. 
Providing an EncodingType is recommended because the API provides the beginning offsets for various outputs, 
such as tokens and mentions, and languages that natively use different text encodings may access offsets differently.
Possible values are NONE, UTF8, UTF16, UTF32<br>
`<language>` (optional) the language of the text within the request. If not specified, language will be automatically 
detected. Unsupported languages will return an error in the JSON response.<br>

The result of the functions is a json in format returned by Google NLP API. `json-path` directive can be used
for further actions on the json.

## Example
```
#pragma load-directives nlp-analyze-entity-sentiment;
nlp-analyze-entity-sentiment :body :result;
```

_Body_ is "I love R&B music. Marvin Gaye is the best. 'What's Going On' is one of my favorite songs. It was so sad 
when Marvin Gaye died."

_Result_ is:

```
{
   "entities":[
      {
         "mentions":[
            {
               "sentiment":{
                  "magnitude":0.9,
                  "score":0.9
               },
               "text":{
                  "beginOffset":7,
                  "content":"R&B music"
               },
               "type":"COMMON"
            }
         ],
         "metadata":{

         },
         "name":"R&B music",
         "salience":0.5597628,
         "sentiment":{
            "magnitude":0.9,
            "score":0.9
         },
         "type":"WORK_OF_ART"
      },
      {
         "mentions":[
            {
               "sentiment":{
                  "magnitude":0.8,
                  "score":0.8
               },
               "text":{
                  "beginOffset":18,
                  "content":"Marvin Gaye"
               },
               "type":"PROPER"
            },
            {
               "sentiment":{
                  "magnitude":0.1,
                  "score":-0.1
               },
               "text":{
                  "beginOffset":109,
                  "content":"Marvin Gaye"
               },
               "type":"PROPER"
            }
         ],
         "metadata":{
            "mid":"/m/012z8_",
            "wikipedia_url":"https://en.wikipedia.org/wiki/Marvin_Gaye"
         },
         "name":"Marvin Gaye",
         "salience":0.18719898,
         "sentiment":{
            "magnitude":1.0,
            "score":0.3
         },
         "type":"PERSON"
      }
      ...
   ],
   "language":"en"
}
```