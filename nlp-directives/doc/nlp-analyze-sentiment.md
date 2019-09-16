# nlp-analyze-sentiment

Provides the prevailing emotional opinion within a provided text. The API returns two values: The score describes 
the emotional leaning of the text from -1 (negative) to +1 (positive), with 0 being neutral.

The magnitude measures the strength of the emotion.

## Syntax
```
nlp-analyze-sentiment <source-column> <destination-column> [authentication-file] [<encoding>] [<language>]
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
#pragma load-directives nlp-analyze-sentiment;
nlp-analyze-sentiment :body :result;
```

_Body_ is "Enjoy your vacation!"

_Result_ is:
```
{
  "documentSentiment": {
    "magnitude": 0.8,
    "score": 0.8
  },
  "language": "en",
  "sentences": [
    {
      "text": {
        "content": "Enjoy your vacation!",
        "beginOffset": 0
      },
      "sentiment": {
        "magnitude": 0.8,
        "score": 0.8
      }
    }
  ]
}
```