Ext.data.JsonP.Function({"tagname":"class","name":"Function","extends":null,"mixins":[],"alternateClassNames":[],"aliases":{},"singleton":false,"requires":[],"uses":[],"enum":null,"override":null,"inheritable":null,"inheritdoc":null,"meta":{},"private":null,"id":"class-Function","members":{"cfg":[],"property":[{"name":"length","tagname":"property","owner":"Function","meta":{},"id":"property-length"}],"method":[{"name":"constructor","tagname":"method","owner":"Function","meta":{},"id":"method-constructor"},{"name":"apply","tagname":"method","owner":"Function","meta":{},"id":"method-apply"},{"name":"bind","tagname":"method","owner":"Function","meta":{},"id":"method-bind"},{"name":"call","tagname":"method","owner":"Function","meta":{},"id":"method-call"},{"name":"toString","tagname":"method","owner":"Function","meta":{},"id":"method-toString"}],"event":[],"css_var":[],"css_mixin":[]},"linenr":1,"files":[{"filename":"Function.js","href":"Function.html#Function"}],"html_meta":{},"statics":{"cfg":[],"property":[],"method":[],"event":[],"css_var":[],"css_mixin":[]},"component":false,"superclasses":[],"subclasses":[],"mixedInto":[],"parentMixins":[],"html":"<div><pre class=\"hierarchy\"><h4>Files</h4><div class='dependency'><a href='source/Function.html#Function' target='_blank'>Function.js</a></div></pre><div class='doc-contents'><p>Every function in JavaScript is actually a <code>Function</code> object.</p>\n\n<p><code>Function</code> objects created with the <code>Function</code> constructor are parsed when the\nfunction is created. This is less efficient than declaring a function and\ncalling it within your code, because functions declared with the function\nstatement are parsed with the rest of the code.</p>\n\n<p>All arguments passed to the function are treated as the names of the\nidentifiers of the parameters in the function to be created, in the order in\nwhich they are passed.</p>\n\n<p>Invoking the <code>Function</code> constructor as a function (without using the <code>new</code>\noperator) has the same effect as invoking it as a constructor.</p>\n\n<h1>Specifying arguments with the <code>Function</code> constructor</h1>\n\n<p>The following code creates a <code>Function</code> object that takes two arguments.</p>\n\n<pre><code>// Example can be run directly in your JavaScript console\n\n// Create a function that takes two arguments and returns the sum of those\narguments\nvar adder = new Function(\"a\", \"b\", \"return a + b\");\n\n// Call the function\nadder(2, 6);\n// &gt; 8\n</code></pre>\n\n<p>The arguments \"a\" and \"b\" are formal argument names that are used in the\nfunction body, \"return a + b\".</p>\n\n<div class=\"notice\">\nDocumentation for this class comes from <a href=\"https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Function\">MDN</a>\nand is available under <a href=\"http://creativecommons.org/licenses/by-sa/2.0/\">Creative Commons: Attribution-Sharealike license</a>.\n</div>\n\n</div><div class='members'><div class='members-section'><div class='definedBy'>Defined By</div><h3 class='members-title icon-property'>Properties</h3><div class='subsection'><div id='property-length' class='member first-child not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Function'>Function</span><br/><a href='source/Function.html#Function-property-length' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Function-property-length' class='name not-expandable'>length</a><span> : <a href=\"#!/api/Number\" rel=\"Number\" class=\"docClass\">Number</a></span></div><div class='description'><div class='short'><p>Specifies the number of arguments expected by the function.</p>\n</div><div class='long'><p>Specifies the number of arguments expected by the function.</p>\n</div></div></div></div></div><div class='members-section'><div class='definedBy'>Defined By</div><h3 class='members-title icon-method'>Methods</h3><div class='subsection'><div id='method-constructor' class='member first-child not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Function'>Function</span><br/><a href='source/Function.html#Function-method-constructor' target='_blank' class='view-source'>view source</a></div><strong class='new-keyword'>new</strong><a href='#!/api/Function-method-constructor' class='name expandable'>Function</a>( <span class='pre'>args, functionBody</span> ) : <a href=\"#!/api/Function\" rel=\"Function\" class=\"docClass\">Function</a></div><div class='description'><div class='short'>Creates new Function object. ...</div><div class='long'><p>Creates new Function object.</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>args</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a>...<div class='sub-desc'><p>Names to be used by the function as formal argument names. Each must be a\nstring that corresponds to a valid JavaScript identifier or a list of such\nstrings separated with a comma; for example \"<code>x</code>\", \"<code>theValue</code>\", or \"<code>a,b</code>\".</p>\n</div></li><li><span class='pre'>functionBody</span> : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a><div class='sub-desc'><p>A string containing the JavaScript statements comprising the function\ndefinition.</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/Function\" rel=\"Function\" class=\"docClass\">Function</a></span><div class='sub-desc'>\n</div></li></ul></div></div></div><div id='method-apply' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Function'>Function</span><br/><a href='source/Function.html#Function-method-apply' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Function-method-apply' class='name expandable'>apply</a>( <span class='pre'>thisArg, argsArray</span> ) : <a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a></div><div class='description'><div class='short'>Applies the method of another object in the context of a different object (the\ncalling object); arguments can be pass...</div><div class='long'><p>Applies the method of another object in the context of a different object (the\ncalling object); arguments can be passed as an Array object.</p>\n\n<p>You can assign a different this object when calling an existing function. <code>this</code> refers to the\ncurrent object, the calling object. With <code>apply</code>, you can write a method once and then inherit it\nin another object, without having to rewrite the method for the new object.</p>\n\n<p><code>apply</code> is very similar to call, except for the type of arguments it supports. You can use an\narguments array instead of a named set of parameters. With apply, you can use an array literal, for\nexample, <code>fun.apply(this, ['eat', 'bananas'])</code>, or an Array object, for example, <code>fun.apply(this,\nnew Array('eat', 'bananas'))</code>.</p>\n\n<p>You can also use arguments for the <code>argsArray</code> parameter. <code>arguments</code> is a local variable of a\nfunction. It can be used for all unspecified arguments of the called object. Thus, you do not have\nto know the arguments of the called object when you use the <code>apply</code> method. You can use arguments\nto pass all the arguments to the called object. The called object is then responsible for handling\nthe arguments.</p>\n\n<p>Since ECMAScript 5th Edition you can also use any kind of object which is array like, so in\npractice this means it's going to have a property length and integer properties in the range\n<code>[0...length)</code>. As an example you can now use a NodeList or a own custom object like <code>{'length': 2,\n'0': 'eat', '1': 'bananas'}</code>.</p>\n\n<p>You can use <code>apply</code> to chain constructors for an object, similar to Java. In the following example,\nthe constructor for the <code>Product</code> object is defined with two parameters, <code>name</code> and <code>value</code>. Two\nother functions <code>Food</code> and <code>Toy</code> invoke <code>Product</code> passing <code>this</code> and <code>arguments</code>. <code>Product</code>\ninitializes the properties <code>name</code> and <code>price</code>, both specialized functions define the category. In\nthis example, the <code>arguments</code> object is fully passed to the product constructor and corresponds to\nthe two defined parameters.</p>\n\n<pre><code>function Product(name, price) {\n    this.name = name;\n    this.price = price;\n\n    if (price &lt; 0)\n        throw RangeError('Cannot create product \"' + name + '\" with a negative price');\n    return this;\n}\n\nfunction Food(name, price) {\n    Product.apply(this, arguments);\n    this.category = 'food';\n}\nFood.prototype = new Product();\n\nfunction Toy(name, price) {\n    Product.apply(this, arguments);\n    this.category = 'toy';\n}\nToy.prototype = new Product();\n\nvar cheese = new Food('feta', 5);\nvar fun = new Toy('robot', 40);\n</code></pre>\n\n<p>Clever usage of <code>apply</code> allows you to use built-ins functions for some tasks that otherwise\nprobably would have been written by looping over the array values. As an example here we are going\nto use Math.max/Math.min to find out the maximum/minimum value in an array.</p>\n\n<pre><code>//min/max number in an array\nvar numbers = [5, 6, 2, 3, 7];\n\n//using Math.min/Math.max apply\nvar max = Math.max.apply(null, numbers); // This about equal to Math.max(numbers[0], ...) or\n// Math.max(5, 6, ..)\nvar min = Math.min.apply(null, numbers);\n\n//vs. simple loop based algorithm\nmax = -Infinity, min = +Infinity;\n\nfor (var i = 0; i &lt; numbers.length; i++) {\nif (numbers[i] &gt; max)\n    max = numbers[i];\nif (numbers[i] &lt; min)\n    min = numbers[i];\n}\n</code></pre>\n\n<p>But beware: in using <code>apply</code> this way, you run the risk of exceeding the JavaScript engine's\nargument length limit. The consequences of applying a function with too many arguments (think more\nthan tens of thousands of arguments) vary across engines, because the limit (indeed even the nature\nof any excessively-large-stack behavior) is unspecified. Some engines will throw an exception. More\nperniciously, others will arbitrarily limit the number of arguments actually passed to the applied\nfunction. (To illustrate this latter case: if such an engine had a limit of four arguments [actual\nlimits are of course significantly higher], it would be as if the arguments 5, 6, 2, 3 had been\npassed to apply in the examples above, rather than the full array.)  If your value array might grow\ninto the tens of thousands, use a hybrid strategy: apply your function to chunks of the array at a\ntime:</p>\n\n<pre><code>function minOfArray(arr)\n{\n    var min = Infinity;\n    var QUANTUM = 32768;\n    for (var i = 0, len = arr.length; i &lt; len; i += QUANTUM)\n    {\n        var submin = Math.min.apply(null, numbers.slice(i, Math.min(i + QUANTUM, len)));\n        min = Math.min(submin, min);\n    }\nreturn min;\n}\n\nvar min = minOfArray([5, 6, 2, 3, 7]);\n</code></pre>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>thisArg</span> : <a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a><div class='sub-desc'><p>The value of this provided for the call to fun. Note that this may not be\nthe actual value seen by the method: if the method is a function in non-strict mode code, null and\nundefined will be replaced with the global object, and primitive values will be boxed.</p>\n</div></li><li><span class='pre'>argsArray</span> : <a href=\"#!/api/Array\" rel=\"Array\" class=\"docClass\">Array</a><div class='sub-desc'><p>An array like object, specifying the arguments with which fun should be\ncalled, or null or undefined if no arguments should be provided to the function.</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a></span><div class='sub-desc'><p>Returns what the function returns.</p>\n</div></li></ul></div></div></div><div id='method-bind' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Function'>Function</span><br/><a href='source/Function.html#Function-method-bind' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Function-method-bind' class='name expandable'>bind</a>( <span class='pre'>thisArg, [args]</span> ) : <a href=\"#!/api/Function\" rel=\"Function\" class=\"docClass\">Function</a></div><div class='description'><div class='short'>Creates a new function that, when called, has its this keyword set\nto the provided value, with a given sequence of ar...</div><div class='long'><p>Creates a new function that, when called, has its <code>this</code> keyword set\nto the provided value, with a given sequence of arguments preceding\nany provided when the new function was called.</p>\n\n<p>The <code>bind()</code> function creates a new function (a bound function) with\nthe same function body (internal Call attribute in ECMAScript 5\nterms) as the function it is being called on (the bound function's\ntarget function) with the <code>this</code> value bound to the first argument of\n<code>bind()</code>, which cannot be overridden. <code>bind()</code> also accepts leading\ndefault arguments to provide to the target function when the bound\nfunction is called. A bound function may also be constructed using\nthe new operator: doing so acts as though the target function had\ninstead been constructed. The provided <code>this</code> value is ignored, while\nprepended arguments are provided to the emulated function.</p>\n\n<h2>Creating a bound function</h2>\n\n<p>The simplest use of <code>bind()</code> is to make a function that, no matter\nhow it is called, is called with a particular <code>this</code> value. A common\nmistake for new JavaScript programmers is to extract a method from\nan object, then to later call that function and expect it to use\nthe original object as its <code>this</code> (e.g. by using that method in\ncallback-based code). Without special care, however, the original\nobject is usually lost. Creating a bound function from the\nfunction, using the original object, neatly solves <code>this</code> problem:</p>\n\n<pre><code>var x = 9;\nvar module = {\n  x: 81,\n  getX: function() { return this.x; }\n};\n\nmodule.getX(); // 81\n\nvar getX = module.getX;\ngetX(); // 9, because in this case, \"this\" refers to the global object\n\n// create a new function with 'this' bound to module\nvar boundGetX = getX.bind(module);\nboundGetX(); // 81\n</code></pre>\n\n<h2>Partial functions</h2>\n\n<p>The next simplest use of <code>bind()</code> is to make a function with\npre-specified initial arguments. These arguments (if any) follow\nthe provided this value and are then inserted at the start of the\narguments passed to the target function, followed by the arguments\npassed to the bound function, whenever the bound function is\ncalled.</p>\n\n<pre><code>function list() {\n  return Array.prototype.slice.call(arguments);\n}\n\nvar list1 = list(1, 2, 3); // [1, 2, 3]\n\n//  Create a function with a preset leading argument\nvar leadingZeroList = list.bind(undefined, 37);\n\nvar list2 = leadingZeroList(); // [37]\nvar list3 = leadingZeroList(1, 2, 3); // [37, 1, 2, 3]\n</code></pre>\n\n<p><strong>NOTE:</strong> This method is part of the ECMAScript 5 standard.</p>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>thisArg</span> : <a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a><div class='sub-desc'><p>The value to be passed as the <code>this</code>\nparameter to the target function when the bound function is\ncalled. The value is ignored if the bound function is constructed\nusing the new operator.</p>\n</div></li><li><span class='pre'>args</span> : Mixed... (optional)<div class='sub-desc'><p>Arguments to prepend to arguments provided\nto the bound function when invoking the target function.</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/Function\" rel=\"Function\" class=\"docClass\">Function</a></span><div class='sub-desc'><p>The bound function.</p>\n</div></li></ul></div></div></div><div id='method-call' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Function'>Function</span><br/><a href='source/Function.html#Function-method-call' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Function-method-call' class='name expandable'>call</a>( <span class='pre'>thisArg, args</span> ) : <a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a></div><div class='description'><div class='short'>Calls (executes) a method of another object in the context of a different\nobject (the calling object); arguments can ...</div><div class='long'><p>Calls (executes) a method of another object in the context of a different\nobject (the calling object); arguments can be passed as they are.</p>\n\n<p>You can assign a different this object when calling an existing function. <code>this</code> refers to the\ncurrent object, the calling object.</p>\n\n<p>With <code>call</code>, you can write a method once and then inherit it in another object, without having to\nrewrite the method for the new object.</p>\n\n<p>You can use call to chain constructors for an object, similar to Java. In the following example,\nthe constructor for the product object is defined with two parameters, name and value. Another\nobject, <code>prod_dept</code>, initializes its unique variable (<code>dept</code>) and calls the constructor for\n<code>product</code> in its constructor to initialize the other variables.</p>\n\n<pre><code>function Product(name, price) {\n    this.name = name;\n    this.price = price;\n\n    if (price &lt; 0)\n        throw RangeError('Cannot create product \"' + name + '\" with a negative price');\n    return this;\n}\n\nfunction Food(name, price) {\n    Product.call(this, name, price);\n    this.category = 'food';\n}\nFood.prototype = new Product();\n\nfunction Toy(name, price) {\n    Product.call(this, name, price);\n    this.category = 'toy';\n}\nToy.prototype = new Product();\n\nvar cheese = new Food('feta', 5);\nvar fun = new Toy('robot', 40);\n</code></pre>\n\n<p>In this purely constructed example, we create anonymous function and use <code>call</code> to invoke it on\nevery object in an array. The main purpose of the anonymous function here is to add a print\nfunction to every object, which is able to print the right index of the object in the array.\nPassing the object as <code>this</code> value was not strictly necessary, but is done for explanatory purpose.</p>\n\n<pre><code>var animals = [\n{species: 'Lion', name: 'King'},\n{species: 'Whale', name: 'Fail'}\n];\n\nfor (var i = 0; i &lt; animals.length; i++) {\n    (function (i) {\n    this.print = function () {\n        console.log('#' + i  + ' ' + this.species + ': ' + this.name);\n    }\n}).call(animals[i], i);\n}\n</code></pre>\n<h3 class=\"pa\">Parameters</h3><ul><li><span class='pre'>thisArg</span> : <a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a><div class='sub-desc'><p>The value of this provided for the call to <code>fun</code>.Note that this may not be\nthe actual value seen by the method: if the method is a function in non-strict mode code, <code>null</code>\nand <code>undefined</code> will be replaced with the global object, and primitive values will be boxed.</p>\n</div></li><li><span class='pre'>args</span> : <a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a>...<div class='sub-desc'><p>Arguments for the object.</p>\n</div></li></ul><h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/Object\" rel=\"Object\" class=\"docClass\">Object</a></span><div class='sub-desc'><p>Returns what the function returns.</p>\n</div></li></ul></div></div></div><div id='method-toString' class='member  not-inherited'><a href='#' class='side expandable'><span>&nbsp;</span></a><div class='title'><div class='meta'><span class='defined-in' rel='Function'>Function</span><br/><a href='source/Function.html#Function-method-toString' target='_blank' class='view-source'>view source</a></div><a href='#!/api/Function-method-toString' class='name expandable'>toString</a>( <span class='pre'></span> ) : <a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a></div><div class='description'><div class='short'>Returns a string representing the source code of the function. ...</div><div class='long'><p>Returns a string representing the source code of the function. Overrides the\n<code><a href=\"#!/api/Object-method-toString\" rel=\"Object-method-toString\" class=\"docClass\">Object.toString</a></code> method.</p>\n\n<p>The <a href=\"#!/api/Function\" rel=\"Function\" class=\"docClass\">Function</a> object overrides the <code>toString</code> method of the Object object; it does\nnot inherit <a href=\"#!/api/Object-method-toString\" rel=\"Object-method-toString\" class=\"docClass\">Object.toString</a>. For <code>Function</code> objects, the <code>toString</code> method returns a string\nrepresentation of the object.</p>\n\n<p>JavaScript calls the <code>toString</code> method automatically when a <code>Function</code> is to be represented as a\ntext value or when a Function is referred to in a string concatenation.</p>\n\n<p>For <code>Function</code> objects, the built-in <code>toString</code> method decompiles the function back into the\nJavaScript source that defines the function. This string includes the <code>function</code> keyword, the\nargument list, curly braces, and function body.</p>\n<h3 class='pa'>Returns</h3><ul><li><span class='pre'><a href=\"#!/api/String\" rel=\"String\" class=\"docClass\">String</a></span><div class='sub-desc'><p>The function as a string.</p>\n</div></li></ul></div></div></div></div></div></div></div>"});