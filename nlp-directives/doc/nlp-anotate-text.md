# nlp-anotate-text

A convenience method that provides all the features that 
nlp-analyze-entities, nlp-analyze-entity-sentiment, nlp-analyze-sentiment,
nlp-analyze-syntax, nlp-classify-text provide in one call.
## Syntax
```
nlp-anotate-text <source-column> <destination-column> [authentication-file] [<encoding>] [<language>]```
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
```