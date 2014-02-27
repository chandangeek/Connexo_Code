# Adding a new guide

Guides can be found here: /com.elster.jupiter.unifyingjs/src/main/web/js/uni/docs/guides

To add a new guide please follow the following steps:

* Open "guides.json"
* Add a new item:

    { "name": "ui_agreements", "url": "ui_agreements", "title": "UI Agreements", "description": "This guides contains some UI agreements", "icon": "../icons/icon-00.png" }


* Fill in the correct name, url, title and description
* Create a new directory in /guides with the same name as the one used in guides.json
* Add the content in this folder (use markdown syntax see http://daringfireball.net/projects/markdown/syntax)
* After commit & rebuild the new guide should be visible here: http://deitvs027.eict.local:9090/unifyingjs

