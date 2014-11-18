Ext.data.JsonP.data({"guide":"<h1>Data</h1>\n\n<hr />\n\n<p>The data package is what loads and saves all of the data in your application and consists of 41 classes, but there are three that are more important than all the others - <a href=\"#!/api/Ext.data.Model\" rel=\"Ext.data.Model\" class=\"docClass\">Model</a>, <a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Store</a> and <a href=\"#!/api/Ext.data.proxy.Proxy\" rel=\"Ext.data.proxy.Proxy\" class=\"docClass\">Ext.data.proxy.Proxy</a>. These are used by almost every application, and are supported by a number of satellite classes:</p>\n\n<p><p><img src=\"guides/data/data-package.png\" alt=\"Data package overview\"></p></p>\n\n<h3>Models and Stores</h3>\n\n<p>The centerpiece of the data package is <a href=\"#!/api/Ext.data.Model\" rel=\"Ext.data.Model\" class=\"docClass\">Ext.data.Model</a>. A Model represents some type of data in an application - for example an e-commerce app might have models for Users, Products and Orders. At its simplest a Model is just a set of fields and their data. We’re going to look at four of the principal parts of Model — <a href=\"#!/api/Ext.data.Field\" rel=\"Ext.data.Field\" class=\"docClass\">Fields</a>, <a href=\"#!/api/Ext.data.proxy.Proxy\" rel=\"Ext.data.proxy.Proxy\" class=\"docClass\">Proxies</a>, <a href=\"#!/api/Ext.data.association.Association\" rel=\"Ext.data.association.Association\" class=\"docClass\">Associations</a> and <a href=\"#!/api/Ext.data.validations\" rel=\"Ext.data.validations\" class=\"docClass\">Validations</a>.</p>\n\n<p><p><img src=\"guides/data/model.png\" alt=\"Model architecture\"></p></p>\n\n<p>Let's look at how we create a model now:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('User', {\n    extend: '<a href=\"#!/api/Ext.data.Model\" rel=\"Ext.data.Model\" class=\"docClass\">Ext.data.Model</a>',\n    fields: [\n        { name: 'id', type: 'int' },\n        { name: 'name', type: 'string' }\n    ]\n});\n</code></pre>\n\n<p>Models are typically used with a Store, which is basically a collection of Model instances. Setting up a Store and loading its data is simple:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Ext.data.Store</a>', {\n    model: 'User',\n    proxy: {\n        type: 'ajax',\n        url : 'users.json',\n        reader: 'json'\n    },\n    autoLoad: true\n});\n</code></pre>\n\n<p>We configured our Store to use an <a href=\"#!/api/Ext.data.proxy.Ajax\" rel=\"Ext.data.proxy.Ajax\" class=\"docClass\">Ajax Proxy</a>, telling it the url to load data from and the <a href=\"#!/api/Ext.data.reader.Reader\" rel=\"Ext.data.reader.Reader\" class=\"docClass\">Reader</a> used to decode the data. In this case our server is returning JSON, so we've set up a <a href=\"#!/api/Ext.data.reader.Json\" rel=\"Ext.data.reader.Json\" class=\"docClass\">Json Reader</a> to read the response.\nThe store auto-loads a set of User model instances from the url <code>users.json</code>.  The <code>users.json</code> url should return a JSON string that looks something like this:</p>\n\n<pre><code>{\n    success: true,\n    users: [\n        { id: 1, name: 'Ed' },\n        { id: 2, name: 'Tommy' }\n    ]\n}\n</code></pre>\n\n<p>For a live demo please see the <a href=\"guides/data/examples/simple_store/index.html\">Simple Store</a> example.</p>\n\n<h3>Inline data</h3>\n\n<p>Stores can also load data inline. Internally, Store converts each of the objects we pass in as <a href=\"#!/api/Ext.data.Store-cfg-data\" rel=\"Ext.data.Store-cfg-data\" class=\"docClass\">data</a> into <a href=\"#!/api/Ext.data.Model\" rel=\"Ext.data.Model\" class=\"docClass\">Model</a> instances:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Ext.data.Store</a>', {\n    model: 'User',\n    data: [\n        { firstName: 'Ed',    lastName: 'Spencer' },\n        { firstName: 'Tommy', lastName: 'Maintz' },\n        { firstName: 'Aaron', lastName: 'Conran' },\n        { firstName: 'Jamie', lastName: 'Avins' }\n    ]\n});\n</code></pre>\n\n<p><a href=\"guides/data/examples/inline_data/index.html\">Inline Data example</a></p>\n\n<h3>Sorting and Grouping</h3>\n\n<p>Stores are able to perform sorting, filtering and grouping locally, as well as supporting remote sorting, filtering and grouping:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Ext.data.Store</a>', {\n    model: 'User',\n\n    sorters: ['name', 'id'],\n    filters: {\n        property: 'name',\n        value   : 'Ed'\n    },\n    groupField: 'age',\n    groupDir: 'DESC'\n});\n</code></pre>\n\n<p>In the store we just created, the data will be sorted first by name then id; it will be filtered to only include Users with the name 'Ed' and the data will be grouped by age in descending order. It's easy to change the sorting, filtering and grouping at any time through the Store API.  For a live demo, see the <a href=\"guides/data/examples/sorting_grouping_filtering_store/index.html\">Sorting Grouping Filtering Store</a> example.</p>\n\n<h3>Proxies</h3>\n\n<p>Proxies are used by Stores to handle the loading and saving of Model data. There are two types of Proxy: Client and Server. Examples of client proxies include Memory for storing data in the browser's memory and Local Storage which uses the HTML 5 local storage feature when available. Server proxies handle the marshaling of data to some remote server and examples include Ajax, JsonP and Rest.</p>\n\n<p>Proxies can be defined directly on a Model like so:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('User', {\n    extend: '<a href=\"#!/api/Ext.data.Model\" rel=\"Ext.data.Model\" class=\"docClass\">Ext.data.Model</a>',\n    fields: ['id', 'name', 'age', 'gender'],\n    proxy: {\n        type: 'rest',\n        url : 'data/users',\n        reader: {\n            type: 'json',\n            root: 'users'\n        }\n    }\n});\n\n// Uses the User Model's Proxy\n<a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Ext.data.Store</a>', {\n    model: 'User'\n});\n</code></pre>\n\n<p>This helps us in two ways. First, it's likely that every Store that uses the User model will need to load its data the same way, so we avoid having to duplicate the Proxy definition for each Store. Second, we can now load and save Model data without a Store:</p>\n\n<pre><code>// Gives us a reference to the User class\nvar User = <a href=\"#!/api/Ext.ModelManager-method-getModel\" rel=\"Ext.ModelManager-method-getModel\" class=\"docClass\">Ext.ModelMgr.getModel</a>('User');\n\nvar ed = <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('User', {\n    name: 'Ed Spencer',\n    age : 25\n});\n\n// We can save Ed directly without having to add him to a Store first because we\n// configured a RestProxy this will automatically send a POST request to the url /users\ned.save({\n    success: function(ed) {\n        console.log(\"Saved Ed! His ID is \"+ ed.getId());\n    }\n});\n\n// Load User 1 and do something with it (performs a GET request to /users/1)\nUser.load(1, {\n    success: function(user) {\n        console.log(\"Loaded user 1: \" + user.get('name'));\n    }\n});\n</code></pre>\n\n<p>There are also Proxies that take advantage of the new capabilities of HTML5 - <a href=\"#/api/Ext.data.proxy.LocalStorage\">LocalStorage</a> and <a href=\"#/api/Ext.data.proxy.SessionStorage\">SessionStorage</a>. Although older browsers don't support these new HTML5 APIs, they're so useful that a lot of applications will benefit enormously from their presence.</p>\n\n<p><a href=\"guides/data/examples/model_with_proxy/index.html\">Example of a Model that uses a Proxy directly</a></p>\n\n<h3>Associations</h3>\n\n<p>Models can be linked together with the Associations API. Most applications deal with many different Models, and the Models are almost always related. A blog authoring application might have models for User, Post and Comment. Each User creates Posts and each Post receives Comments. We can express those relationships like so:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('User', {\n    extend: '<a href=\"#!/api/Ext.data.Model\" rel=\"Ext.data.Model\" class=\"docClass\">Ext.data.Model</a>',\n    fields: ['id', 'name'],\n    proxy: {\n        type: 'rest',\n        url : 'data/users',\n        reader: {\n            type: 'json',\n            root: 'users'\n        }\n    },\n\n    hasMany: 'Post' // shorthand for { model: 'Post', name: 'posts' }\n});\n\n<a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('Post', {\n    extend: '<a href=\"#!/api/Ext.data.Model\" rel=\"Ext.data.Model\" class=\"docClass\">Ext.data.Model</a>',\n    fields: ['id', 'user_id', 'title', 'body'],\n\n    proxy: {\n        type: 'rest',\n        url : 'data/posts',\n        reader: {\n            type: 'json',\n            root: 'posts'\n        }\n    },\n    belongsTo: 'User',\n    hasMany: { model: 'Comment', name: 'comments' }\n});\n\n<a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('Comment', {\n    extend: '<a href=\"#!/api/Ext.data.Model\" rel=\"Ext.data.Model\" class=\"docClass\">Ext.data.Model</a>',\n    fields: ['id', 'post_id', 'name', 'message'],\n\n    belongsTo: 'Post'\n});\n</code></pre>\n\n<p>It's easy to express rich relationships between different Models in your application. Each Model can have any number of associations with other Models and your Models can be defined in any order. Once we have a Model instance we can easily traverse the associated data - for example, if we wanted to log all Comments made on each Post for a given User, we can do something like this:</p>\n\n<pre><code>// Loads User with ID 1 and related posts and comments using User's Proxy\nUser.load(1, {\n    success: function(user) {\n        console.log(\"User: \" + user.get('name'));\n\n        user.posts().each(function(post) {\n            console.log(\"Comments for post: \" + post.get('title'));\n\n            post.comments().each(function(comment) {\n                console.log(comment.get('message'));\n            });\n        });\n    }\n});\n</code></pre>\n\n<p>Each of the hasMany associations we created above results in a new function being added to the Model. We declared that each User model hasMany Posts, which added the <code>user.posts()</code> function we used in the snippet above. Calling <code>user.posts()</code> returns a <a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Store</a> configured with the Post model. In turn, the Post model gets a <code>comments()</code> function because of the hasMany Comments association we set up.</p>\n\n<p>Associations aren't just helpful for loading data - they're useful for creating new records too:</p>\n\n<pre><code>user.posts().add({\n    title: 'Ext JS 4.0 MVC Architecture',\n    body: 'It\\'s a great Idea to structure your Ext JS Applications using the built in MVC Architecture...'\n});\n\nuser.posts().sync();\n</code></pre>\n\n<p>Here we instantiate a new Post, which is automatically given the User's id in the user_id field. Calling sync() saves the new Post via its configured Proxy - this, again, is an asynchronous operation to which you can pass a callback if you want to be notified when the operation completed.</p>\n\n<p>The belongsTo association also generates new methods on the model, here's how we can use those:</p>\n\n<pre><code>// get the user reference from the post's belongsTo association\npost.getUser(function(user) {\n    console.log('Just got the user reference from the post: ' + user.get('name'))\n});\n\n// try to change the post's user\npost.setUser(100, {\n    callback: function(product, operation) {\n        if (operation.wasSuccessful()) {\n            console.log('Post\\'s user was updated');\n        } else {\n            console.log('Post\\'s user could not be updated');\n        }\n    }\n});\n</code></pre>\n\n<p>Once more, the loading function (getUser) is asynchronous and requires a callback function to get at the user instance. The setUser method simply updates the foreign_key (user_id in this case) to 100 and saves the Post model. As usual, callbacks can be passed in that will be triggered when the save operation has completed - whether successful or not.</p>\n\n<h3>Loading Nested Data</h3>\n\n<p>You may be wondering why we passed a <code>success</code> function to the User.load call but didn't have to do so when accessing the User's posts and comments. This is because the above example assumes that when we make a request to get a user the server returns the user data in addition to all of its nested Posts and Comments. By setting up associations as we did above, the framework can automatically parse out nested data in a single request. Instead of making a request for the User data, another for the Posts data and then yet more requests to load the Comments for each Post, we can return all of the data in a single server response like this:</p>\n\n<pre><code>{\n    success: true,\n    users: [\n        {\n            id: 1,\n            name: 'Ed',\n            age: 25,\n            gender: 'male',\n            posts: [\n                {\n                    id   : 12,\n                    title: 'All about data in Ext JS 4',\n                    body : 'One areas that has seen the most improvement...',\n                    comments: [\n                        {\n                            id: 123,\n                            name: 'S Jobs',\n                            message: 'One more thing'\n                        }\n                    ]\n                }\n            ]\n        }\n    ]\n}\n</code></pre>\n\n<p>The data is all parsed out automatically by the framework. It's easy to configure your Models' Proxies to load data from almost anywhere, and their Readers to handle almost any response format. As with Ext JS 3, Models and Stores are used throughout the framework by many of the components such a Grids, Trees and Forms.</p>\n\n<p>See the <a href=\"guides/data/examples/associations_validations/index.html\">Associations and Validations</a> demo for a working example of models that use relationships.</p>\n\n<p>Of course, it's possible to load your data in a non-nested fashion.  This can be useful if you need to \"lazy load\" the relational data only when it's needed.  Let's just load the User data like before, except we'll assume the response only includes the User data without any associated Posts. Then we'll add a call to <code>user.posts().load()</code> in our callback to get the related Post data:</p>\n\n<pre><code>// Loads User with ID 1 User's Proxy\nUser.load(1, {\n    success: function(user) {\n        console.log(\"User: \" + user.get('name'));\n\n        // Loads posts for user 1 using Post's Proxy\n        user.posts().load({\n            callback: function(posts, operation) {\n                <a href=\"#!/api/Ext-method-each\" rel=\"Ext-method-each\" class=\"docClass\">Ext.each</a>(posts, function(post) {\n                    console.log(\"Comments for post: \" + post.get('title'));\n\n                    post.comments().each(function(comment) {\n                        console.log(comment.get('message'));\n                    });\n                });\n            }\n        });\n    }\n});\n</code></pre>\n\n<p>For a full example see <a href=\"guides/data/examples/lazy_associations/index.html\">Lazy Associations</a></p>\n\n<h3>Validations</h3>\n\n<p>As of Ext JS 4 Models became a lot richer with support for validating their data. To demonstrate this we're going to build upon the example we used above for associations. First let's add some validations to the <code>User</code> model:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('User', {\n    extend: '<a href=\"#!/api/Ext.data.Model\" rel=\"Ext.data.Model\" class=\"docClass\">Ext.data.Model</a>',\n    fields: ...,\n\n    validations: [\n        {type: 'presence', name: 'name'},\n        {type: 'length',   name: 'name', min: 5},\n        {type: 'format',   name: 'age', matcher: /\\d+/},\n        {type: 'inclusion', name: 'gender', list: ['male', 'female']},\n        {type: 'exclusion', name: 'name', list: ['admin']}\n    ],\n\n    proxy: ...\n});\n</code></pre>\n\n<p>Validations follow the same format as field definitions. In each case, we specify a field and a type of validation. The validations in our example are expecting the name field to be present and to be at least 5 characters in length, the age field to be a number, the gender field to be either \"male\" or \"female\", and the username to be anything but \"admin\". Some validations take additional optional configuration - for example the length validation can take min and max properties, format can take a matcher, etc. There are five validations built into Ext JS and adding custom rules is easy. First, let's meet the ones built right in:</p>\n\n<ul>\n<li><code>presence</code> simply ensures that the field has a value. Zero counts as a valid value but empty strings do not.</li>\n<li><code>length</code> ensures that a string is between a min and max length. Both constraints are optional.</li>\n<li><code>format</code> ensures that a string matches a regular expression format. In the example above we ensure that the age field consists only of numbers.</li>\n<li><code>inclusion</code> ensures that a value is within a specific set of values (e.g. ensuring gender is either male or female).</li>\n<li><code>exclusion</code> ensures that a value is not one of the specific set of values (e.g. blacklisting usernames like 'admin').</li>\n</ul>\n\n\n<p>Now that we have a grasp of what the different validations do, let's try using them against a User instance. We'll create a user and run the validations against it, noting any failures:</p>\n\n<pre><code>// now lets try to create a new user with as many validation errors as we can\nvar newUser = <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('User', {\n    name: 'admin',\n    age: 'twenty-nine',\n    gender: 'not a valid gender'\n});\n\n// run some validation on the new user we just created\nvar errors = newUser.validate();\n\nconsole.log('Is User valid?', errors.isValid()); //returns 'false' as there were validation errors\nconsole.log('All Errors:', errors.items); //returns the array of all errors found on this model instance\n\nconsole.log('Age Errors:', errors.getByField('age')); //returns the errors for the age field\n</code></pre>\n\n<p>The key function here is validate(), which runs all of the configured validations and returns an <a href=\"#/api/Ext.data.Errors\">Errors</a> object. This simple object is just a collection of any errors that were found, plus some convenience methods such as <code>isValid()</code> - which returns true if there were no errors on any field - and <code>getByField()</code>, which returns all errors for a given field.</p>\n\n<p>For a complete example that uses validations please see <a href=\"guides/data/examples/associations_validations/index.html\">Associations and Validations</a></p>\n","title":"The Data Package"});