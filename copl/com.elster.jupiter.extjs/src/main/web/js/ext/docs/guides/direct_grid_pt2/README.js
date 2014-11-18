Ext.data.JsonP.direct_grid_pt2({"guide":"<h1>How to map an Ext 4 Grid to a MySQL table using Ext Direct and PHP (Part 2: CRUD)</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/direct_grid_pt2-section-1'>I. Introduction</a></li>\n<li><a href='#!/guide/direct_grid_pt2-section-2'>II. Getting Started</a></li>\n<li><a href='#!/guide/direct_grid_pt2-section-3'>III. Writing the Application</a></li>\n<li><a href='#!/guide/direct_grid_pt2-section-4'>IV. Conclusion</a></li>\n</ol>\n</div>\n\n<p>Duration: around 30 minutes</p>\n\n<h2 id='direct_grid_pt2-section-1'>I. Introduction</h2>\n\n<p>In <a href=\"#!/guide/direct_grid_pt1\">the last tutorial</a> we created a grid that pulled information from a MySQL database using Ext Direct. Through the power and simplicity of grids we created what was essentially a glorified table (albeit 'turbocharged'). To add to the dynamism that grids present to us we'll be adding CRUD (Create, Read, Update, Delete) capabilities as well. A typical scenario that benefits from this is backend interfaces where a client might want to do anything from update someone's address or rename a blog post. We've already got it reading from a database so we're a quarter of the way there already!</p>\n\n<p>By the end of this tutorial, we should have something that looks like this.</p>\n\n<p><p><img src=\"guides/direct_grid_pt2/grid-full.png\" alt=\"The final product\"></p></p>\n\n<h2 id='direct_grid_pt2-section-2'>II. Getting Started</h2>\n\n<h3>2.1 Requirements</h3>\n\n<p>You will need:</p>\n\n<ul>\n<li>A server with PHP (5.3+) and MySQL (4.1.3+) installed</li>\n<li>A browser compatible with Ext JS 4 and debugging tools</li>\n<li>A text editor</li>\n</ul>\n\n\n<p>Personally, I find Firefox with Firebug best for debugging Ext.</p>\n\n<p>It's highly recommended that you complete Part 1 before embarking on this tutorial to fully understand what we're doing. The base files that we'll be following on from <a href=\"guides/direct_grid_pt1/start.zip\">can be found here</a>.</p>\n\n<h2 id='direct_grid_pt2-section-3'>III. Writing the Application</h2>\n\n<h3>3.1 API</h3>\n\n<p>Carrying on from our last tutorial in this series, we wrote a file called grid.js that housed the JavaScript portion of our Ext application. Now, if we're going to move forward to this brave new world of CRUDing we need to make some modifications.</p>\n\n<p>Within our store variable we want to change the proxy to go from having a single Direct function (getting the results) to having four functions that will create, read, update and delete. To do this, the following is needed:</p>\n\n<p>grid.js</p>\n\n<pre><code>proxy: {\n    type: 'direct',\n    api: {\n        create: QueryDatabase.createRecord,\n        read: QueryDatabase.getResults,\n        update: QueryDatabase.updateRecords,\n        destroy: QueryDatabase.destroyRecord\n    }\n},\n</code></pre>\n\n<p>So there should no longer be a <code>directFn</code>, it's been replaced by a more robust API. Writing this tells it that it's looking for a class called QueryDatabase and the method name (e.g. createRecord).</p>\n\n<p>Next, we're going to expose these methods in our api.php file, in the first tutorial we added getResults to our server-side API, now we're going to add the names of our other functions that we'll be creating shortly.</p>\n\n<p>api.php</p>\n\n<pre><code>&lt;?php\n$API = array(\n    'QueryDatabase'=&gt;array(\n        'methods'=&gt;array(\n            'getResults'=&gt;array(\n                'len'=&gt;1\n            ),\n            'createRecord'=&gt;array(\n                'len'=&gt;1\n            ),\n            'updateRecords'=&gt;array(\n                'len'=&gt;1\n            ),\n            'destroyRecord'=&gt;array(\n                'len'=&gt;1\n            )\n        )\n    )\n);\n</code></pre>\n\n<h3>3.2 Plugin</h3>\n\n<p>Beneath this, we're going to add a plugin for grids called the Row Editor. This will give an overlay that makes it really simple for users to change a field's content and looks just like this:</p>\n\n<p><p><img src=\"guides/direct_grid_pt2/row-editor.png\" alt=\"Row Editor in action\"></p></p>\n\n<p>grid.js</p>\n\n<pre><code>...\nvar rowEditing = <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.grid.plugin.RowEditing\" rel=\"Ext.grid.plugin.RowEditing\" class=\"docClass\">Ext.grid.plugin.RowEditing</a>', {\n    clicksToMoveEditor: 1,\n    autoCancel: false\n});\n</code></pre>\n\n<p>By declaring it as a variable, it'll make it easier to use later on, as for the configuration options, <code>clicksToMoveEditor</code> is how many times the user clicks to move to a different record in our grid. <code>autoCancel</code> means that they can't discard changes before moving on and will bring up an arrow pointing to the record saying that they need to commit their changes first. Whenever adding a new component it's well worth checking out the <a href=\"#!/api/Ext.grid.plugin.RowEditing\" rel=\"Ext.grid.plugin.RowEditing\" class=\"docClass\">corresponding API documentation on it</a> to find out all the configuration possibilities.</p>\n\n<p>We also need to specify a field type for each of the fields we'll want to edit in the grid, simply append the following to the name, address and state columns:</p>\n\n<p>grid.js</p>\n\n<pre><code>...\ntext: 'Name',\nfield: {\n    type: 'textfield',\n    allowBlank: false\n}\n</code></pre>\n\n<p>For the State column, we're going to add a validation check as well.</p>\n\n<p>grid.js</p>\n\n<pre><code>...\nallowBlank: false,\nvtype: 'alpha'\n</code></pre>\n\n<p><code>vtype</code> stands for validation type and we're asking to only allow characters from the alphabet to be entered into this field as no state has a number in it. However, if you try to add a record and type a state with a space in it, e.g. New York, you'll find that you're not able to type spaces because we've <em>only</em> allowed characters in the alphabet. To get around this we need to write a custom <code>vtype</code>.</p>\n\n<p>Above our grid variable, create a new variable called alphaSpaceTest, the funny looking string after this is a 'regular expression' which will only allow hyphens, white space and letters.</p>\n\n<p>grid.js</p>\n\n<pre><code>...\nvar alphaSpaceTest = /^[-\\sa-zA-Z]+$/;\n\n<a href=\"#!/api/Ext-method-apply\" rel=\"Ext-method-apply\" class=\"docClass\">Ext.apply</a>(<a href=\"#!/api/Ext.form.field.VTypes\" rel=\"Ext.form.field.VTypes\" class=\"docClass\">Ext.form.field.VTypes</a>, {\n    //  vtype validation function\n    alphaSpace: function(val, field) {\n        return alphaSpaceTest.test(val);\n    },\n    // vtype Text property: The error text to display when the validation function returns false\n    alphaSpaceText: 'Not a valid state, must not contain numbers or special characters.',\n    // vtype Mask property: The keystroke filter mask\n    alphaSpaceMask: /^[-\\sa-zA-Z]+$/\n});\n</code></pre>\n\n<p>Then change the vtype that previously read <code>vtype: alpha</code> to <code>vtype: alphaSpace</code> and we'll have the best of both worlds.</p>\n\n<p>To actually be able to edit our grid with the nice Row Editor interface we need to reference the plugin we created earlier. Still within the grid variable write:</p>\n\n<p>grid.js</p>\n\n<pre><code>...\nplugins: [\n    rowEditing\n],\n</code></pre>\n\n<h3>3.3 UI</h3>\n\n<p>So far our grid looks a tad sparse, we're now going to beef it up a bit and add some extra controls with minimal effort. Within our grid variable we're going to add a property called <code>dockedItems</code> that will hold all of the information for our gorgeous UI.</p>\n\n<p>grid.js</p>\n\n<pre><code>...\ndockedItems: [{\n    xtype: 'toolbar',\n    store: store,\n    dock: 'bottom',\n}]\n</code></pre>\n\n<p>Setting the <code>xtype</code> to a toolbar will house the buttons that we'll create later on. You can also try <a href=\"http://docs.sencha.com/ext-js/4-0/#!/api/Ext.panel.Panel-cfg-dockedItems\">experimenting with the dock</a> seeing which position you prefer (possible values are top, left, right and bottom) and even try <code>frame: true</code>, it's just a matter of preference.</p>\n\n<h3>3.4 Updating</h3>\n\n<p>We now want to add a function that will update our MySQL table through our QueryDatabase.php file. Because this is a feature that could be used multiple times in one session, we're using the MySQLi prepare statement for it's <a href=\"http://www.linearchat.co.uk/2011/08/why-prepared-statements-in-mysql-are-a-good-thing/\">security and speed improvements</a>.</p>\n\n<p>QueryDatabase.php</p>\n\n<pre><code>...\npublic function updateRecords(stdClass $params)\n{\n    $_db = $this-&gt;__construct();\n\n    if ($stmt = $_db-&gt;prepare(\"UPDATE owners SET name=?, address=?, state=? WHERE id=?\")) {\n        $stmt-&gt;bind_param('sssd', $name, $address, $state, $id);\n\n        $name = $params-&gt;name;\n        $address = $params-&gt;address;\n        $state = $params-&gt;state;\n        //cast id to int\n        $id = (int) $params-&gt;id;\n\n        $stmt-&gt;execute();\n\n        $stmt-&gt;close();\n    }\n\n    return $this;\n}\n</code></pre>\n\n<p>When we say <code>bind_param</code>, we're telling it the order and type that our variables will appear in, so <code>sssd</code> means three strings and a decimal variable. Slightly counter-intuitively, we then say what those variables are and force the id to be an integer, by default it is a string because of the way it's been parsed with Ext Direct. We then execute the statement and close it.</p>\n\n<p>You'll see that when you update a record's value a small, red right triangle will appear on the field that you updated, if you click the refresh button on the dock you should hopefully find that it has been saved and the triangle disappears!</p>\n\n<h3>3.5 Creating</h3>\n\n<p>In life, creation is the most complicated process but with Ext we'll get through it without too much sweat. To start, within our dockedItems we want to add a button for the user to click to add a record. To do this, we make an array of items.</p>\n\n<p>grid.js</p>\n\n<pre><code>...\nitems: [\n{\n    iconCls: 'add',\n    text: 'Add'\n}]\n</code></pre>\n\n<p>The <code>iconCls</code> will be referenced in our own CSS file to give it an appropriate icon. This is just the aesthetics, to make it function properly we'll have to add a <code>handler</code> and work our PHP magic.</p>\n\n<p>A handler can either be a reference to a function variable or contain the function inline, in this case I've done it inline but you could arrange your file with all of the CRUD functions at the top or bottom and reference it like <code>handler: addRecord</code>.</p>\n\n<p>grid.js</p>\n\n<pre><code>...\ntext: 'Add',\nhandler: function() {\n    rowEditing.cancelEdit();\n    // create a record\n    var newRecord = <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('PersonalInfo');\n\n    // insert into store and start editing that record\n    store.insert(0, newRecord);\n    rowEditing.startEdit(0, 0);\n\n    // get the selection model in order to get which record is selected\n    var sm = grid.getSelectionModel();\n\n    // after user clicks off from editing, sync the store, remove the record from the top and reload the store to see new changes\n    grid.on('edit', function() {\n        var record = sm.getSelection()\n        store.sync();\n        store.remove(record);\n        store.load();\n    });\n}\n</code></pre>\n\n<p>This anonymous function first cancels editing if another record is being edited, and then creates a new record from our PersonalInfo model, this inserts blank values for our name, address and state fields. We then insert this 'phantom' record into the store using the insert method and then tell the row editor to start editing the top record - where we inserted the record. Once we've finished editing it, the edit event is called, to which we've added a function that'll get the internal ID of the record we just added, sync the new record so it's no longer a phantom. Left like this, it leaves a blank line at the top of our grid so we remove the previously selected record and then reload the store so our newly added record appears at the bottom of our grid. Still following? Good.</p>\n\n<p>As wonderful as Ext is, it can't deal with the server side stuff for us so opening our QueryDatabase.php file we're going to create a new function that will insert records into the database.</p>\n\n<p>QueryDatabase.php</p>\n\n<pre><code>...\npublic function createRecord(stdClass $params)\n{\n    $_db = $this-&gt;__construct();\n    if($stmt = $_db-&gt;prepare(\"INSERT INTO owners (name, address, state) VALUES (?, ?, ?)\")) {\n        $stmt-&gt;bind_param('sss', $name, $address, $state);\n\n        $name = $_db-&gt;real_escape_string($params-&gt;name);\n        $address = $_db-&gt;real_escape_string($params-&gt;address);\n        $state = $_db-&gt;real_escape_string($params-&gt;state);\n\n        $stmt-&gt;execute();\n        $stmt-&gt;close();\n    }\n\n    return $this;\n}\n</code></pre>\n\n<p>Our function name corresponds to the name we set when declaring our API in grid.js, i.e. createRecord, we then say that 'data that comes from the class stdClass (from router.php) will be assigned to a variable called params, this is for added security so attacks can't be spoofed from another file. The data in question looks like this:</p>\n\n<p><p><img src=\"guides/direct_grid_pt2/creating-record.png\" alt=\"Firebug showing the JSON that contains the data to create a record\"></p></p>\n\n<p>This clearly shows us which class and method is being invoked and includes a JSON data object that we access to get the individual fields for our database. The <code>tid</code> is the number of POST requests in this session, so this is the second (the first being when it loaded the data).</p>\n\n<p>We then prepare our MySQL statement as we did before. The question marks are linked to the next line, where we bind parameters, the strange looking 'sss' denotes that there are three variables that are all strings which we then map afterwards, making sure to escape input into our database as one last security measure before executing our query.</p>\n\n<h3>3.7 Deleting</h3>\n\n<p>Destruction is always easier than creation and this next section will teach you how to be an Ext Shiva. We already have a button to add so now we're going to do the same process to delete, complete with a handler.</p>\n\n<p>grid.js</p>\n\n<pre><code>...\n}, {\niconCls: 'delete',\ntext: 'Delete',\nhandler: function() {\n    rowEditing.cancelEdit();\n    var sm = grid.getSelectionModel();\n    store.remove(sm.getSelection());\n    store.sync();\n}\n</code></pre>\n\n<p>We've seen the first two parts before with adding so I'm going to jump straight to what we're doing with the handler. We get rid of the editing overlay if it's open, get which record is being selected in the grid, and then remove the row from the store (using the selection model) and finally sync everything up so our database is up-to-date with our store.</p>\n\n<h3>3.8 UX: Bare Buttons and Bad Behavior</h3>\n\n<p>Noticed that the buttons are looking a bit bare? To add an icon we're going to write some old school CSS. The classes aren't arbitrary, when coding our buttons we added an <code>iconCls</code> to each one, this adds the class name that we're now using in the CSS. I've also positioned the grid so that it's no longer tucked up in the top left corner.</p>\n\n<p>style.css</p>\n\n<pre><code>body {\n    background: #ededed;\n}\n\n.grid {\n    margin: 0 auto;\n    position: relative;\n    width: 700px;\n    margin-top: 5px;\n}\n\n.add {\n    background: url('images/add.png');\n}\n\n.delete {\n    background: url('images/delete.png');\n}\n</code></pre>\n\n<p>Of course, we also have to link it in our index.html like so</p>\n\n<p>index.html</p>\n\n<pre><code>...\n&lt;link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\"&gt;\n</code></pre>\n\n<p>As long as it's beneath the Ext CSS file and in the <code>&lt;head&gt;</code> tag it doesn't matter where you place it.</p>\n\n<p>Something else you may have noticed is how easy it is to delete a record forever and how easily this might be done by mistake. To be less rude to our users we're going to add a dialog box to confirm deletions.</p>\n\n<p>Dealing with confirming deletions isn't hard at all. First, we want to find the part of our code that deals with deleting things - our handler for the delete button. Second, we want to split the handler into two parts, destructive and non-destructive behavior. The first two lines are non-destructive so I've left them at the top so they get run as soon as the user clicks Delete, but we only want to remove and sync when they <em>confirm</em> that that's what they want to do.</p>\n\n<p>We invoke <code><a href=\"#!/api/Ext.MessageBox-method-show\" rel=\"Ext.MessageBox-method-show\" class=\"docClass\">Ext.Msg.show</a></code> with some configuration options. The title and message are self-explanatory, the buttons option dictates what buttons the user will be able to click on, in this case, Yes or No. We've also added an icon to the dialog so that the user knows immediately that an action is needed from them to continue. When the user does decide on one of the options and clicks the corresponding button we can check which one was chosen by supplying a callback function with <code>fn</code>. This is shorthand for function (no surprises) and works the same way as a handler on our Add and Delete buttons where we simply check if they said yes (always in lowercase) and if so, carry out what needs to be done. If we were being <em>really</em> nice we could add an <code>else</code> and resume their editing where they left off.</p>\n\n<p>grid.js</p>\n\n<pre><code>  ...\n  handler: function() {\n      rowEditing.cancelEdit();\n      var sm = grid.getSelectionModel();\n\n      <a href=\"#!/api/Ext.MessageBox-method-show\" rel=\"Ext.MessageBox-method-show\" class=\"docClass\">Ext.Msg.show</a>({\n           title:'Delete Record?',\n           msg: 'You are deleting a record permanently, this cannot be undone. Proceed?',\n           buttons: <a href=\"#!/api/Ext.MessageBox-property-YESNO\" rel=\"Ext.MessageBox-property-YESNO\" class=\"docClass\">Ext.Msg.YESNO</a>,\n           icon: <a href=\"#!/api/Ext.MessageBox-property-QUESTION\" rel=\"Ext.MessageBox-property-QUESTION\" class=\"docClass\">Ext.Msg.QUESTION</a>,\n           fn: function(btn){\n            if (btn === 'yes'){\n                store.remove(sm.getSelection());\n                store.sync();\n            }\n           }\n      });\n  }\n</code></pre>\n\n<p>Voila, now we have a nice, user friendly message that confirms their actions.</p>\n\n<h2 id='direct_grid_pt2-section-4'>IV. Conclusion</h2>\n\n<p>In this tutorial we've covered a lot of ground. You should now know how to implement Ext Direct to create, read, update and delete from a database. We've also looked at how easy Ext makes dialogs and alerts that have a direct impact on the application and create a better user experience overall.</p>\n\n<p>If you're going to integrate this into production code, you could look into how to optimize it using <a href=\"http://www.sencha.com/blog/using-ext-loader-for-your-application/\">Ext Loader to only load the classes that we use</a> or process actions in batches with a 'Save changes' button so permanent changes aren't immediate.</p>\n\n<p>Finally, for reference, you can find the <a href=\"guides/direct_grid_pt1/reference-files.zip\">completed source files here</a>.</p>\n","title":"Ext Direct and Grid Part 2"});