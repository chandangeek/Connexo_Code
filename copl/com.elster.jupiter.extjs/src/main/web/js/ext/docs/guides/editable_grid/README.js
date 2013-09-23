Ext.data.JsonP.editable_grid({"title":"Editable Grid + Node.js 1","guide":"<h1>Editable Grid + Node.js, Part 1</h1>\n\n<p>The <a href=\"#!/api/Ext.grid.Panel\" rel=\"Ext.grid.Panel\" class=\"docClass\">Grid</a> is a powerful way to display tabular data. It is an ideal solution for displaying dynamic data from a database. It can also allow users to edit the fields of the data displayed in the grid. Changes to the dataset can be easily saved back to the server. This guide describes how to create this functionality using Ext's MVC application architecture. If you're not familiar with this, then I recommend you checkout <a href=\"#!/guide/application_architecture\">the guide</a> about it for more details.</p>\n\n<p>The example dataset we will be dealing with is the set of movies which Computer Geeks like. So we will call the demo app GeekFlicks.</p>\n\n<h2>Setting up the Application</h2>\n\n<p>First, lets create a folder structure for our app as described in the <a href=\"#!/guide/getting_started\">getting started guide</a>:</p>\n\n<pre><code>GeekFlicks\n    app/\n        controller/\n        model/\n        store/\n        view/\n    extjs/\n</code></pre>\n\n<p>The extjs/ folder has the ExtJS 4 SDK (or a symlink to it). In the GeekFlicks folder, we create a index.html file with the following:</p>\n\n<pre><code>&lt;!doctype html&gt;\n&lt;html&gt;\n  &lt;head&gt;\n    &lt;meta charset=\"utf-8\"&gt;\n    &lt;title&gt;Editable Grid&lt;/title&gt;\n    &lt;link rel=\"stylesheet\" href=\"extjs/resources/css/ext-all.css\"&gt;\n    &lt;script src=\"extjs/ext-all-debug.js\"&gt;&lt;/script&gt;\n    &lt;script&gt;\n      <a href=\"#!/api/Ext.Loader-method-setConfig\" rel=\"Ext.Loader-method-setConfig\" class=\"docClass\">Ext.Loader.setConfig</a>({\n        enabled: true\n      });\n    &lt;/script&gt;\n    &lt;script type=\"text/javascript\" src=\"app.js\"&gt; &lt;/script&gt;\n  &lt;/head&gt;\n  &lt;body&gt;\n\n  &lt;/body&gt;\n&lt;/html&gt;\n</code></pre>\n\n<p>I'm using the HTML5 recommended syntax here, though this is not necessary. Also, note that I have included 'ext-all-debug.js' not 'ext.js'. This ensures that all the Ext JS classes are available immediately after it is loaded, rather than loading them all dynamically, which is what would occur if you used 'ext.js' (or ext-debug.js). The grid does require a large number of classes, and this tends to slow down the initial page load, and clutter up the class list with a bunch of classes, which makes finding your own classes harder. However the MVC Application class does require the Loader to be enabled, and it is disabled by default when you use the 'ext-all' version. So I've manually re-enabled it here.</p>\n\n<p>The app.js has this:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-application\" rel=\"Ext-method-application\" class=\"docClass\">Ext.application</a>({\n    name: \"GeekFlicks\",\n    appFolder: \"app\",\n    launch: function () {\n        <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.container.Viewport\" rel=\"Ext.container.Viewport\" class=\"docClass\">Ext.container.Viewport</a>', {\n            layout: 'fit',\n            items: [{\n                xtype: 'panel',\n                title: 'Flicks for Geeks',\n                html: 'Add your favorite geeky movies'\n            }]\n        });\n    }\n});\n</code></pre>\n\n<p>So, if you stick this on a webserver and navigate to it, you should see a panel with the title \"Flicks for Geeks\" and the text \"Add your favorite geeky movies\" beneath it. If not, check the console for errors.. perhaps something got misplaced. Now we still don't have a grid anywhere in sight, so lets remedy that.</p>\n\n<h3>The View</h3>\n\n<p>Create a view for the editable grid in the 'views' folder, called 'Movies', with the following code :</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('GeekFlicks.view.Movies', {\n    extend: '<a href=\"#!/api/Ext.grid.Panel\" rel=\"Ext.grid.Panel\" class=\"docClass\">Ext.grid.Panel</a>',\n    id: \"movies_editor\",\n    alias: 'widget.movieseditor',\n    initComponent: function () {\n        //hardcoded store with static data:\n        this.store = {\n            fields: ['title', 'year'],\n            data: [{\n                title: 'The Matrix',\n                year: '1999'\n            }, {\n                title: 'Star Wars: Return of the Jedi',\n                year: '1983'\n            }]\n        };\n        this.columns = [{\n            header: 'Title',\n            dataIndex: 'title',\n            flex: 1\n        }, {\n            header: 'Year',\n            dataIndex: 'year',\n            flex: 1\n        }];\n        this.callParent(arguments);\n    }\n});\n</code></pre>\n\n<p>This creates a new class called 'Movies' which extends the grid, and hardcodes some data in a store, which is simply declared inline. This will be refactored later, but is enough to get us going for now.</p>\n\n<h3>The Controller</h3>\n\n<p>Lets now create the controller, in the 'controller' folder, as follows:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>(\"GeekFlicks.controller.Movies\", {\n    extend: '<a href=\"#!/api/Ext.app.Controller\" rel=\"Ext.app.Controller\" class=\"docClass\">Ext.app.Controller</a>',\n    views: [\n        'Movies'\n    ],\n    init: function () {\n        this.control({\n            '#movies_editor': {\n                render: this.onEditorRender\n            }\n        });\n    },\n\n    onEditorRender: function () {\n        console.log(\"movies editor was rendered\");\n    }\n});\n</code></pre>\n\n<p>This sets up a controller for the view we just created, by including it in the 'views' array.  The 'init()' method is automatically called by the Application when it starts. The 'control()' method adds the 'onEditorRender' event listener to the movies editor grid which was selected by the ComponentQuery expression '#movies_editor'. This is nice because it means the view does not have to know anything about the Controller (or the Model) and so it can potentially be reused in more than one context.</p>\n\n<p>So now we have added a view, and a controller to listen for its events. We now need to tell the Application about them. Firstly, the view: we simply replace the hardcoded panel we had before with an item of the 'movieseditor' xtype, which will add the view. We also set the 'controllers' array to contain the 'Movies' controller.</p>\n\n<pre><code><a href=\"#!/api/Ext-method-application\" rel=\"Ext-method-application\" class=\"docClass\">Ext.application</a>({\n    name: \"GeekFlicks\",\n    appFolder: \"app\",\n    controllers: ['Movies'],\n    launch: function () {\n        <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.container.Viewport\" rel=\"Ext.container.Viewport\" class=\"docClass\">Ext.container.Viewport</a>', {\n            layout: 'fit',\n            items: [{\n                xtype: 'panel',\n                title: 'Top Geek Flicks of All Time',\n                items: [{\n                    xtype: 'movieseditor'\n                }]\n            }]\n        });\n    }\n});\n</code></pre>\n\n<p>Now, you should see the actual grid panel show up, with the data we hardcoded into the view. Next we will refactor, to make this data load dynamically.</p>\n\n<h3>The Model</h3>\n\n<p>The Model element of the MVC trinity, consists of a few classes with specific responsibilities. They are as follows:</p>\n\n<ul>\n<li>The Model: defines the schema of the data (think of it as a data-model or object-model)</li>\n<li>The Store: stores records of data (which are defined by the Model)</li>\n<li>The Proxy: loads the Store from the server (or other storage) and saves changes</li>\n</ul>\n\n\n<p>These are covered in more detail in the <a href=\"#!/guide/data\">data package guide</a>. So lets define our data, first by creating a Movie.js in the 'model' folder:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('GeekFlicks.model.Movie', {\n    extend: '<a href=\"#!/api/Ext.data.Model\" rel=\"Ext.data.Model\" class=\"docClass\">Ext.data.Model</a>',\n    fields: [{\n        name: 'title',\n        type: 'string'\n    }, {\n        name: 'year',\n        type: 'int'\n    }]\n});\n</code></pre>\n\n<p>Then, in store/Movies.js, add the following:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('GeekFlicks.store.Movies', {\n    extend: '<a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Ext.data.Store</a>',\n    model: 'GeekFlicks.model.Movie',\n    data: [{\n        title: 'The Matrix',\n        year: '1999'\n    }, {\n        title: 'Star Wars: Return of the Jedi',\n        year: '1983'\n    }]\n});\n</code></pre>\n\n<p>Which is just copied from the View, where it was previously set in the initComponent() method. The one change is that instead on the fields being defined inline, there is a reference to the Model we just created, where they are defined. Let's now clean up the view to reference our store... change the contents of the view/Movies.js to:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('GeekFlicks.view.Movies', {\n    extend: '<a href=\"#!/api/Ext.grid.Panel\" rel=\"Ext.grid.Panel\" class=\"docClass\">Ext.grid.Panel</a>',\n    id: \"movies_editor\",\n    alias: 'widget.movieseditor',\n    store: 'Movies',\n    initComponent: function () {\n        //note: store removed\n        this.columns = [{\n            header: 'Title',\n            dataIndex: 'title',\n            flex: 1\n        }, {\n            header: 'Year',\n            dataIndex: 'year',\n            flex: 1\n        }];\n        this.callParent(arguments);\n\n    }\n});\n</code></pre>\n\n<p>Note that the 'store' configuration property was set to 'Movies' which will cause an instance of the Movies store to be instantiated at run time, and assigned to the grid.</p>\n\n<p>The Controller also needs to know about the Model and Store, so we tell it about them by adding a couple of config items, named (surprise!) 'models' and 'stores':</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>(\"GeekFlicks.controller.Movies\", {\n    extend: '<a href=\"#!/api/Ext.app.Controller\" rel=\"Ext.app.Controller\" class=\"docClass\">Ext.app.Controller</a>',\n    views: ['Movies'],\n    models: ['Movie'],\n    stores: ['Movies'],\n    init: function () {\n        this.control({\n            '#movies_editor': {\n                render: this.onEditorRender\n            }\n        });\n    },\n\n    onEditorRender: function () {\n        console.log(\"movies editor was rendered\");\n    }\n});\n</code></pre>\n\n<p>Now we still have hard-coded data in our Store, so lets fix tht using a Proxy. The proxy will load the data from a server-side script hosted at /movies.php. The first version of this script simply echoes back a JSON representation of the current movies. Here is the script:</p>\n\n<pre><code>&lt;?php\nheader(\"Content-type: application/json\");\n?&gt;\n{\n  \"success\": true,\n  \"data\": [{\n      \"title\": \"The Matrix\",\n      \"year\": \"1999\"\n  }, {\n      \"title\": \"Star Wars: Return of the Jedi\",\n      \"year\": \"1983\"\n  }]\n}\n</code></pre>\n\n<p>Now this data is still hardcoded - just on the server-side. In the Real World you will be reading this out of a database. But at least we can remove the hardcoded data from our store, and replace it with the Proxy definition:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('GeekFlicks.store.Movies', {\n    extend: '<a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Ext.data.Store</a>',\n    autoLoad: true,\n    fields: ['title', 'year'],\n    //data removed, instead using proxy:\n    proxy: {\n        type: 'ajax',\n        url: 'movies.php',\n        reader: {\n            type: 'json',\n            root: 'data',\n            successProperty: 'success'\n        }\n    }\n});\n</code></pre>\n\n<p><a href=\"guides/editable_grid/geekflicks.zip\">Download project files</a></p>\n\n<p><strong>About the Author</strong>: David Wilhelm (<a href=\"http://www.dafishinsea.com/blog/\">blog</a>, <a href=\"http://www.linkedin.com/in/dewilhelm\">linkedin</a>, <a href=\"https://plus.google.com/105009766412274176330/about\">google+</a>) is a UI developer at Blue Coat Systems.</p>\n"});