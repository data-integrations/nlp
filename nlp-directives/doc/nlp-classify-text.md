# nlp-classify-text

Classifies the input documents into a large set of categories. The categories are structured hierarchical, 
e.g. the Category "Hobbies & Leisure" has several sub-categories, one of which would be "Hobbies & Leisure/Outdoors" 
which itself has sub-categories like "Hobbies & Leisure/Outdoors/Fishing."
## Syntax
```
nlp-classify-text <source-column> <destination-column> [authentication-file] [<encoding>] [<language>]```
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
#pragma load-directives nlp-classify-text;
nlp-classify-text :body :result;
```

_Body_ is "Google, headquartered in Mountain View, unveiled the new Android phone at the Consumer Electronic Show. 
Sundar Pichai said in his keynote that users love their new Android phones."

_Result_ is:
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