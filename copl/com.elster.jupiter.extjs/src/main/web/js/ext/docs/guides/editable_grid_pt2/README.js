/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.data.JsonP.editable_grid_pt2({"guide":"<h1>Editable Grid + Node.js, Part 2</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/editable_grid_pt2-section-1'>Updating</a></li>\n<li><a href='#!/guide/editable_grid_pt2-section-2'>Creating new Movies</a></li>\n<li><a href='#!/guide/editable_grid_pt2-section-3'>Deleting Movies</a></li>\n<li><a href='#!/guide/editable_grid_pt2-section-4'>Operation CRUD Complete</a></li>\n</ol>\n</div>\n\n<p>Since we already have the Read operation working from <a href=\"#!/guide/editable_grid\">part 1</a>,\nwe are left with the Create, Update, and Delete operations to implement.\nLet's start with updating existing movies in our grid.</p>\n\n<h2 id='editable_grid_pt2-section-1'>Updating</h2>\n\n<h3>Making the Grid Editable</h3>\n\n<p>As explained in the <a href=\"#!/api/Ext.grid.Panel\" rel=\"Ext.grid.Panel\" class=\"docClass\">Grid</a> documentation, making a grid editable requires a couple of modifications to the grid's configuration:</p>\n\n<ol>\n<li>In the grid columns config, define which form control should be used to edit the editable fields, eg. 'textfield' for simple text</li>\n<li>Add the <a href=\"#!/api/Ext.grid.plugin.RowEditing\" rel=\"Ext.grid.plugin.RowEditing\" class=\"docClass\">RowEditing</a> plugin to the grid's plugins list.</li>\n</ol>\n\n\n<p>Here is <code>view/Movies.js</code> after these changes have been applied:</p>\n\n<pre><code>/**\n * The Grid of Movies\n */\n<a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('GeekFlicks.view.Movies', {\n    extend: '<a href=\"#!/api/Ext.grid.Panel\" rel=\"Ext.grid.Panel\" class=\"docClass\">Ext.grid.Panel</a>',\n    alias: 'widget.movieseditor',\n    selType: 'rowmodel',\n    rowEditor: <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.grid.plugin.RowEditing\" rel=\"Ext.grid.plugin.RowEditing\" class=\"docClass\">Ext.grid.plugin.RowEditing</a>', {\n        clicksToEdit: 2\n    }),\n    store: 'Movies',\n\n    initComponent: function () {\n        this.columns = [\n            {\n                header: 'Title',\n                dataIndex: 'title',\n                editor: {\n                    xtype: 'textfield',\n                    allowBlank: true\n                },\n                flex: 1\n            },\n            {\n                header: 'Year',\n                dataIndex: 'year',\n                editor: {\n                    xtype: 'numberfield',\n                    allowBlank: true\n                },\n                flex: 1\n            }\n        ];\n        this.plugins = [ this.rowEditor ];\n        this.callParent(arguments);\n    }\n});\n</code></pre>\n\n<p>The grid's <code>selType</code> config option determines whether clicking on the grid selects a whole row (the default) or a specific cell (<code>selType = 'cellmodel'</code>). Here, we've chosen 'rowmodel'. The definitions of the editable fields is done in the columns config. Finally, the RowEditing plugin allows you to choose whether it takes one or two clicks to edit the field. Since we may want to allow the user to select a row without editing it, we'll go with 2 clicks. We are also allowing the user to enter an empty value.</p>\n\n<p>If you open this up in a browser, you should see that the rows of the grid are editable, ie. if you double-click on any one of them, all the fields in the row become editable. If you click <code>OK</code> or hit <code>Enter</code> while editing, the field goes back to being plain text, and now contains the edited text. If you have made changes, a small red marker will appear in the top left corner of the textfield. This indicates that the value has changed, but the changes have not been saved.</p>\n\n<p><strong>Note</strong> We specified the editor 'xtype' of the Year column to be 'numberfield'. When editing this column, the grid will use a <a href=\"#!/api/Ext.form.field.Number\" rel=\"Ext.form.field.Number\" class=\"docClass\">NumberField</a> to ensure that the user can only enter a number here (and they get the numeric stepper buttons to increment the values easily).</p>\n\n<h3>Updating the Server</h3>\n\n<p>Now that we have allowed the user to change the values in the grid, it would be nice to provide a way to save the changes to the server. One way would be to provide a 'Save' button, and then when it is clicked, call the <code>sync</code> method of the Movies Store. This would trigger the Store to save all changes made since the last time it was synced. The other is to set the <code>autoSync</code> config option of the grid to 'true' (it is false by default) and then the Store will automatically save each change as soon as it is made. We will use the <code>autoSave</code> approach here.</p>\n\n<p>If we add the line <code>autoSync: true</code> just after the <code>autoLoad: true</code> line in the <code>app/store/Movies.js</code> file, reload the app and then edit one of the fields, we see that there is an error reported. This is because the Store attempts to save the value using its Proxy, which is currently the default Ajax proxy. The Ajax Proxy issued a POST request to its URL (/movies) containing a JSON representation of the value of the row which was edited. The request fails because we have not setup a handler for 'POST' requests in our Node.js app. So we need to go back to the server code and decide how to design our webservice API.</p>\n\n<h3>Designing the Server API: REST vs RPC</h3>\n\n<p>Webservices come in two main flavors: RPC (Remote Procedure Call) and RESTful. <a href=\"http://en.wikipedia.org/wiki/Remote_procedure_call\">RPC</a> style APIs have been a common approach, but for this tutorial we will be using a <a href=\"http://en.wikipedia.org/wiki/Representational_state_transfer\">RESTful API</a>. In a nutshell, a RESTful API defines resources using unique URLs, while the CRUD actions are indicated by the HTTP request methods POST, GET, PUT, and DELETE respectively. We can describe the desired API of our application as follows:</p>\n\n<table>\n<tr><td>    Request Method</td><td>     URL       </td><td>   Action Performed              </td><td>         Response (JSON)                                </td></tr>\n<tr><td>    GET       </td><td>    /movies    </td><td>   READ the movies               </td><td>         The list of movies                             </td></tr>\n<tr><td>    GET       </td><td>    /movies/123</td><td>   READ the movie with id = 123  </td><td>         The movie with id 123                          </td></tr>\n<tr><td>    POST      </td><td>    /movies    </td><td>   CREATE the given movie        </td><td>         The new movie with a unique ID                 </td></tr>\n<tr><td>    PUT       </td><td>    /movies/123</td><td>   UPDATE the movie with id = 123</td><td>         The movie with the id 123, after it was updated</td></tr>\n<tr><td>    DELETE    </td><td>    /movies/123</td><td>   DELETE the movie with id = 123</td><td>         Whether the delete succeeded or not            </td></tr>\n</table>\n\n\n<h3>Using the REST Proxy</h3>\n\n<p>There is a <a href=\"#!/api/Ext.data.proxy.Rest\" rel=\"Ext.data.proxy.Rest\" class=\"docClass\">REST Proxy</a> for use with RESTful APIs, so let's change the definition of the store to use it. The Movies Store now looks like this:</p>\n\n<pre><code>/**\n * The Movies store\n */\n<a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('GeekFlicks.store.Movies', {\n    extend: '<a href=\"#!/api/Ext.data.Store\" rel=\"Ext.data.Store\" class=\"docClass\">Ext.data.Store</a>',\n\n    autoLoad: true,\n    autoSync: true,\n    fields: ['_id', 'title', 'year'],\n\n    proxy: {\n        type: 'rest',\n        url: '/movies',\n        model: 'GeekFlicks.model.Movie',\n        reader: {\n            type: 'json',\n            root: 'data',\n            successProperty: 'success'\n        }\n    }\n});\n</code></pre>\n\n<p>Note that we specified the Model that the Proxy must use, and we added the <code>_id</code> field which was missing from the list of fields before. In addition, we need to specify that the <code>_id</code> field is indeed the idProperty of the Model by adding the config <code>idProperty: '_id'</code> to the Model as defined in /app/model/Movie.js.</p>\n\n<h3>Handling the PUT Request to Update</h3>\n\n<p>Now when we reload the app and modify a field, we see that there is a PUT request made to a URL of the form, '/movies/1234' where the number in the path is the ID of the movie in our database. We still receive a server error in response, so we should implement the web service in our Node.js server app. We can do this by defining a new route in the Express application (the toplevel /app.js) as follows:</p>\n\n<pre><code>// Routes...\n\napp.put('/movies/:id', function(req, res){\n    movieModel.find({_id: req.params.id}, function (err, movies) {\n        // update db\n        var movie = movies[0];\n        movie.set(req.body);\n        movie.save(function (err) {\n            res.contentType('json');\n            res.json({\n                success: !err,\n                data: req.body\n            });\n        });\n    });\n});\n</code></pre>\n\n<p>The '/movies/:id' special URL causes the movie ID part of the URL to be captured as 'req.params.id'. We plug this in the Mongoose find() function which acts like a SELECT statement in SQL, and grab the first result. Then we use the 'set' Mongoose method to set all the fields of the movie model to the values which were posted. Note: the JSON request body is automatically parsed into the 'req.body' object because of this configuration line:</p>\n\n<pre><code>app.use(express.bodyParser()); //parse JSON into objects\n</code></pre>\n\n<p>Finally, we respond with a JSON response containing the 'success' parameter (true if no error was reported) and the updated value of the document (which will be the same as the request since it was successfully set).</p>\n\n<p>Now if you stop and restart the node app, you should be able to make modifications to the movies, and see that they are preserved after refreshing the browser.</p>\n\n<p><p><img src=\"guides/editable_grid_pt2/editing.png\" alt=\"Updating a Movie\"></p></p>\n\n<h2 id='editable_grid_pt2-section-2'>Creating new Movies</h2>\n\n<h3>Adding an 'Add Movie' Button</h3>\n\n<p>In order to allow the user to add new movies, we need to change the view to include an 'Add Movie' button somewhere. Currently the Movies view extends from GridPanel, which extends from Panel. This means that we can add a bottom toolbar as one of its <code>dockedItems</code>:</p>\n\n<pre><code>// app/view/Movies.js\n\n//... at the end of initComponent method override:\n\nthis.dockedItems = [{\n    xtype: 'toolbar',\n    dock: 'bottom',\n    items: [\n        '-&gt;',\n        {\n            text: 'Add Movie'\n        }\n    ]\n}];\n\nthis.callParent(arguments);\n</code></pre>\n\n<p>In case you are wondering, the '->' special form expands to a ToolbarFill which ensures that the Add Movie Button is aligned to the right of the toolbar. The default xtype inside a Toolbar is a Button, so we only need to specify its label text.</p>\n\n<h3>Adding a new Movie Model to the Store</h3>\n\n<p>Handling events such as button clicks is the Controller's job, so lets tell it to listen for them by adding another item to the <code>control()</code> function in <code>app/controller/Movies.js</code>:</p>\n\n<pre><code>//...\ninit: function () {\n    this.control({\n        'movieseditor': {\n            render: this.onEditorRender\n        },\n        'movieseditor button': {\n            click: this.addMovie\n        }\n    });\n},\n\n//...\naddMovie: function () {\n    var newMovie,\n        movieStore = this.getStore('Movies');\n\n    // add blank item to store -- will automatically add new row to grid\n    newMovie = movieStore.add({\n        title: '',\n        year: ''\n    })[0];\n\n    this.rowEditor.startEdit(newMovie, this.moviesEditor.columns[0]);\n}\n</code></pre>\n\n<p>The <code>addMovie()</code> handler for the click event adds a new movie to the Store and starts editing the new movie, allowing the user to fill in the field values, which are initially empty.</p>\n\n<h3>Handling the POST request to Create a new Movie</h3>\n\n<p>Because <code>autoSync</code> is set to <code>true</code> on the Store, as soon as the new empty movie is added to the Store, it will attempt to save it to the server using the POST method. This is not going to work yet as we have not yet added that capability to our server. We need to add another route to our Express app to handle the create action, as follows:</p>\n\n<pre><code>//... Routes\napp.post('/movies', function (req, res) {\n    var newMovie = new Movie();\n    var newMovieData = req.body;\n\n    // remove the id which the client sends since it is a new Movie\n    delete newMovieData['_id'];\n    newMovie.set(newMovieData);\n    newMovie.save(function (err, movie) {\n        res.contentType('json');\n        res.json({\n            success: !err,\n            data: movie\n        });\n    });\n});\n</code></pre>\n\n<p>We renamed the <code>movieModel</code> variable to be simply <code>Movie</code>, so you'll need to do a search and replace if you're following along... Sorry about that. However, the code reads a lot better with this change. The other tricky thing to note is that we want MongoDB to generate a new ID for the new Movie, so we delete the bogus one we got from the client, and then the <code>_id</code> field will automatically be created when the new Movie is saved to the database. Don't forget to restart the Node app for the changes to take effect.</p>\n\n<h3>The Case of the Disappearing Row Editor</h3>\n\n<p>Now you may have noticed that when we click the 'Add Movie' button, the row becomes editable for an instant, and then reverts back to read-only mode, having saved the empty row. What is happening here is an unfortunate side-effect of <code>autoSync: true</code>: once we create a new empty Movie Model and add it to the Store, the store sends the request to the server and when the response comes back the grid is updated with the new data (which has the server-generated <code>_id</code>). At this point the Grid re-renders its view, causing the RowEditor to be obliterated.</p>\n\n<p>So now we must reconsider the <code>autoSync</code> setting: it was helpful for modifying existing Movies, but it is causing us more trouble than good when it comes to adding new ones using a Row Editor. So let's set <code>autoSync</code> to <code>false</code> now in the Movie Store, and see if we can get our CRUD actions working another way. Since we are simply adding an empty Movie and then allowing the user to edit it, we only really need to save to the server after the edit action is complete. This can be done by adding an event listener for the <code>edit</code> event, which the Grid Panel fires after an edit finishes. Change the Movies Controller's <code>init</code> method to listen for this event and also add an event handler which simply calls <code>sync</code> on the Store:</p>\n\n<pre><code>//...\ninit: function () {\n    this.control({\n        'movieseditor': {\n            render: this.onEditorRender,\n            edit: this.afterMovieEdit\n        },\n        'movieseditor button': {\n            click: this.addMovie\n        }\n    });\n},\n\nonEditorRender: function () {\n    // cache a reference to the moviesEditor and rowEditor\n    this.moviesEditor = <a href=\"#!/api/Ext.ComponentQuery-method-query\" rel=\"Ext.ComponentQuery-method-query\" class=\"docClass\">Ext.ComponentQuery.query</a>('movieseditor')[0];\n    this.rowEditor = this.moviesEditor.rowEditor;\n},\n\nafterMovieEdit: function () {\n    var movieStore = this.getStore('Movies');\n    movieStore.sync();\n}\n</code></pre>\n\n<p>Now you should be able to add a new Movie and find that it is saved to the database (it is still there after a page refresh). The <code>onEditorRender</code> handler now does something useful, by storing a reference to the movies editor grid and its RowEditor. This helps keep some of the other code more readable.</p>\n\n<h2 id='editable_grid_pt2-section-3'>Deleting Movies</h2>\n\n<h3>Adding Delete and Edit Icons</h3>\n\n<p>By now you probably have a whole bunch of random movies in your grid from testing out the previous command. Let's clean them up. Thanks once again to our the REST Proxy, triggering a DELETE request to the server is as easy as removing the offending item from the Store. But we need some way to make this easy for the user. One way to do this is to add a delete icon in every row of the column. The <a href=\"#!/api/Ext.grid.column.Action\" rel=\"Ext.grid.column.Action\" class=\"docClass\">Action Column</a> is the perfect solution to this, and can be configured in the view for the Movie Grid. While we're at it, we might as well add an icon for editing movies as well, as not every user will realize that double-clicking on a row is required for editing it.</p>\n\n<pre><code>//...\ninitComponent: function () {\n    var movieEditor = this;\n    this.addEvents(['movieEdit', 'movieDelete']);\n    this.columns = [\n        //... other columns\n        {\n            xtype: 'actioncolumn',\n            width: 50,\n            items: [\n                {\n                    icon: 'images/edit.png',  // Use a URL in the icon config\n                    tooltip: 'Edit',\n                    handler: function(grid, rowIndex, colIndex) {\n                        movieEditor.fireEvent('movieEdit', {\n                            rowIndex: rowIndex,\n                            colIndex: colIndex\n                        });\n                    }\n                },\n                {\n                    icon: 'images/delete.png',\n                    tooltip: 'Delete',\n                    handler: function(grid, rowIndex, colIndex) {\n                        movieEditor.fireEvent('movieDelete', {\n                            rowIndex: rowIndex,\n                            colIndex: colIndex\n                        });\n                    }\n                }\n            ]\n        }\n    ];\n    //...\n</code></pre>\n\n<p>In the icon event handlers we fire a custom event: 'movieEdit' or 'movieDelete' depending on which icon was clicked. The events were added just before the columns declaration. We also pass the row and column which was clicked in a data object which is passed to the event. Of course, the icons will need to be located in the 'images/' folder of the app (they are included in the tutorial download files). I also added a line of CSS to the index.html to space them out a bit:</p>\n\n<pre><code>    &lt;style&gt;\n      img.x-action-col-icon {\n        margin-right: 5px;\n      }\n    &lt;/style&gt;\n</code></pre>\n\n<p><p><img src=\"guides/editable_grid_pt2/icons.png\" alt=\"Delete and Edit Icons\"></p></p>\n\n<h3>Deleting Movies from the Store</h3>\n\n<p>Now we add a couple more handlers for our custom events into the Movies Controller:</p>\n\n<pre><code>//...\ninit: function () {\n    this.control({\n        'movieseditor': {\n            render: this.onEditorRender,\n            edit: this.afterMovieEdit,\n            movieEdit: this.onMovieEdit,\n            movieDelete: this.onMovieDelete\n        },\n        'movieseditor button': {\n            click: this.addMovie\n        }\n    });\n},\n\n//...\n\nonMovieEdit: function (evtData) {\n    var movieStore = this.getStore('Movies');\n    var record = movieStore.getAt(evtData.rowIndex);\n    if(record) {\n        this.rowEditor.startEdit(record, this.moviesEditor.columns[evtData.colIndex]);\n    }\n},\n\nonMovieDelete: function (evtData) {\n    var movieStore = this.getStore('Movies');\n    var record = movieStore.getAt(evtData.rowIndex);\n    if(record) {\n        movieStore.remove(record);\n        movieStore.sync();\n    }\n}\n</code></pre>\n\n<p> When the <code>onMovieEdit()</code> handler gets called, we start editing the record which was referenced by the data we passed in from the View event. The <code>onMovieDelete()</code> handler is similar, but simply removes the record, and then calls <code>sync</code> since in this case the <code>edit</code> event does not fire.</p>\n\n<h3>Handling DELETE Requests</h3>\n\n<p>Since the REST Proxy will send a DELETE request to the Node.js app when we remove a Movie from the Store, we must add another route to the app to handle it.</p>\n\n<pre><code>//... Routes\n\napp.del('/movies/:id', function(req, res){\n    Movie.remove({_id: req.params.id}, function (err, movies) {\n        res.contentType('json');\n        res.json({\n            success: !err,\n            data: []\n        });\n    });\n});\n</code></pre>\n\n<p>This route is very simple: it just calls <code>remove()</code> on any Movies which have the id which was in the request (there will be just one). So now you can remove the blank row which was inadvertently added earlier. After you refresh the page, it should still be gone.</p>\n\n<h2 id='editable_grid_pt2-section-4'>Operation CRUD Complete</h2>\n\n<p>Our application now supports full CRUD operations! In a real-world application you would add input validation and escaping... but we will not be covering that here. Hopefully you have found that creating a database driven web application using ExtJS, NodeJS, and MongoDB was an effective solution. Being able to use Javascript on the server and even to access a database is indeed an exciting development.</p>\n\n<p><a href=\"guides/editable_grid_pt2/geekflicks.zip\">Download project files</a></p>\n\n<p><strong>About the Author</strong>: David Wilhelm (<a href=\"http://www.dafishinsea.com/blog/\">blog</a>, <a href=\"http://www.linkedin.com/in/dewilhelm\">linkedin</a>, <a href=\"https://plus.google.com/105009766412274176330/about\">google+</a>) is a UI developer at Blue Coat Systems.</p>\n","title":"Editable Grid + Node.js 2"});