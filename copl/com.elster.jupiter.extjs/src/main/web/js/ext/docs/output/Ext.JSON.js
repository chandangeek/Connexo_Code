Ext.data.JsonP.Ext_JSON({"alternateClassNames":[],"aliases":{},"enum":null,"parentMixins":[],"tagname":"class","subclasses":[],"extends":null,"uses":[],"html":"<div><pre class=\"hierarchy\"><h4>Files</h4><div class='dependency'><a href='source/JSON.html#Ext-JSON' target='_blank'>JSON.js</a></div></pre><div class='doc-contents'><p>Modified version of <a href=\"http://www.json.org/js.html\">Douglas Crockford's JSON.js</a> that doesn't\nmess with the Object prototype.</p>\n</div><div class='members'><div class='members-section'><div class='definedBy'>Defined By</div><h3 class='members-title icon-method'>Methods</h3><div class='subsection'><div id='method-decode' class='member first-child not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.JSON'>Ext.JSON</span><br/><a href='source/JSON.html#Ext-JSON-method-decode' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.JSON-method-decode' class='name expandable'>decode</a>( <span class='pre'>json, [safe]</span> ) : <a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a></div><div class='description'><div class='short'>Decodes (parses) a JSON string to an object. ...</div><div class='long'><p>Decodes (parses) a JSON string to an object. If the JSON is invalid, this function throws\na SyntaxError unless the safe option is set.</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>json</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a><div class='sub-desc'><p>The JSON string</p>\n</div></li><li><span class='pre'>safe</span> : <a href=\"#!/api/Boolean\" rel=\"Boolean\" class=\"docClass\">Boolean</a> (optional)<div class='sub-desc'><p>True to return null, false to throw an exception if the JSON is invalid.</p>\n<p>Defaults to: <code>false</code></p></div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a></span><div class='sub-desc'><p>The resulting object</p>\n</div></li></ul></div></div></div><div id='method-encode' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.JSON'>Ext.JSON</span><br/><a href='source/JSON.html#Ext-JSON-method-encode' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.JSON-method-encode' class='name expandable'>encode</a>( <span class='pre'>o</span> ) : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a></div><div class='description'><div class='short'>Encodes an Object, Array or other value. ...</div><div class='long'><p>Encodes an Object, Array or other value.</p>\n\n<p>If the environment's native JSON encoding is not being used (<a href=\"#!/api/Ext-property-USE_NATIVE_JSON\" rel=\"Ext-property-USE_NATIVE_JSON\" class=\"docClass\">Ext.USE_NATIVE_JSON</a> is not set,\nor the environment does not support it), then ExtJS's encoding will be used. This allows the developer\nto add a <code>toJSON</code> method to their classes which need serializing to return a valid JSON representation\nof the object.</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>o</span> : <a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a><div class='sub-desc'><p>The variable to encode</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a></span><div class='sub-desc'><p>The JSON string</p>\n</div></li></ul></div></div></div><div id='method-encodeDate' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.JSON'>Ext.JSON</span><br/><a href='source/JSON.html#Ext-JSON-method-encodeDate' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.JSON-method-encodeDate' class='name expandable'>encodeDate</a>( <span class='pre'>d</span> ) : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a></div><div class='description'><div class='short'>Encodes a Date. ...</div><div class='long'><p>Encodes a Date. This returns the actual string which is inserted into the JSON string as the literal\nexpression. <strong>The returned value includes enclosing double quotation marks.</strong></p>\n\n<p>The default return format is <code>\"yyyy-mm-ddThh:mm:ss\"</code>.</p>\n\n<p>To override this:</p>\n\n<pre><code><a href=\"#!/api/Ext.JSON-method-encodeDate\" rel=\"Ext.JSON-method-encodeDate\" class=\"docClass\">Ext.JSON.encodeDate</a> = function(d) {\n    return <a href=\"#!/api/Ext.Date-method-format\" rel=\"Ext.Date-method-format\" class=\"docClass\">Ext.Date.format</a>(d, '\"Y-m-d\"');\n};\n</code></pre>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>d</span> : <a href=\"#!/api/Date\" rel=\"Date\" class=\"docClass\">Date</a><div class='sub-desc'><p>The Date to encode</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a></span><div class='sub-desc'><p>The string literal to use in a JSON string.</p>\n</div></li></ul></div></div></div><div id='method-encodeString' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.JSON'>Ext.JSON</span><br/><a href='source/JSON.html#Ext-JSON-method-encodeString' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.JSON-method-encodeString' class='name expandable'>encodeString</a>( <span class='pre'>s</span> ) : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a></div><div class='description'><div class='short'>Encodes a String. ...</div><div class='long'><p>Encodes a String. This returns the actual string which is inserted into the JSON string as the literal\nexpression. <strong>The returned value includes enclosing double quotation marks.</strong></p>\n\n<p>To override this:</p>\n\n<pre><code><a href=\"#!/api/Ext.JSON-method-encodeString\" rel=\"Ext.JSON-method-encodeString\" class=\"docClass\">Ext.JSON.encodeString</a> = function(s) {\n    return 'Foo' + s;\n};\n</code></pre>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>s</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a><div class='sub-desc'><p>The String to encode</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a></span><div class='sub-desc'><p>The string literal to use in a JSON string.</p>\n</div></li></ul></div></div></div><div id='method-encodeValue' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Ext.JSON'>Ext.JSON</span><br/><a href='source/JSON.html#Ext-JSON-method-encodeValue' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Ext.JSON-method-encodeValue' class='name expandable'>encodeValue</a>( <span class='pre'>o</span> ) : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a></div><div class='description'><div class='short'>The function which encode uses to encode all javascript values to their JSON representations\nwhen Ext.USE_NATIVE_JSON...</div><div class='long'><p>The function which <a href=\"#!/api/Ext.JSON-method-encode\" rel=\"Ext.JSON-method-encode\" class=\"docClass\">encode</a> uses to encode all javascript values to their JSON representations\nwhen <a href=\"#!/api/Ext-property-USE_NATIVE_JSON\" rel=\"Ext-property-USE_NATIVE_JSON\" class=\"docClass\">Ext.USE_NATIVE_JSON</a> is <code>false</code>.</p>\n\n<p>This is made public so that it can be replaced with a custom implementation.</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>o</span> : <a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a><div class='sub-desc'><p>Any javascript value to be converted to its JSON representation</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a></span><div class='sub-desc'><p>The JSON representation of the passed value.</p>\n</div></li></ul></div></div></div></div></div></div></div>","superclasses":[],"meta":{},"requires":[],"html_meta":{},"statics":{"property":[],"cfg":[],"css_var":[],"method":[],"event":[],"css_mixin":[]},"files":[{"href":"JSON.html#Ext-JSON","filename":"JSON.js"}],"linenr":4,"members":{"property":[],"cfg":[],"css_var":[],"method":[{"tagname":"method","owner":"Ext.JSON","meta":{},"name":"decode","id":"method-decode"},{"tagname":"method","owner":"Ext.JSON","meta":{},"name":"encode","id":"method-encode"},{"tagname":"method","owner":"Ext.JSON","meta":{},"name":"encodeDate","id":"method-encodeDate"},{"tagname":"method","owner":"Ext.JSON","meta":{},"name":"encodeString","id":"method-encodeString"},{"tagname":"method","owner":"Ext.JSON","meta":{},"name":"encodeValue","id":"method-encodeValue"}],"event":[],"css_mixin":[]},"inheritable":null,"private":null,"component":false,"name":"Ext.JSON","singleton":true,"override":null,"inheritdoc":null,"id":"class-Ext.JSON","mixins":[],"mixedInto":[]});