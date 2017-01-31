/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.data.JsonP.application_architecture({"guide":"<h1>MVC Architecture</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/application_architecture-section-1'>File Structure</a></li>\n<li><a href='#!/guide/application_architecture-section-2'>Creating the application in app.js</a></li>\n<li><a href='#!/guide/application_architecture-section-3'>Defining a Controller</a></li>\n<li><a href='#!/guide/application_architecture-section-4'>Defining a View</a></li>\n<li><a href='#!/guide/application_architecture-section-5'>Controlling the grid</a></li>\n<li><a href='#!/guide/application_architecture-section-6'>Creating a Model and a Store</a></li>\n<li><a href='#!/guide/application_architecture-section-7'>Saving data with the Model</a></li>\n<li><a href='#!/guide/application_architecture-section-8'>Deployment</a></li>\n<li><a href='#!/guide/application_architecture-section-9'>Next Steps</a></li>\n</ol>\n</div>\n\n<p>Large client side applications have always been hard to write, hard to\norganize and hard to maintain. They tend to quickly grow out of\ncontrol as you add more functionality and developers to a project. Ext\nJS 4 comes with a new application architecture that not only organizes\nyour code but reduces the amount you have to write.</p>\n\n<p>Our application architecture follows an MVC-like pattern with Models\nand Controllers being introduced for the first time. There are many\nMVC architectures, most of which are slightly different from one\nanother. Here's how we define ours:</p>\n\n<ul>\n<li><p><strong>Model</strong> is a collection of fields and their data (e.g. a User\nmodel with username and password fields). Models know how to\npersist themselves through the data package, and can be linked to\nother models through associations. Models work a lot like the Ext\nJS 3 Record class, and are normally used with\n<a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Stores</a> to present data into grids and\nother components</p></li>\n<li><p><strong>View</strong> is any type of component - grids, trees and panels are all\nviews.</p></li>\n<li><p><strong>Controllers</strong> are special places to put all of the code that makes\nyour app work - whether that's rendering views, instantiating\nModels, or any other app logic.</p></li>\n</ul>\n\n\n<p>In this guide we'll be creating a very simple application that manages\nUser data. By the end you will know how to put simple applications\ntogether using the new Ext JS 4 application architecture.</p>\n\n<p>The application architecture is as much about providing structure and\nconsistency as it is about actual classes and framework\ncode. Following the conventions unlocks a number of important\nbenefits:</p>\n\n<ul>\n<li>Every application works the same way so you only have to learn it once</li>\n<li>It's easy to share code between apps because they all work the same way</li>\n<li>You can use our build tools to create optimized versions of your\napplications for production use</li>\n</ul>\n\n\n<h2 id='application_architecture-section-1'>File Structure</h2>\n\n<p>Ext JS 4 applications follow a unified directory structure that is the\nsame for every app. Please check out the <a href=\"#/guide/getting_started\">Getting Started\nguide</a> for a detailed explanation on the\nbasic file structure of an application. In MVC layout, all classes are\nplaced into the <code>app/</code> folder, which in turn contains sub-folders to\nnamespace your models, views, controllers and stores. Here is how the\nfolder structure for the simple example app will look when we're done:</p>\n\n<p><p><img src=\"guides/application_architecture/folderStructure.png\" alt=\"Folder Structure\"></p></p>\n\n<p>In this example, we are encapsulating the whole application inside one\nfolder called '<code>account_manager</code>'. Essential files from the <a href=\"http://www.sencha.com/products/extjs/\">Ext JS 4\nSDK</a> are wrapped inside <code>ext-4/</code>\nfolder. Hence the content of our <code>index.html</code> looks like this:</p>\n\n<pre><code>&lt;html&gt;\n&lt;head&gt;\n    &lt;title&gt;Account Manager&lt;/title&gt;\n\n    &lt;link rel=\"stylesheet\" type=\"text/css\" href=\"ext-4/resources/css/ext-all.css\"&gt;\n\n    &lt;script type=\"text/javascript\" src=\"ext-4/ext-debug.js\"&gt;&lt;/script&gt;\n\n    &lt;script type=\"text/javascript\" src=\"app.js\"&gt;&lt;/script&gt;\n&lt;/head&gt;\n&lt;body&gt;&lt;/body&gt;\n&lt;/html&gt;\n</code></pre>\n\n<h2 id='application_architecture-section-2'>Creating the application in app.js</h2>\n\n<p>Every Ext JS 4 application starts with an instance of <a href=\"#!/api/Ext.app.Application\" rel=\"Ext.app.Application\" class=\"docClass\">Application</a> class. The Application contains\nglobal settings for your application (such as the app's name), as well\nas maintains references to all of the models, views and controllers\nused by the app. An Application also contains a launch function, which\nis run automatically when everything is loaded.</p>\n\n<p>Let's create a simple Account Manager app that will help us manage\nUser accounts. First we need to pick a global namespace for this\napplication. All Ext JS 4 applications should only use a single global\nvariable, with all of the application's classes nested inside\nit. Usually we want a short global variable so in this case we're\ngoing to use \"AM\":</p>\n\n<pre><code><a href=\"#!/api/Ext-method-application\" rel=\"Ext-method-application\" class=\"docClass\">Ext.application</a>({\n    requires: ['<a href=\"#!/api/Ext.container.Viewport\" rel=\"Ext.container.Viewport\" class=\"docClass\">Ext.container.Viewport</a>'],\n    name: 'AM',\n\n    appFolder: 'app',\n\n    launch: function() {\n        <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.container.Viewport\" rel=\"Ext.container.Viewport\" class=\"docClass\">Ext.container.Viewport</a>', {\n            layout: 'fit',\n            items: [\n                {\n                    xtype: 'panel',\n                    title: 'Users',\n                    html : 'List of users will go here'\n                }\n            ]\n        });\n    }\n});\n</code></pre>\n\n<p>There are a few things going on here. First we invoked\n<code><a href=\"#!/api/Ext-method-application\" rel=\"Ext-method-application\" class=\"docClass\">Ext.application</a></code> to create a new instance of Application class, to\nwhich we passed the name <code>'AM'</code>. This automatically sets up a global\nvariable <code>AM</code> for us, and registers the namespace to <code><a href=\"#!/api/Ext.Loader\" rel=\"Ext.Loader\" class=\"docClass\">Ext.Loader</a></code>,\nwith the corresponding path of '<code>app</code>' set via the <code>appFolder</code> config\noption. We also provided a simple launch function that just creates a\n<a href=\"#!/api/Ext.container.Viewport\" rel=\"Ext.container.Viewport\" class=\"docClass\">Viewport</a> which contains a single\n<a href=\"#!/api/Ext.panel.Panel\" rel=\"Ext.panel.Panel\" class=\"docClass\">Panel</a> that will fill the screen.</p>\n\n<p><p><img src=\"guides/application_architecture/panelView.png\" alt=\"Initial view with a simple Panel\"></p></p>\n\n<h2 id='application_architecture-section-3'>Defining a Controller</h2>\n\n<p>Controllers are the glue that binds an application together. All they\nreally do is listen for events (usually from views) and take some\nactions. Continuing our Account Manager application, lets create a\ncontroller.  Create a file called <code>app/controller/Users.js</code> and add\nthe following code:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.controller.Users', {\n    extend: '<a href=\"#!/api/Ext.app.Controller\" rel=\"Ext.app.Controller\" class=\"docClass\">Ext.app.Controller</a>',\n\n    init: function() {\n        console.log('Initialized Users! This happens before the Application launch function is called');\n    }\n});\n</code></pre>\n\n<p>Now lets add our newly created Users controller to the application\nconfig in app.js:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-application\" rel=\"Ext-method-application\" class=\"docClass\">Ext.application</a>({\n    ...\n\n    controllers: [\n        'Users'\n    ],\n\n    ...\n});\n</code></pre>\n\n<p>When we load our application by visiting <code>index.html</code> inside a\nbrowser, the <code>Users</code> controller is automatically loaded (because we\nspecified it in the Application definition above), and its <code>init</code>\nfunction is called just before the Application's <code>launch</code> function.</p>\n\n<p>The <code>init</code> function is a great place to set up how your controller\ninteracts with the view, and is usually used in conjunction with\nanother Controller function - <a href=\"#!/api/Ext.app.Controller-method-control\" rel=\"Ext.app.Controller-method-control\" class=\"docClass\">control</a>. The <code>control</code> function makes it easy to listen to events on\nyour view classes and take some action with a handler function. Let's\nupdate our <code>Users</code> controller to tell us when the panel is rendered:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.controller.Users', {\n    extend: '<a href=\"#!/api/Ext.app.Controller\" rel=\"Ext.app.Controller\" class=\"docClass\">Ext.app.Controller</a>',\n\n    init: function() {\n        this.control({\n            'viewport &gt; panel': {\n                render: this.onPanelRendered\n            }\n        });\n    },\n\n    onPanelRendered: function() {\n        console.log('The panel was rendered');\n    }\n});\n</code></pre>\n\n<p>We've updated the <code>init</code> function to use <code>this.control</code> to set up\nlisteners on views in our application. The <code>control</code> function uses the\nnew ComponentQuery engine to quickly and easily get references to\ncomponents on the page. If you are not familiar with ComponentQuery\nyet, be sure to check out the <a href=\"#!/api/Ext.ComponentQuery\" rel=\"Ext.ComponentQuery\" class=\"docClass\">ComponentQuery\ndocumentation</a> for a full explanation. In brief though, it allows us\nto pass a CSS-like selector that will find every matching component on\nthe page.</p>\n\n<p>In our init function above we supplied <code>'viewport &gt; panel'</code>, which\ntranslates to \"find me every Panel that is a direct child of a\nViewport\". We then supplied an object that maps event names (just\n<code>render</code> in this case) to handler functions. The overall effect is\nthat whenever any component that matches our selector fires a <code>render</code>\nevent, our <code>onPanelRendered</code> function is called.</p>\n\n<p>When we run our application now we see the following:</p>\n\n<p><p><img src=\"guides/application_architecture/firstControllerListener.png\" alt=\"Controller listener\"></p></p>\n\n<p>Not exactly the most exciting application ever, but it shows how easy\nit is to get started with organized code. Let's flesh the app out a\nlittle now by adding a grid.</p>\n\n<h2 id='application_architecture-section-4'>Defining a View</h2>\n\n<p>Until now our application has only been a few lines long and only\ninhabits two files - <code>app.js</code> and <code>app/controller/Users.js</code>. Now that\nwe want to add a grid showing all of the users in our system, it's\ntime to organize our logic a little better and start using views.</p>\n\n<p>A View is nothing more than a Component, usually defined as a subclass\nof an Ext JS component. We're going to create our Users grid now by\ncreating a new file called <code>app/view/user/List.js</code> and putting the\nfollowing into it:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.view.user.List' ,{\n    extend: '<a href=\"#!/api/Ext.grid.Panel\" rel=\"Ext.grid.Panel\" class=\"docClass\">Ext.grid.Panel</a>',\n    alias: 'widget.userlist',\n\n    title: 'All Users',\n\n    initComponent: function() {\n        this.store = {\n            fields: ['name', 'email'],\n            data  : [\n                {name: 'Ed',    email: 'ed@sencha.com'},\n                {name: 'Tommy', email: 'tommy@sencha.com'}\n            ]\n        };\n\n        this.columns = [\n            {header: 'Name',  dataIndex: 'name',  flex: 1},\n            {header: 'Email', dataIndex: 'email', flex: 1}\n        ];\n\n        this.callParent(arguments);\n    }\n});\n</code></pre>\n\n<p>Our View class is nothing more than a normal class. In this case we\nhappen to extend the Grid Component and set up an alias so that we can\nuse it as an xtype (more on that in a moment). We also passed in the\n<a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">store</a> configuration and the <a href=\"#!/api/Ext.grid.Panel-cfg-columns\" rel=\"Ext.grid.Panel-cfg-columns\" class=\"docClass\">columns</a> that the grid should render.</p>\n\n<p>Next we need to add this view to our <code>Users</code> controller. Because we\nset an alias using the special <code>'widget.'</code> format, we can use\n'userlist' as an xtype now, just like we had used <code>'panel'</code>\npreviously.</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.controller.Users', {\n    extend: '<a href=\"#!/api/Ext.app.Controller\" rel=\"Ext.app.Controller\" class=\"docClass\">Ext.app.Controller</a>',\n\n    views: [\n        'user.List'\n    ],\n\n    init: ...\n\n    onPanelRendered: ...\n});\n</code></pre>\n\n<p>And then render it inside the main viewport by modifying the launch\nmethod in <code>app.js</code> to:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-application\" rel=\"Ext-method-application\" class=\"docClass\">Ext.application</a>({\n    ...\n\n    launch: function() {\n        <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.container.Viewport\" rel=\"Ext.container.Viewport\" class=\"docClass\">Ext.container.Viewport</a>', {\n            layout: 'fit',\n            items: {\n                xtype: 'userlist'\n            }\n        });\n    }\n});\n</code></pre>\n\n<p>The only other thing to note here is that we specified <code>'user.List'</code>\ninside the views array. This tells the application to load that file\nautomatically so that we can use it when we launch. The application\nuses Ext JS 4's new dynamic loading system to automatically pull this\nfile from the server. Here's what we see when we refresh the page now:</p>\n\n<p><p><img src=\"guides/application_architecture/firstView.png\" alt=\"Our first View\"></p></p>\n\n<h2 id='application_architecture-section-5'>Controlling the grid</h2>\n\n<p>Note that our <code>onPanelRendered</code> function is still being called. This\nis because our grid class still matches the <code>'viewport &gt; panel'</code>\nselector. The reason for this is that our class extends Grid, which in\nturn extends Panel.</p>\n\n<p>At the moment, the listeners we add to this selector will actually be\ncalled for every Panel or Panel subclass that is a direct child of the\nviewport, so let's tighten that up a bit using our new xtype. While\nwe're at it, let's instead listen for double clicks on rows in the\ngrid so that we can later edit that User:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.controller.Users', {\n    extend: '<a href=\"#!/api/Ext.app.Controller\" rel=\"Ext.app.Controller\" class=\"docClass\">Ext.app.Controller</a>',\n\n    views: [\n        'user.List'\n    ],\n\n    init: function() {\n        this.control({\n            'userlist': {\n                itemdblclick: this.editUser\n            }\n        });\n    },\n\n    editUser: function(grid, record) {\n        console.log('Double clicked on ' + record.get('name'));\n    }\n});\n</code></pre>\n\n<p>Note that we changed the ComponentQuery selector (to simply\n<code>'userlist'</code>), the event name (to <code>'itemdblclick'</code>) and the handler\nfunction name (to <code>'editUser'</code>). For now we're just logging out the\nname of the User we double clicked:</p>\n\n<p><p><img src=\"guides/application_architecture/doubleClickHandler.png\" alt=\"Double click handler\"></p></p>\n\n<p>Logging to the console is all well and good but we really want to edit\nour Users. Let's do that now, starting with a new view in\n<code>app/view/user/Edit.js</code>:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.view.user.Edit', {\n    extend: '<a href=\"#!/api/Ext.window.Window\" rel=\"Ext.window.Window\" class=\"docClass\">Ext.window.Window</a>',\n    alias: 'widget.useredit',\n\n    title: 'Edit User',\n    layout: 'fit',\n    autoShow: true,\n\n    initComponent: function() {\n        this.items = [\n            {\n                xtype: 'form',\n                items: [\n                    {\n                        xtype: 'textfield',\n                        name : 'name',\n                        fieldLabel: 'Name'\n                    },\n                    {\n                        xtype: 'textfield',\n                        name : 'email',\n                        fieldLabel: 'Email'\n                    }\n                ]\n            }\n        ];\n\n        this.buttons = [\n            {\n                text: 'Save',\n                action: 'save'\n            },\n            {\n                text: 'Cancel',\n                scope: this,\n                handler: this.close\n            }\n        ];\n\n        this.callParent(arguments);\n    }\n});\n</code></pre>\n\n<p>Again we're just defining a subclass of an existing component - this\ntime <code><a href=\"#!/api/Ext.window.Window\" rel=\"Ext.window.Window\" class=\"docClass\">Ext.window.Window</a></code>. Once more we used <code>initComponent</code> to specify\nthe complex objects <code>items</code> and <code>buttons</code>. We used a <code>'fit'</code> layout\nand a form as the single item, which contains fields to edit the name\nand the email address. Finally we created two buttons, one which just\ncloses the window, and the other that will be used to save our\nchanges.</p>\n\n<p>All we have to do now is add the view to the controller, render it and\nload the User into it:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.controller.Users', {\n    extend: '<a href=\"#!/api/Ext.app.Controller\" rel=\"Ext.app.Controller\" class=\"docClass\">Ext.app.Controller</a>',\n\n    views: [\n        'user.List',\n        'user.Edit'\n    ],\n\n    init: ...\n\n    editUser: function(grid, record) {\n        var view = <a href=\"#!/api/Ext-method-widget\" rel=\"Ext-method-widget\" class=\"docClass\">Ext.widget</a>('useredit');\n\n        view.down('form').loadRecord(record);\n    }\n});\n</code></pre>\n\n<p>First we created the view using the convenient method <code><a href=\"#!/api/Ext-method-widget\" rel=\"Ext-method-widget\" class=\"docClass\">Ext.widget</a></code>,\nwhich is equivalent to <code><a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('widget.useredit')</code>. Then we\nleveraged ComponentQuery once more to quickly get a reference to the\nedit window's form. Every component in Ext JS 4 has a <code>down</code> function,\nwhich accepts a ComponentQuery selector to quickly find any child\ncomponent.</p>\n\n<p>Double clicking a row in our grid now yields something like this:</p>\n\n<p><p><img src=\"guides/application_architecture/loadedForm.png\" alt=\"Loading the form\"></p></p>\n\n<h2 id='application_architecture-section-6'>Creating a Model and a Store</h2>\n\n<p>Now that we have our edit form it's almost time to start editing our\nusers and saving those changes. Before we do that though, we should\nrefactor our code a little.</p>\n\n<p>At the moment the <code>AM.view.user.List</code> component creates a Store\ninline. This works well but we'd like to be able to reference that\nStore elsewhere in the application so that we can update the data in\nit. We'll start by breaking the Store out into its own file -\n<code>app/store/Users.js</code>:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.store.Users', {\n    extend: '<a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Ext.data.Store</a>',\n    fields: ['name', 'email'],\n    data: [\n        {name: 'Ed',    email: 'ed@sencha.com'},\n        {name: 'Tommy', email: 'tommy@sencha.com'}\n    ]\n});\n</code></pre>\n\n<p>Now we'll just make 2 small changes - first we'll ask our <code>Users</code>\ncontroller to include this Store when it loads:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.controller.Users', {\n    extend: '<a href=\"#!/api/Ext.app.Controller\" rel=\"Ext.app.Controller\" class=\"docClass\">Ext.app.Controller</a>',\n    stores: [\n        'Users'\n    ],\n    ...\n});\n</code></pre>\n\n<p>then we'll update <code>app/view/user/List.js</code> to simply reference the\nStore by id:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.view.user.List' ,{\n    extend: '<a href=\"#!/api/Ext.grid.Panel\" rel=\"Ext.grid.Panel\" class=\"docClass\">Ext.grid.Panel</a>',\n    alias: 'widget.userlist',\n    title: 'All Users',\n\n    // we no longer define the Users store in the `initComponent` method\n    store: 'Users',\n\n    initComponent: function() {\n\n        this.columns = [\n        ...\n});\n</code></pre>\n\n<p>By including the stores that our <code>Users</code> controller cares about in its\ndefinition they are automatically loaded onto the page and given a\n<a href=\"#!/api/Ext.data.Store-cfg-storeId\" rel=\"Ext.data.Store-cfg-storeId\" class=\"docClass\">storeId</a>, which makes them really easy\nto reference in our views (by simply configuring <code>store: 'Users'</code> in\nthis case).</p>\n\n<p>At the moment we've just defined our fields (<code>'name'</code> and <code>'email'</code>)\ninline on the store. This works well enough but in Ext JS 4 we have a\npowerful <code><a href=\"#!/api/Ext.data.Model\" rel=\"Ext.data.Model\" class=\"docClass\">Ext.data.Model</a></code> class that we'd like to take advantage of\nwhen it comes to editing our Users. We'll finish this section by\nrefactoring our Store to use a Model, which we'll put in\n<code>app/model/User.js</code>:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.model.User', {\n    extend: '<a href=\"#!/api/Ext.data.Model\" rel=\"Ext.data.Model\" class=\"docClass\">Ext.data.Model</a>',\n    fields: ['name', 'email']\n});\n</code></pre>\n\n<p>That's all we need to do to define our Model. Now we'll just update\nour Store to reference the Model name instead of providing fields\ninline...</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.store.Users', {\n    extend: '<a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Ext.data.Store</a>',\n    model: 'AM.model.User',\n\n    data: [\n        {name: 'Ed',    email: 'ed@sencha.com'},\n        {name: 'Tommy', email: 'tommy@sencha.com'}\n    ]\n});\n</code></pre>\n\n<p>And we'll ask the <code>Users</code> controller to get a reference to the <code>User</code>\nmodel too:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.controller.Users', {\n    extend: '<a href=\"#!/api/Ext.app.Controller\" rel=\"Ext.app.Controller\" class=\"docClass\">Ext.app.Controller</a>',\n    stores: ['Users'],\n    models: ['User'],\n    ...\n});\n</code></pre>\n\n<p>Our refactoring will make the next section easier but should not have\naffected the application's current behavior. If we reload the page now\nand double click on a row we see that the edit User window still\nappears as expected. Now it's time to finish the editing\nfunctionality:</p>\n\n<p><p><img src=\"guides/application_architecture/loadedForm.png\" alt=\"Loading the form\"></p></p>\n\n<h2 id='application_architecture-section-7'>Saving data with the Model</h2>\n\n<p>Now that we have our users grid loading data and opening an edit\nwindow when we double click each row, we'd like to save the changes\nthat the user makes. The Edit User window that the defined above\ncontains a form (with fields for name and email), and a save\nbutton. First let's update our controller's init function to listen\nfor clicks to that save button:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.controller.Users', {\n    ...\n    init: function() {\n        this.control({\n            'viewport &gt; userlist': {\n                itemdblclick: this.editUser\n            },\n            'useredit button[action=save]': {\n                click: this.updateUser\n            }\n        });\n    },\n    ...\n    updateUser: function(button) {\n        console.log('clicked the Save button');\n    }\n    ...\n});\n</code></pre>\n\n<p>We added a second ComponentQuery selector to our <code>this.control</code> call -\nthis time <code>'useredit button[action=save]'</code>. This works the same way as\nthe first selector - it uses the <code>'useredit'</code> xtype that we defined\nabove to focus in on our edit user window, and then looks for any\nbuttons with the <code>'save'</code> action inside that window. When we defined\nour edit user window we passed <code>{action: 'save'}</code> to the save button,\nwhich gives us an easy way to target that button.</p>\n\n<p>We can satisfy ourselves that the <code>updateUser</code> function is called when\nwe click the Save button:</p>\n\n<p><p><img src=\"guides/application_architecture/saveHandler.png\" alt=\"Seeing the save handler\"></p></p>\n\n<p>Now that we've seen our handler is correctly attached to the Save\nbutton's click event, let's fill in the real logic for the\n<code>updateUser</code> function. In this function we need to get the data out of\nthe form, update our User with it and then save that back to the Users\nstore we created above. Let's see how we might do that:</p>\n\n<pre><code>updateUser: function(button) {\n    var win    = button.up('window'),\n        form   = win.down('form'),\n        record = form.getRecord(),\n        values = form.getValues();\n\n    record.set(values);\n    win.close();\n}\n</code></pre>\n\n<p>Let's break down what's going on here. Our click event gave us a\nreference to the button that the user clicked on, but what we really\nwant is access to the form that contains the data and the window\nitself. To get things working quickly we'll just use ComponentQuery\nagain here, first using <code>button.up('window')</code> to get a reference to\nthe Edit User window, then <code>win.down('form')</code> to get the form.</p>\n\n<p>After that we simply fetch the record that's currently loaded into the\nform and update it with whatever the user has typed into the\nform. Finally we close the window to bring attention back to the\ngrid. Here's what we see when we run our app again, change the name\nfield to <code>'Ed Spencer'</code> and click save:</p>\n\n<p><p><img src=\"guides/application_architecture/updatedGridRecord.png\" alt=\"The record in the grid has been updated\"></p></p>\n\n<h3>Saving to the server</h3>\n\n<p>Easy enough. Let's finish this up now by making it interact with our\nserver side. At the moment we are hard coding the two User records\ninto the Users Store, so let's start by reading those over AJAX\ninstead:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('AM.store.Users', {\n    extend: '<a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Ext.data.Store</a>',\n    model: 'AM.model.User',\n    autoLoad: true,\n\n    proxy: {\n        type: 'ajax',\n        url: 'data/users.json',\n        reader: {\n            type: 'json',\n            root: 'users',\n            successProperty: 'success'\n        }\n    }\n});\n</code></pre>\n\n<p>Here we removed the <code>'data'</code> property and replaced it with a\n<a href=\"#!/api/Ext.data.proxy.Proxy\" rel=\"Ext.data.proxy.Proxy\" class=\"docClass\">Proxy</a>. Proxies are the way to load and\nsave data from a Store or a Model in Ext JS 4. There are proxies for\nAJAX, JSON-P and HTML5 localStorage among others. Here we've used a\nsimple AJAX proxy, which we've told to load data from the url\n<code>'data/users.json'</code>.</p>\n\n<p>We also attached a <a href=\"#!/api/Ext.data.reader.Reader\" rel=\"Ext.data.reader.Reader\" class=\"docClass\">Reader</a> to the\nProxy. The reader is responsible for decoding the server response into\na format the Store can understand. This time we used a <a href=\"#!/api/Ext.data.reader.Json\" rel=\"Ext.data.reader.Json\" class=\"docClass\">JSON Reader</a>, and specified the root and\n<code>successProperty</code> configurations. Finally we'll create our\n<code>data/users.json</code> file and paste our previous data into it:</p>\n\n<pre><code>{\n    \"success\": true,\n    \"users\": [\n        {\"id\": 1, \"name\": 'Ed',    \"email\": \"ed@sencha.com\"},\n        {\"id\": 2, \"name\": 'Tommy', \"email\": \"tommy@sencha.com\"}\n    ]\n}\n</code></pre>\n\n<p>The only other change we made to the Store was to set <code>autoLoad</code> to\n<code>true</code>, which means the Store will ask its Proxy to load that data\nimmediately. If we refresh the page now we'll see the same outcome as\nbefore, except that we're now no longer hard coding the data into our\napplication.</p>\n\n<p>The last thing we want to do here is send our changes back to the\nserver. For this example we're just using static JSON files on the\nserver side so we won't see any database changes but we can at least\nverify that everything is plugged together correctly. First we'll make\na small change to our new proxy to tell it to send updates back to a\ndifferent url:</p>\n\n<pre><code>proxy: {\n    type: 'ajax',\n    api: {\n        read: 'data/users.json',\n        update: 'data/updateUsers.json'\n    },\n    reader: {\n        type: 'json',\n        root: 'users',\n        successProperty: 'success'\n    }\n}\n</code></pre>\n\n<p>We're still reading the data from <code>users.json</code> but any updates will be\nsent to <code>updateUsers.json</code>. This is just so we know things are working\nwithout overwriting our test data. After updating a record, the\n<code>updateUsers.json</code> file just contains <code>{\"success\": true}</code>. Since it is\nupdated through a HTTP POST command, you may have to create an empty\nfile to avoid receiving a 404 error. <code>updateUsers.json</code> is just a placeholder\nfile that does nothing, it's only there to simulate a server page that\nwould update the data to a persistent data store.</p>\n\n<p>The only other change we need to make is to tell our Store to\nsynchronize itself after editing, which we do by adding one more line\ninside the updateUser function, which now looks like this:</p>\n\n<pre><code>updateUser: function(button) {\n    var win    = button.up('window'),\n        form   = win.down('form'),\n        record = form.getRecord(),\n        values = form.getValues();\n\n    record.set(values);\n    win.close();\n    // synchronize the store after editing the record\n    this.getUsersStore().sync();\n}\n</code></pre>\n\n<p>Now we can run through our full example and make sure that everything\nworks. We'll edit a row, hit the Save button and see that the request\nis correctly sent to <code>updateUser.json</code></p>\n\n<p><p><img src=\"guides/application_architecture/postUpdatesToServer.png\" alt=\"The record in the grid has been updated\"></p></p>\n\n<h2 id='application_architecture-section-8'>Deployment</h2>\n\n<p>The newly introduced Sencha SDK Tools (<a href=\"http://www.sencha.com/products/extjs/download/\">download\nhere</a>) makes\ndeployment of any Ext JS 4 application easier than ever. The tools\nallows you to generate a manifest of all dependencies in the form of a\nJSB3 (JSBuilder file format) file, and create a minimal custom build\nof just what your application needs within minutes.</p>\n\n<p>Please refer to the <a href=\"#/guide/getting_started\">Getting Started guide</a>\nfor detailed instructions.</p>\n\n<h2 id='application_architecture-section-9'>Next Steps</h2>\n\n<p>We've created a very simple application that manages User data and\nsends any updates back to the server. We started out simple and\ngradually refactored our code to make it cleaner and more\norganized. At this point it's easy to add more functionality to our\napplication without creating spaghetti code. The full source code for\nthis application can be found in the Ext JS 4 SDK download, inside the\nexamples/app/simple folder.</p>\n","title":"MVC Application Architecture"});