Ext.data.JsonP.Ext_dom_Query({"tagname":"class","name":"Ext.dom.Query","extends":null,"mixins":[],"alternateClassNames":["Ext.DomQuery","Ext.core.DomQuery"],"aliases":{},"singleton":true,"requires":[],"uses":[],"enum":null,"override":null,"inheritable":null,"inheritdoc":null,"meta":{},"private":null,"id":"class-Ext.dom.Query","members":{"cfg":[],"property":[{"name":"matchers","tagname":"property","owner":"Ext.dom.Query","meta":{},"id":"property-matchers"},{"name":"operators","tagname":"property","owner":"Ext.dom.Query","meta":{},"id":"property-operators"},{"name":"pseudos","tagname":"property","owner":"Ext.dom.Query","meta":{},"id":"property-pseudos"}],"method":[{"name":"compile","tagname":"method","owner":"Ext.dom.Query","meta":{},"id":"method-compile"},{"name":"filter","tagname":"method","owner":"Ext.dom.Query","meta":{},"id":"method-filter"},{"name":"is","tagname":"method","owner":"Ext.dom.Query","meta":{},"id":"method-is"},{"name":"jsSelect","tagname":"method","owner":"Ext.dom.Query","meta":{},"id":"method-jsSelect"},{"name":"select","tagname":"method","owner":"Ext.dom.Query","meta":{},"id":"method-select"},{"name":"selectNode","tagname":"method","owner":"Ext.dom.Query","meta":{},"id":"method-selectNode"},{"name":"selectNumber","tagname":"method","owner":"Ext.dom.Query","meta":{},"id":"method-selectNumber"},{"name":"selectValue","tagname":"method","owner":"Ext.dom.Query","meta":{},"id":"method-selectValue"}],"event":[],"css_var":[],"css_mixin":[]},"linenr":12,"files":[{"filename":"Query.js","href":"Query.html#Ext-dom-Query"}],"html_meta":{},"statics":{"cfg":[],"property":[],"method":[],"event":[],"css_var":[],"css_mixin":[]},"component":false,"superclasses":[],"subclasses":[],"mixedInto":[],"parentMixins":[],"html":"<div><pre class=\"hierarchy\"><h4>Alternate names</h4><div class='alternate-class-name'>Ext.DomQuery</div><div class='alternate-class-name'>Ext.core.DomQuery</div><h4>Files</h4><div class='dependency'><a href='source/Query.html#Ext-dom-Query' target='_blank'>Query.js</a></div></pre><div class='doc-contents'><p>Provides high performance selector/xpath processing by compiling queries into reusable functions. New pseudo classes\nand matchers can be plugged. It works on HTML and XML documents (if a content node is passed in).</p>\n\n<p>DomQuery supports most of the <a href=\"http://www.w3.org/TR/2005/WD-css3-selectors-20051215/#selectors\">CSS3 selectors spec</a>, along with some custom selectors and basic XPath.</p>\n\n<p>All selectors, attribute filters and pseudos below can be combined infinitely in any order. For example\n<code>div.foo:nth-child(odd)[@foo=bar].bar:first</code> would be a perfectly valid selector. Node filters are processed\nin the order in which they appear, which allows you to optimize your queries for your document structure.</p>\n\n<h2>Simple Selectors</h2>\n\n<p>For performance reasons, some query methods accept selectors that are termed as <strong>simple selectors</strong>. A simple\nselector is a selector that does not include contextual information about any parent/sibling elements.</p>\n\n<p>Some examples of valid simple selectors:</p>\n\n<pre><code>var simple = '.foo'; // Only asking for the class name on the element\nvar simple = 'div.bar'; // Only asking for the tag/class name on the element\nvar simple = '[href];' // Asking for an attribute on the element.\nvar simple = ':not(.foo)'; // Only asking for the non-matches against the class name\nvar simple = 'span:first-child'; // Doesn't require any contextual information about the parent node\n</code></pre>\n\n<p>Simple examples of invalid simple selectors:</p>\n\n<pre><code>var notSimple = 'div.foo div.bar'; // Requires matching a parent node by class name\nvar notSimple = 'span + div'; //  Requires matching a sibling by tag name\n</code></pre>\n\n<h2>Element Selectors:</h2>\n\n<ul>\n<li><strong><code>*</code></strong> any element</li>\n<li><strong><code>E</code></strong> an element with the tag E</li>\n<li><strong><code>E F</code></strong> All descendent elements of E that have the tag F</li>\n<li><strong><code>E &gt; F</code></strong> or <strong>E/F</strong> all direct children elements of E that have the tag F</li>\n<li><strong><code>E + F</code></strong> all elements with the tag F that are immediately preceded by an element with the tag E</li>\n<li><strong><code>E ~ F</code></strong> all elements with the tag F that are preceded by a sibling element with the tag E</li>\n</ul>\n\n\n<h2>Attribute Selectors:</h2>\n\n<p>The use of <code>@</code> and quotes are optional. For example, <code>div[@foo='bar']</code> is also a valid attribute selector.</p>\n\n<ul>\n<li><strong><code>E[foo]</code></strong> has an attribute \"foo\"</li>\n<li><strong><code>E[foo=bar]</code></strong> has an attribute \"foo\" that equals \"bar\"</li>\n<li><strong><code>E[foo^=bar]</code></strong> has an attribute \"foo\" that starts with \"bar\"</li>\n<li><strong><code>E[foo$=bar]</code></strong> has an attribute \"foo\" that ends with \"bar\"</li>\n<li><strong><code>E[foo*=bar]</code></strong> has an attribute \"foo\" that contains the substring \"bar\"</li>\n<li><strong><code>E[foo%=2]</code></strong> has an attribute \"foo\" that is evenly divisible by 2</li>\n<li><strong><code>E[foo!=bar]</code></strong> attribute \"foo\" does not equal \"bar\"</li>\n</ul>\n\n\n<h2>Pseudo Classes:</h2>\n\n<ul>\n<li><strong><code>E:first-child</code></strong> E is the first child of its parent</li>\n<li><strong><code>E:last-child</code></strong> E is the last child of its parent</li>\n<li><strong><code>E:nth-child(_n_)</code></strong> E is the _n_th child of its parent (1 based as per the spec)</li>\n<li><strong><code>E:nth-child(odd)</code></strong> E is an odd child of its parent</li>\n<li><strong><code>E:nth-child(even)</code></strong> E is an even child of its parent</li>\n<li><strong><code>E:only-child</code></strong> E is the only child of its parent</li>\n<li><strong><code>E:checked</code></strong> E is an element that is has a checked attribute that is true (e.g. a radio or checkbox)</li>\n<li><strong><code>E:first</code></strong> the first E in the resultset</li>\n<li><strong><code>E:last</code></strong> the last E in the resultset</li>\n<li><strong><code>E:nth(_n_)</code></strong> the _n_th E in the resultset (1 based)</li>\n<li><strong><code>E:odd</code></strong> shortcut for :nth-child(odd)</li>\n<li><strong><code>E:even</code></strong> shortcut for :nth-child(even)</li>\n<li><strong><code>E:contains(foo)</code></strong> E's innerHTML contains the substring \"foo\"</li>\n<li><strong><code>E:nodeValue(foo)</code></strong> E contains a textNode with a nodeValue that equals \"foo\"</li>\n<li><strong><code>E:not(S)</code></strong> an E element that does not match simple selector S</li>\n<li><strong><code>E:has(S)</code></strong> an E element that has a descendent that matches simple selector S</li>\n<li><strong><code>E:next(S)</code></strong> an E element whose next sibling matches simple selector S</li>\n<li><strong><code>E:prev(S)</code></strong> an E element whose previous sibling matches simple selector S</li>\n<li><strong><code>E:any(S1|S2|S2)</code></strong> an E element which matches any of the simple selectors S1, S2 or S3</li>\n<li><strong><code>E:visible(true)</code></strong> an E element which is deeply visible according to <a href=\"#!/api/Ext.dom.Element-method-isVisible\" rel=\"Ext.dom.Element-method-isVisible\" class=\"docClass\">Ext.dom.Element.isVisible</a></li>\n</ul>\n\n\n<h2>CSS Value Selectors:</h2>\n\n<ul>\n<li><strong><code>E{display=none}</code></strong> css value \"display\" that equals \"none\"</li>\n<li><strong><code>E{display^=none}</code></strong> css value \"display\" that starts with \"none\"</li>\n<li><strong><code>E{display$=none}</code></strong> css value \"display\" that ends with \"none\"</li>\n<li><strong><code>E{display*=none}</code></strong> css value \"display\" that contains the substring \"none\"</li>\n<li><strong><code>E{display%=2}</code></strong> css value \"display\" that is evenly divisible by 2</li>\n<li><strong><code>E{display!=none}</code></strong> css value \"display\" that does not equal \"none\"</li>\n</ul>\n\n\n<h2>XML Namespaces:</h2>\n\n<ul>\n<li><strong><code>ns|E</code></strong> an element with tag E and namespace prefix ns</li>\n</ul>\n\n</div><div class='members'><div class='members-section'><div class='definedBy'>Defined By</div><h3 class='members-title icon-property'>Properties</h3><div class='subsection'><div id='property-matchers' class='member first-child not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.dom.Query'>Ext.dom.Query</span><br/><a href='source/Query.html#Ext-dom-Query-property-matchers' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.dom.Query-property-matchers' class='name expandable'>matchers</a><span> : <a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a></span></div><div class='description'><div class='short'>Collection of matching regular expressions and code snippets. ...</div><div class='long'><p>Collection of matching regular expressions and code snippets.\nEach capture group within <code>()</code> will be replace the <code>{}</code> in the select\nstatement as specified by their index.</p>\n</div></div></div><div id='property-operators' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.dom.Query'>Ext.dom.Query</span><br/><a href='source/Query.html#Ext-dom-Query-property-operators' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.dom.Query-property-operators' class='name expandable'>operators</a><span> : <a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a></span></div><div class='description'><div class='short'>Collection of operator comparison functions. ...</div><div class='long'><p>Collection of operator comparison functions.\nThe default operators are <code>=</code>, <code>!=</code>, <code>^=</code>, <code>$=</code>, <code>*=</code>, <code>%=</code>, <code>|=</code> and <code>~=</code>.</p>\n\n<p>New operators can be added as long as the match the format <em>c</em><code>=</code> where <em>c</em>\nis any character other than space, <code>&gt;</code>, or <code>&lt;</code>.</p>\n\n<p>Operator functions are passed the following parameters:</p>\n\n<ul>\n<li><code>propValue</code> : The property value to test.</li>\n<li><code>compareTo</code> : The value to compare to.</li>\n</ul>\n\n</div></div></div><div id='property-pseudos' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.dom.Query'>Ext.dom.Query</span><br/><a href='source/Query.html#Ext-dom-Query-property-pseudos' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.dom.Query-property-pseudos' class='name expandable'>pseudos</a><span> : <a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a></span></div><div class='description'><div class='short'>Object hash of \"pseudo class\" filter functions which are used when filtering selections. ...</div><div class='long'><p>Object hash of \"pseudo class\" filter functions which are used when filtering selections.\nEach function is passed two parameters:</p>\n\n<ul>\n<li><p><strong>c</strong> : Array\n  An Array of DOM elements to filter.</p></li>\n<li><p><strong>v</strong> : String\n  The argument (if any) supplied in the selector.</p></li>\n</ul>\n\n\n<p>A filter function returns an Array of DOM elements which conform to the pseudo class.\nIn addition to the provided pseudo classes listed above such as <code>first-child</code> and <code>nth-child</code>,\ndevelopers may add additional, custom psuedo class filters to select elements according to application-specific requirements.</p>\n\n<p>For example, to filter <code>a</code> elements to only return links to <strong>external</strong> resources:</p>\n\n<pre><code>Ext.DomQuery.pseudos.external = function(c, v) {\n    var r = [], ri = -1;\n    for(var i = 0, ci; ci = c[i]; i++) {\n        // Include in result set only if it's a link to an external resource\n        if (ci.hostname != location.hostname) {\n            r[++ri] = ci;\n        }\n    }\n    return r;\n};\n</code></pre>\n\n<p>Then external links could be gathered with the following statement:</p>\n\n<pre><code>var externalLinks = <a href=\"#!/api/Ext-method-select\" rel=\"Ext-method-select\" class=\"docClass\">Ext.select</a>(\"a:external\");\n</code></pre>\n</div></div></div></div></div><div class='members-section'><div class='definedBy'>Defined By</div><h3 class='members-title icon-method'>Methods</h3><div class='subsection'><div id='method-compile' class='member first-child not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.dom.Query'>Ext.dom.Query</span><br/><a href='source/Query.html#Ext-dom-Query-method-compile' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.dom.Query-method-compile' class='name expandable'>compile</a>( <span class='pre'>selector, [type]</span> ) : <a href=\"#!/api/Function\" rel=\"Function\" class=\"docClass\">Function</a></div><div class='description'><div class='short'>Compiles a selector/xpath query into a reusable function. ...</div><div class='long'><p>Compiles a selector/xpath query into a reusable function. The returned function\ntakes one parameter \"root\" (optional), which is the context node from where the query should start.</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>selector</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a><div class='sub-desc'><p>The selector/xpath query</p>\n</div></li><li><span class='pre'>type</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a> (optional)<div class='sub-desc'><p>Either \"select\" or \"simple\" for a simple selector match</p>\n<p>Defaults to: <code>&quot;select&quot;</code></p></div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/Function\" rel=\"Function\" class=\"docClass\">Function</a></span><div class='sub-desc'>\n</div></li></ul></div></div></div><div id='method-filter' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.dom.Query'>Ext.dom.Query</span><br/><a href='source/Query.html#Ext-dom-Query-method-filter' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.dom.Query-method-filter' class='name expandable'>filter</a>( <span class='pre'>el, selector, nonMatches</span> ) : HTMLElement[]</div><div class='description'><div class='short'>Filters an array of elements to only include matches of a simple selector ...</div><div class='long'><p>Filters an array of elements to only include matches of a simple selector</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>el</span> : HTMLElement[]<div class='sub-desc'><p>An array of elements to filter</p>\n</div></li><li><span class='pre'>selector</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a><div class='sub-desc'><p>The simple selector to test</p>\n</div></li><li><span class='pre'>nonMatches</span> : <a href=\"#!/api/Boolean\" rel=\"Boolean\" class=\"docClass\">Boolean</a><div class='sub-desc'><p>If true, it returns the elements that DON'T match the selector instead of the\nones that match</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'>HTMLElement[]</span><div class='sub-desc'><p>An Array of DOM elements which match the selector. If there are no matches, and empty\nArray is returned.</p>\n</div></li></ul></div></div></div><div id='method-is' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.dom.Query'>Ext.dom.Query</span><br/><a href='source/Query.html#Ext-dom-Query-method-is' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.dom.Query-method-is' class='name expandable'>is</a>( <span class='pre'>el, selector</span> ) : <a href=\"#!/api/Boolean\" rel=\"Boolean\" class=\"docClass\">Boolean</a></div><div class='description'><div class='short'>Returns true if the passed element(s) match the passed simple selector ...</div><div class='long'><p>Returns true if the passed element(s) match the passed simple selector</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>el</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a>/HTMLElement/HTMLElement[]<div class='sub-desc'><p>An element id, element or array of elements</p>\n</div></li><li><span class='pre'>selector</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a><div class='sub-desc'><p>The simple selector to test</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/Boolean\" rel=\"Boolean\" class=\"docClass\">Boolean</a></span><div class='sub-desc'>\n</div></li></ul></div></div></div><div id='method-jsSelect' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.dom.Query'>Ext.dom.Query</span><br/><a href='source/Query.html#Ext-dom-Query-method-jsSelect' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.dom.Query-method-jsSelect' class='name expandable'>jsSelect</a>( <span class='pre'>selector, [root]</span> ) : HTMLElement[]</div><div class='description'><div class='short'>Selects an array of DOM nodes using JavaScript-only implementation. ...</div><div class='long'><p>Selects an array of DOM nodes using JavaScript-only implementation.</p>\n\n<p>Use <a href=\"#!/api/Ext.dom.Query-method-select\" rel=\"Ext.dom.Query-method-select\" class=\"docClass\">select</a> to take advantage of browsers built-in support for CSS selectors.</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>selector</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a><div class='sub-desc'><p>The selector/xpath query (can be a comma separated list of selectors)</p>\n</div></li><li><span class='pre'>root</span> : HTMLElement/<a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a> (optional)<div class='sub-desc'><p>The start of the query.</p>\n<p>Defaults to: <code>document</code></p></div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'>HTMLElement[]</span><div class='sub-desc'><p>An Array of DOM elements which match the selector. If there are\nno matches, and empty Array is returned.</p>\n</div></li></ul></div></div></div><div id='method-select' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.dom.Query'>Ext.dom.Query</span><br/><a href='source/Query.html#Ext-dom-Query-method-select' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.dom.Query-method-select' class='name expandable'>select</a>( <span class='pre'>path, [root], [type], [single]</span> ) : HTMLElement[]</div><div class='description'><div class='short'>Selects an array of DOM nodes by CSS/XPath selector. ...</div><div class='long'><p>Selects an array of DOM nodes by CSS/XPath selector.</p>\n\n<p>Uses <a href=\"https://developer.mozilla.org/en/DOM/document.querySelectorAll\">document.querySelectorAll</a> if browser supports that, otherwise falls back to\n<a href=\"#!/api/Ext.dom.Query-method-jsSelect\" rel=\"Ext.dom.Query-method-jsSelect\" class=\"docClass\">jsSelect</a> to do the work.</p>\n\n<p>Aliased as <a href=\"#!/api/Ext-method-query\" rel=\"Ext-method-query\" class=\"docClass\">Ext.query</a>.</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>path</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a><div class='sub-desc'><p>The selector/xpath query</p>\n</div></li><li><span class='pre'>root</span> : HTMLElement (optional)<div class='sub-desc'><p>The start of the query.</p>\n<p>Defaults to: <code>document</code></p></div></li><li><span class='pre'>type</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a> (optional)<div class='sub-desc'><p>Either \"select\" or \"simple\" for a simple selector match (only valid when\nused when the call is deferred to the jsSelect method)</p>\n<p>Defaults to: <code>&quot;select&quot;</code></p></div></li><li><span class='pre'>single</span> : <a href=\"#!/api/Boolean\" rel=\"Boolean\" class=\"docClass\">Boolean</a> (optional)<div class='sub-desc'><p>Pass <code>true</code> to select only the first matching node using <code>document.querySelector</code> (where available)</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'>HTMLElement[]</span><div class='sub-desc'><p>An array of DOM elements (not a NodeList as returned by <code>querySelectorAll</code>).</p>\n</div></li></ul></div></div></div><div id='method-selectNode' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.dom.Query'>Ext.dom.Query</span><br/><a href='source/Query.html#Ext-dom-Query-method-selectNode' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.dom.Query-method-selectNode' class='name expandable'>selectNode</a>( <span class='pre'>selector, [root]</span> ) : HTMLElement</div><div class='description'><div class='short'>Selects a single element. ...</div><div class='long'><p>Selects a single element.</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>selector</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a><div class='sub-desc'><p>The selector/xpath query</p>\n</div></li><li><span class='pre'>root</span> : HTMLElement (optional)<div class='sub-desc'><p>The start of the query.</p>\n<p>Defaults to: <code>document</code></p></div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'>HTMLElement</span><div class='sub-desc'><p>The DOM element which matched the selector.</p>\n</div></li></ul></div></div></div><div id='method-selectNumber' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.dom.Query'>Ext.dom.Query</span><br/><a href='source/Query.html#Ext-dom-Query-method-selectNumber' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.dom.Query-method-selectNumber' class='name expandable'>selectNumber</a>( <span class='pre'>selector, [root], [defaultValue]</span> ) : <a href=\"#!/api/Number\" rel=\"Number\" class=\"docClass\">Number</a></div><div class='description'><div class='short'>Selects the value of a node, parsing integers and floats. ...</div><div class='long'><p>Selects the value of a node, parsing integers and floats.\nReturns the defaultValue, or 0 if none is specified.</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>selector</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a><div class='sub-desc'><p>The selector/xpath query</p>\n</div></li><li><span class='pre'>root</span> : HTMLElement (optional)<div class='sub-desc'><p>The start of the query.</p>\n<p>Defaults to: <code>document</code></p></div></li><li><span class='pre'>defaultValue</span> : <a href=\"#!/api/Number\" rel=\"Number\" class=\"docClass\">Number</a> (optional)<div class='sub-desc'><p>When specified, this is return as empty value.</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/Number\" rel=\"Number\" class=\"docClass\">Number</a></span><div class='sub-desc'>\n</div></li></ul></div></div></div><div id='method-selectValue' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.dom.Query'>Ext.dom.Query</span><br/><a href='source/Query.html#Ext-dom-Query-method-selectValue' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.dom.Query-method-selectValue' class='name expandable'>selectValue</a>( <span class='pre'>selector, [root], [defaultValue]</span> ) : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a></div><div class='description'><div class='short'>Selects the value of a node, optionally replacing null with the defaultValue. ...</div><div class='long'><p>Selects the value of a node, optionally replacing null with the defaultValue.</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>selector</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a><div class='sub-desc'><p>The selector/xpath query</p>\n</div></li><li><span class='pre'>root</span> : HTMLElement (optional)<div class='sub-desc'><p>The start of the query.</p>\n<p>Defaults to: <code>document</code></p></div></li><li><span class='pre'>defaultValue</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a> (optional)<div class='sub-desc'><p>When specified, this is return as empty value.</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a></span><div class='sub-desc'>\n</div></li></ul></div></div></div></div></div></div></div>"});