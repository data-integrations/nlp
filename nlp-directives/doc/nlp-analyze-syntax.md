# nlp-analyze-syntax

For a given text, Google’s syntax analysis will return a breakdown of all words with a rich set of linguistic information for each token. The information can be divided into two parts:

1. Part of speech. This part contains information about the morphology of each token. For each word, a fine-grained 
analysis is returned containing its type (noun, verb, etc.), gender, grammatical case, tense, grammatical mood, grammatical voice, and much more.

2. Dependency trees. The second part of the return is called a dependency tree, which describes the syntactic structure of each sentence. 

## Syntax
```
nlp-analyze-syntax <source-column> <destination-column> [authentication-file] [<encoding>] [<language>]```
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
#pragma load-directives nlp-analyze-syntax;
nlp-analyze-syntax :body :result;
```

_Body_ is "Google, headquartered in Mountain View, unveiled the new Android phone at the Consumer Electronic Show. 
Sundar Pichai said in his keynote that users love their new Android phones."

_Result_ is:
```
{
  "sentences": [
    {
      "text": {
        "content": "Google, headquartered in Mountain View, unveiled the new Android phone at the Consumer Electronic Show.",
        "beginOffset": 0
      }
    },
    {
      "text": {
        "content": "Sundar Pichai said in his keynote that users love their new Android phones.",
        "beginOffset": 105
      }
    }
  ],
  "tokens": [
    {
      "text": {
        "content": "Google",
        "beginOffset": 0
      },
      "partOfSpeech": {
        "tag": "NOUN",
        "aspect": "ASPECT_UNKNOWN",
        "case": "CASE_UNKNOWN",
        "form": "FORM_UNKNOWN",
        "gender": "GENDER_UNKNOWN",
        "mood": "MOOD_UNKNOWN",
        "number": "SINGULAR",
        "person": "PERSON_UNKNOWN",
        "proper": "PROPER",
        "reciprocity": "RECIPROCITY_UNKNOWN",
        "tense": "TENSE_UNKNOWN",
        "voice": "VOICE_UNKNOWN"
      },
      "dependencyEdge": {
        "headTokenIndex": 7,
        "label": "NSUBJ"
      },
      "lemma": "Google"
    },
    ...
    {
      "text": {
        "content": ".",
        "beginOffset": 179
      },
      "partOfSpeech": {
        "tag": "PUNCT",
        "aspect": "ASPECT_UNKNOWN",
        "case": "CASE_UNKNOWN",
        "form": "FORM_UNKNOWN",
        "gender": "GENDER_UNKNOWN",
        "mood": "MOOD_UNKNOWN",
        "number": "NUMBER_UNKNOWN",
        "person": "PERSON_UNKNOWN",
        "proper": "PROPER_UNKNOWN",
        "reciprocity": "RECIPROCITY_UNKNOWN",
        "tense": "TENSE_UNKNOWN",
        "voice": "VOICE_UNKNOWN"
      },
      "dependencyEdge": {
        "headTokenIndex": 20,
        "label": "P"
      },
      "lemma": "."
    }
  ],
  "language": "en"
}
```