/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

// Publish project specific data
(function () {
    rh = window.rh;
    model = rh.model;

    rh.consts('DEFAULT_TOPIC', encodeURI("#000017_EN_Connexo_1.0_User_guide/01_Cover_Page/01_Cover_Page.htm".substring(1)));
    rh.consts('HOME_FILEPATH', encodeURI("index.html"));
rh.consts('START_FILEPATH', encodeURI('index.html'));
    rh.consts('HELP_ID', '4A0ACE85-FABF-450B-A052-0540CE36F23A' || 'preview');
    rh.consts('LNG_STOP_WORDS', ["a", "about", "after", "against", "all", "also", "among", "an", "and", "are", "as", "at", "be", "became", "because", "been", "between", "but", "by", "can", "come", "do", "during", "each", "early", "for", "form", "found", "from", "had", "has", "have", "he", "her", "his", "however", "in", "include", "into", "is", "it", "its", "late", "later", "made", "many", "may", "me", "med", "more", "most", "near", "no", "non", "not", "of", "on", "only", "or", "other", "over", "several", "she", "some", "such", "than", "that", "the", "their", "then", "there", "these", "they", "this", "through", "to", "under", "until", "use", "was", "we", "were", "when", "where", "which", "who", "with", "you"]);
    rh.consts('LNG_SUBSTR_SEARCH', 0);

    model.publish(rh.consts('KEY_DIR'), "ltr");
    model.publish(rh.consts('KEY_LNG_NAME'), "");
    model.publish(rh.consts('KEY_LNG'), {
        "SearchResultsPerScreen": "Search results per page",
        "SyncToc": "SyncToc",
        "HomeButton": "Home",
        "WebSearchButton": "WebSearch",
        "GlossaryFilterTerms": "Find Term",
        "HighlightSearchResults": "Highlight Search Results",
        "WebSearch": "WebSearch",
        "Show": "Show",
        "ShowAll": "Show All",
        "EnableAndSearch": "Show results that include all search words",
        "Next": "Next",
        "PreviousLabel": "Previous",
        "NoScriptErrorMsg": "Enable JavaScript support in the browser to view this page.",
        "Print": "Print",
        "Contents": "Contents",
        "Search": "-Search-",
        "Hide": "Hide",
        "Canceled": "Canceled",
        "ShowHide": "Show/Hide",
        "Loading": "Loading...",
        "Logo": "Logo",
        "Logo/Author": "Powered By",
        "JS_alert_LoadXmlFailed": "Failed to load XML file",
        "Searching": "Searching...",
        "Disabled Next": ">>",
        "JS_alert_InitDatabaseFailed": "Failed to initialise database",
        "Cancel": "Cancel",
        "UnknownError": "Unknown error",
        "ResultsFoundText": "%1 result(s) found for %2",
        "Index": "Index",
        "Seperate": "|",
        "SearchPageTitle": "Search Results",
        "TopicsNotFound": "No results found",
        "Glossary": "Glossary",
        "NextLabel": "Next",
        "TableOfContents": "Table of Contents",
        "HideAll": "Hide All",
        "Disabled Prev": "<<",
        "SearchOptions": "Search Options",
        "Back": "Back",
        "Prev": "Previous",
        "JS_alert_InvalidExpression_1": "The search string you typed is not valid.",
        "IndexFilterKewords": "Find Keyword",
        "IeCompatibilityErrorMsg": "This page cannot be viewed in Internet Explorer 8 or earlier version."
    });

    model.publish(rh.consts('KEY_HEADER_DEFAULT_TITLE_COLOR'), "#ffffff");
    model.publish(rh.consts('KEY_HEADER_DEFAULT_BACKGROUND_COLOR'), "#025172");
    model.publish(rh.consts('KEY_LAYOUT_DEFAULT_FONT_FAMILY'), "\"Trebuchet MS\", Arial, sans-serif");

    model.publish(rh.consts('KEY_HEADER_TITLE'), "Connexo MultiSense");
    model.publish(rh.consts('KEY_HEADER_TITLE_COLOR'), "");
    model.publish(rh.consts('KEY_HEADER_BACKGROUND_COLOR'), "");
    model.publish(rh.consts('KEY_HEADER_LOGO_PATH'), "");
    model.publish(rh.consts('KEY_LAYOUT_FONT_FAMILY'), "");
    model.publish(rh.consts('KEY_HEADER_HTML'), "<div class='topic-header'>\
  <div class='logo' onClick='rh._.redirectToLayout()'>\
    <img src='#{logo}' />\
  </div>\
  <div class='nav'>\
    <div class='title' title='#{title}'>\
      <span onClick='rh._.redirectToLayout()'>#{title}</span>\
    </div>\
    <div class='gotohome' title='#{tooltip}' onClick='rh._.redirectToLayout()'>\
      <span>#{label}</span>\
    </div></div>\
  </div>\
<div class='topic-header-shadow'></div>\
");
    model.publish(rh.consts('KEY_HEADER_CSS'), ".topic-header { background-color: #{background-color}; color: #{color}; width: calc(100%); height: 3em; position: fixed; left: 0; top: 0; font-family: #{font-family}; display: table; box-sizing: border-box; }\
.topic-header-shadow { height: 3em; width: 100%; }\
.logo { cursor: pointer; padding: 0.2em; height: calc(100% - 0.4em); text-align: center; display: table-cell; vertical-align: middle; }\
.logo img { max-height: 100%; display: block; }\
.nav { width: 100%; display: table-cell; }\
.title { width: 40%; height: 100%; float: left; line-height: 3em; cursor: pointer; }\
.gotohome { width: 60%; float: left; text-align: right; height: 100%; line-height: 3em; cursor: pointer; }\
.title span, .gotohome span { padding: 0em 1em; white-space: nowrap; text-overflow: ellipsis; overflow: hidden; display: block; }");

})();