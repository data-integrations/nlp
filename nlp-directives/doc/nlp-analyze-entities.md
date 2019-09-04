# nlp-analyze-entities

Entity Analysis is the process of detecting known entities like public figures or landmarks from a given text. 
Entity detection is very helpful for all kinds of classification and topic modeling tasks.

A salience score is calculated. This score for an entity provides information about the importance or centrality of 
that entity to the entire document text. 
## Syntax
```
nlp-analyze-entities <source-column> <destination-column> [authentication-file] [<encoding>] [<language>]
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
#pragma load-directives nlp-analyze-entities;
nlp-analyze-entities :body :result;
```

_Body_ is "President Trump will speak from the White House, located at 1600 Pennsylvania Ave NW, Washington, DC, on 
October 7."

_Result_ is:
```
{
  "entities": [
    {
      "name": "Trump",
      "type": "PERSON",
      "metadata": {
        "mid": "/m/0cqt90",
        "wikipedia_url": "https://en.wikipedia.org/wiki/Donald_Trump"
      },
      "salience": 0.7936003,
      "mentions": [
        {
          "text": {
            "content": "Trump",
            "beginOffset": 10
          },
          "type": "PROPER"
        },
        {
          "text": {
            "content": "President",
            "beginOffset": 0
          },
          "type": "COMMON"
        }
      ]
    },
    {
      "name": "White House",
      "type": "LOCATION",
      "metadata": {
        "mid": "/m/081sq",
        "wikipedia_url": "https://en.wikipedia.org/wiki/White_House"
      },
      "salience": 0.09172433,
      "mentions": [
        {
          "text": {
            "content": "White House",
            "beginOffset": 36
          },
          "type": "PROPER"
        }
      ]
    },
    {
      "name": "Pennsylvania Ave NW",
      "type": "LOCATION",
      "metadata": {
        "mid": "/g/1tgb87cq"
      },
      "salience": 0.085507184,
      "mentions": [
        {
          "text": {
            "content": "Pennsylvania Ave NW",
            "beginOffset": 65
          },
          "type": "PROPER"
        }
      ]
    },
    {
      "name": "Washington, DC",
      "type": "LOCATION",
      "metadata": {
        "mid": "/m/0rh6k",
        "wikipedia_url": "https://en.wikipedia.org/wiki/Washington,_D.C."
      },
      "salience": 0.029168168,
      "mentions": [
        {
          "text": {
            "content": "Washington, DC",
            "beginOffset": 86
          },
          "type": "PROPER"
        }
      ]
    }
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
          "text": {
            "content": "1600 Pennsylvania Ave NW, Washington, DC",
            "beginOffset": 60
          },
          "type": "TYPE_UNKNOWN"
        }
      ]
      }
    }
    ...
  ],
  "language": "en"
}
```