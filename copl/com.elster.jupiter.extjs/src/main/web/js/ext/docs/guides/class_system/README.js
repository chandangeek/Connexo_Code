/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.data.JsonP.class_system({"guide":"<h1>Class System</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/class_system-section-1'>I. Overview</a></li>\n<li><a href='#!/guide/class_system-section-2'>II. Naming Conventions</a></li>\n<li><a href='#!/guide/class_system-section-3'>III. Hands-on</a></li>\n<li><a href='#!/guide/class_system-section-4'>IV. Errors Handling &amp; Debugging</a></li>\n<li><a href='#!/guide/class_system-section-5'>See Also</a></li>\n</ol>\n</div>\n\n<hr />\n\n<p>For the first time in its history, Ext JS went through a huge refactoring from the ground up with the new class system. The new architecture stands behind almost every single class written in Ext JS 4.x, hence it's important to understand it well before you start coding.</p>\n\n<p>This manual is intended for any developer who wants to create new or extend existing classes in Ext JS 4.x. It's divided into 4 sections:</p>\n\n<ul>\n<li>Section I: \"Overview\" explains the need for a robust class system</li>\n<li>Section II: \"Naming Conventions\" discusses the best practices for naming classes, methods, properties, variables and files.</li>\n<li>Section III: \"Hands-on\" provides detailed step-by-step code examples</li>\n<li>Section IV: \"Errors Handling &amp; Debugging\" gives useful tips &amp; tricks on how to deal with exceptions</li>\n</ul>\n\n\n<h2 id='class_system-section-1'>I. Overview</h2>\n\n<hr />\n\n<p>Ext JS 4 ships with more than 300 classes. We have a huge community of more than 200,000 developers to date, coming from various programming backgrounds all over the world. At that scale of a framework, we face a big challenge of providing a common code architecture that is:</p>\n\n<ul>\n<li>familiar and simple to learn</li>\n<li>fast to develop, easy to debug, painless to deploy</li>\n<li>well-organized, extensible and maintainable</li>\n</ul>\n\n\n<p>JavaScript is a classless, <a href=\"http://en.wikipedia.org/wiki/Prototype-based_programming\">prototype-oriented</a> language. Hence by nature, one of the language's most powerful features is flexibility. It can get the same job done by many different ways, in many different coding styles and techniques. That feature, however, comes with the cost of unpredictability. Without a unified structure, JavaScript code can be really hard to understand, maintain and re-use.</p>\n\n<p><a href=\"http://en.wikipedia.org/wiki/Class-based_programming\">Class-based</a> programming, on the other hand, still stays as the most popular model of OOP. <a href=\"http://en.wikipedia.org/wiki/Category:Class-based_programming_languages\">Class-based languages</a> usually require strong-typing, provide encapsulation, and come with standard coding convention. By generally making developers adhere to a large set of principles, written code is more likely to be predictable, extensible and scalable over time. However, they don't have the same dynamic capability found in such language as JavaScript.</p>\n\n<p>Each approach has its own pros and cons, but can we have the good parts of both at the same time while concealing the bad parts? The answer is yes, and we've implemented the solution in Ext JS 4.</p>\n\n<h2 id='class_system-section-2'>II. Naming Conventions</h2>\n\n<hr />\n\n<p>Using consistent naming conventions throughout your code base for classes, namespaces and filenames helps keep your code organized, structured and readable.</p>\n\n<h3>1) Classes</h3>\n\n<p>Class names may only contain <strong>alphanumeric</strong> characters. Numbers are permitted but are discouraged in most cases, unless they belong to a technical term. Do not use underscores, hyphens, or any other nonalphanumeric character. For example:</p>\n\n<ul>\n<li><code>MyCompany.useful_util.Debug_Toolbar</code> is discouraged</li>\n<li><code>MyCompany.util.Base64</code> is acceptable</li>\n</ul>\n\n\n<p>Class names should be grouped into packages where appropriate and properly namespaced using object property dot-notation (.). At the minimum, there should be one unique top-level namespace followed by the class name. For example:</p>\n\n<pre><code>MyCompany.data.CoolProxy\nMyCompany.Application\n</code></pre>\n\n<p>The top-level namespaces and the actual class names should be in CamelCased, everything else should be all lower-cased. For example:</p>\n\n<pre><code>MyCompany.form.action.AutoLoad\n</code></pre>\n\n<p>Classes that are not distributed by Sencha should never use <code>Ext</code> as the top-level namespace.</p>\n\n<p>Acronyms should also follow CamelCased convention listed above. For example:</p>\n\n<ul>\n<li><code>Ext.data.JsonProxy</code> instead of <code>Ext.data.JSONProxy</code></li>\n<li><code>MyCompany.util.HtmlParser</code> instead of <code>MyCompary.parser.HTMLParser</code></li>\n<li><code>MyCompany.server.Http</code> instead of <code>MyCompany.server.HTTP</code></li>\n</ul>\n\n\n<h3>2) Source Files</h3>\n\n<p>The names of the classes map directly to the file paths in which they are stored. As a result, there must only be one class per file. For example:</p>\n\n<ul>\n<li><code><a href=\"#!/api/Ext.util.Observable\" rel=\"Ext.util.Observable\" class=\"docClass\">Ext.util.Observable</a></code> is stored in <code>path/to/src/Ext/util/Observable.js</code></li>\n<li><code><a href=\"#!/api/Ext.form.action.Submit\" rel=\"Ext.form.action.Submit\" class=\"docClass\">Ext.form.action.Submit</a></code> is stored in <code>path/to/src/Ext/form/action/Submit.js</code></li>\n<li><code>MyCompany.chart.axis.Numeric</code> is stored in <code>path/to/src/MyCompany/chart/axis/Numeric.js</code></li>\n</ul>\n\n\n<p><code>path/to/src</code> is the directory of your application's classes. All classes should stay under this common root and should be properly namespaced for the best development, maintenance and deployment experience.</p>\n\n<h3>3) Methods and Variables</h3>\n\n<ul>\n<li><p>Similarly to class names, method and variable names may only contain <strong>alphanumeric</strong> characters. Numbers are permitted but are discouraged in most cases, unless they belong to a technical term. Do not use underscores, hyphens, or any other nonalphanumeric character.</p></li>\n<li><p>Method and variable names should always be in camelCased. This also applies to acronyms.</p></li>\n<li><p>Examples</p>\n\n<ul>\n<li>Acceptable method names:\n  encodeUsingMd5()\n  getHtml() instead of getHTML()\n  getJsonResponse() instead of <code>getJSONResponse()\n  parseXmlContent() instead of</code>parseXMLContent()</li>\n<li>Acceptable variable names:\n  var isGoodName\n  var base64Encoder\n  var xmlReader\n  var httpServer</li>\n</ul>\n</li>\n</ul>\n\n\n<h3>4) Properties</h3>\n\n<ul>\n<li><p>Class property names follow the exact same convention with methods and variables mentioned above, except the case when they are static constants.</p></li>\n<li><p>Static class properties that are constants should be all upper-cased. For example:</p>\n\n<ul>\n<li><code><a href=\"#!/api/Ext.MessageBox-property-YES\" rel=\"Ext.MessageBox-property-YES\" class=\"docClass\">Ext.MessageBox.YES</a> = \"Yes\"</code></li>\n<li><code><a href=\"#!/api/Ext.MessageBox-property-NO\" rel=\"Ext.MessageBox-property-NO\" class=\"docClass\">Ext.MessageBox.NO</a>  = \"No\"</code></li>\n<li><code>MyCompany.alien.Math.PI = \"4.13\"</code></li>\n</ul>\n</li>\n</ul>\n\n\n<h2 id='class_system-section-3'>III. Hands-on</h2>\n\n<hr />\n\n<h3>1. Declaration</h3>\n\n<h4>1.1) The Old Way</h4>\n\n<p>If you have ever used any previous version of Ext JS, you are certainly familiar with <code><a href=\"#!/api/Ext-method-extend\" rel=\"Ext-method-extend\" class=\"docClass\">Ext.extend</a></code> to create a class:</p>\n\n<pre><code>var MyWindow = <a href=\"#!/api/Ext-method-extend\" rel=\"Ext-method-extend\" class=\"docClass\">Ext.extend</a>(Object, { ... });\n</code></pre>\n\n<p>This approach is easy to follow to create a new class that inherits from another. Other than direct inheritance, however, we didn't have a fluent API for other aspects of class creation, such as configuration, statics and mixins. We will be reviewing these items in details shortly.</p>\n\n<p>Let's take a look at another example:</p>\n\n<pre><code>My.cool.Window = <a href=\"#!/api/Ext-method-extend\" rel=\"Ext-method-extend\" class=\"docClass\">Ext.extend</a>(<a href=\"#!/api/Ext.window.Window\" rel=\"Ext.window.Window\" class=\"docClass\">Ext.Window</a>, { ... });\n</code></pre>\n\n<p>In this example we want to <a href=\"http://en.wikipedia.org/wiki/Namespace_(computer_science)\">namespace</a> our new class, and make it extend from <code><a href=\"#!/api/Ext.window.Window\" rel=\"Ext.window.Window\" class=\"docClass\">Ext.Window</a></code>. There are two concerns we need to address:</p>\n\n<ol>\n<li> <code>My.cool</code> needs to be an existing object before we can assign <code>Window</code> as its property</li>\n<li> <code><a href=\"#!/api/Ext.window.Window\" rel=\"Ext.window.Window\" class=\"docClass\">Ext.Window</a></code> needs to exist / loaded on the page before it can be referenced</li>\n</ol>\n\n\n<p>The first item is usually solved with <code><a href=\"#!/api/Ext-method-namespace\" rel=\"Ext-method-namespace\" class=\"docClass\">Ext.namespace</a></code> (aliased by <code><a href=\"#!/api/Ext-method-ns\" rel=\"Ext-method-ns\" class=\"docClass\">Ext.ns</a></code>). This method recursively transverse through the object / property tree and create them if they don't exist yet. The  boring part is you need to remember adding them above <code><a href=\"#!/api/Ext-method-extend\" rel=\"Ext-method-extend\" class=\"docClass\">Ext.extend</a></code> all the time.</p>\n\n<pre><code><a href=\"#!/api/Ext-method-ns\" rel=\"Ext-method-ns\" class=\"docClass\">Ext.ns</a>('My.cool');\nMy.cool.Window = <a href=\"#!/api/Ext-method-extend\" rel=\"Ext-method-extend\" class=\"docClass\">Ext.extend</a>(<a href=\"#!/api/Ext.window.Window\" rel=\"Ext.window.Window\" class=\"docClass\">Ext.Window</a>, { ... });\n</code></pre>\n\n<p>The second issue, however, is not easy to address because <code><a href=\"#!/api/Ext.window.Window\" rel=\"Ext.window.Window\" class=\"docClass\">Ext.Window</a></code> might depend on many other classes that it directly / indirectly inherits from, and in turn, these dependencies might depend on other classes to exist. For that reason, applications written before Ext JS 4 usually include the whole library in the form of <code>ext-all.js</code> even though they might only need a small portion of the framework.</p>\n\n<h3>1.2) The New Way</h3>\n\n<p>Ext JS 4 eliminates all those drawbacks with just one single method you need to remember for class creation: <code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a></code>. Its basic syntax is as follows:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>(className, members, onClassCreated);\n</code></pre>\n\n<ul>\n<li><code>className</code>: The class name</li>\n<li><code>members</code> is an object represents a collection of class members in key-value pairs</li>\n<li><code>onClassCreated</code> is an optional function callback to be invoked when all dependencies of this class are ready, and the class itself is fully created. Due to the <a href=\"#\">new asynchronous nature</a> of class creation, this callback can be useful in many situations. These will be discussed further in <a href=\"#\">Section IV</a></li>\n</ul>\n\n\n<p><strong>Example:</strong></p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('My.sample.Person', {\n    name: 'Unknown',\n\n    constructor: function(name) {\n        if (name) {\n            this.name = name;\n        }\n    },\n\n    eat: function(foodType) {\n        alert(this.name + \" is eating: \" + foodType);\n    }\n});\n\nvar aaron = <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('My.sample.Person', 'Aaron');\n    aaron.eat(\"Salad\"); // alert(\"Aaron is eating: Salad\");\n</code></pre>\n\n<p>Note we created a new instance of <code>My.sample.Person</code> using the <code><a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>()</code> method.  We could have used the <code>new</code> keyword (<code>new My.sample.Person()</code>).  However it is recommended to get in the habit of always using <code><a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a></code> since it allows you to take advantage of dynamic loading.  For more info on dynamic loading see the <a href=\"#/guide/getting_started\">Getting Started guide</a></p>\n\n<h3>2. Configuration</h3>\n\n<p>In Ext JS 4, we introduce a dedicated <code>config</code> property that gets processed by the powerful <a href=\"#!/api/Ext.Class\" rel=\"Ext.Class\" class=\"docClass\">Ext.Class</a> pre-processors before the class is created. Features include:</p>\n\n<ul>\n<li>Configurations are completely encapsulated from other class members</li>\n<li>Getter and setter, methods for every config property are automatically generated into the class' prototype during class creation if the class does not have these methods already defined.</li>\n<li>An <code>apply</code> method is also generated for every config property.  The auto-generated setter method calls the <code>apply</code> method internally before setting the value.  Override the <code>apply</code> method for a config property if you need to run custom logic before setting the value. If <code>apply</code> does not return a value then the setter will not set the value. For an example see <code>applyTitle</code> below.</li>\n</ul>\n\n\n<p>Here's an example:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('My.own.Window', {\n    /** @readonly */\n    isWindow: true,\n\n    config: {\n        title: 'Title Here',\n\n        bottomBar: {\n            height: 50,\n            resizable: false\n        }\n    },\n\n    constructor: function(config) {\n        this.initConfig(config);\n    },\n\n    applyTitle: function(title) {\n        if (!<a href=\"#!/api/Ext-method-isString\" rel=\"Ext-method-isString\" class=\"docClass\">Ext.isString</a>(title) || title.length === 0) {\n            alert('Error: Title must be a valid non-empty string');\n        }\n        else {\n            return title;\n        }\n    },\n\n    applyBottomBar: function(bottomBar) {\n        if (bottomBar) {\n            if (!this.bottomBar) {\n                return <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('My.own.WindowBottomBar', bottomBar);\n            }\n            else {\n                this.bottomBar.setConfig(bottomBar);\n            }\n        }\n    }\n});\n\n/** A child component to complete the example. */\n<a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('My.own.WindowBottomBar', {\n    config: {\n        height: undefined,\n        resizable: true\n    }\n});\n</code></pre>\n\n<p>And here's an example of how it can be used:</p>\n\n<pre><code>var myWindow = <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('My.own.Window', {\n    title: 'Hello World',\n    bottomBar: {\n        height: 60\n    }\n});\n\nalert(myWindow.getTitle()); // alerts \"Hello World\"\n\nmyWindow.setTitle('Something New');\n\nalert(myWindow.getTitle()); // alerts \"Something New\"\n\nmyWindow.setTitle(null); // alerts \"Error: Title must be a valid non-empty string\"\n\nmyWindow.setBottomBar({ height: 100 });\n\nalert(myWindow.getBottomBar().getHeight()); // alerts 100\n</code></pre>\n\n<h3>3. Statics</h3>\n\n<p>Static members can be defined using the <code>statics</code> config</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('Computer', {\n    statics: {\n        instanceCount: 0,\n        factory: function(brand) {\n            // 'this' in static methods refer to the class itself\n            return new this({brand: brand});\n        }\n    },\n\n    config: {\n        brand: null\n    },\n\n    constructor: function(config) {\n        this.initConfig(config);\n\n        // the 'self' property of an instance refers to its class\n        this.self.instanceCount ++;\n    }\n});\n\nvar dellComputer = Computer.factory('Dell');\nvar appleComputer = Computer.factory('Mac');\n\nalert(appleComputer.getBrand()); // using the auto-generated getter to get the value of a config property. Alerts \"Mac\"\n\nalert(Computer.instanceCount); // Alerts \"2\"\n</code></pre>\n\n<h2 id='class_system-section-4'>IV. Errors Handling &amp; Debugging</h2>\n\n<hr />\n\n<p>Ext JS 4 includes some useful features that will help you with debugging and error handling.</p>\n\n<ul>\n<li><p>You can use <code>Ext.getDisplayName()</code> to get the display name of any method.  This is especially useful for throwing errors that have the class name and method name in their description:</p>\n\n<pre><code>  throw new Error('['+ Ext.getDisplayName(arguments.callee) +'] Some message here');\n</code></pre></li>\n<li><p>When an error is thrown in any method of any class defined using <code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>()</code>, you should see the method and class names in the call stack if you are using a WebKit based browser (Chrome or Safari).  For example, here is what it would look like in Chrome:</p></li>\n</ul>\n\n\n<p><p><img src=\"guides/class_system/call-stack.png\" alt=\"Call Stack\"></p></p>\n\n<h2 id='class_system-section-5'>See Also</h2>\n\n<ul>\n<li><a href=\"http://www.sencha.com/blog/countdown-to-ext-js-4-dynamic-loading-and-new-class-system\">Dynamic Loading and the New Class System</a></li>\n<li><a href=\"http://edspencer.net/2011/01/classes-in-ext-js-4-under-the-hood.html\">Classes in Ext JS 4: Under the Hood</a></li>\n<li><a href=\"http://edspencer.net/2011/01/ext-js-4-the-class-definition-pipeline.html\">The Class Definition Pipeline</a></li>\n</ul>\n\n","title":"The Class System"});