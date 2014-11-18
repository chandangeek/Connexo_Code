Ext.data.JsonP.mvc_pt3({"guide":"<h1>Architecting Your App in Ext JS 4, Part 3</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/mvc_pt3-section-1'>Getting References</a></li>\n<li><a href='#!/guide/mvc_pt3-section-2'>Referencing view instances</a></li>\n<li><a href='#!/guide/mvc_pt3-section-3'>Cascading your controller logic on application launch.</a></li>\n<li><a href='#!/guide/mvc_pt3-section-4'>Starting a new station</a></li>\n<li><a href='#!/guide/mvc_pt3-section-5'>Summary</a></li>\n</ol>\n</div>\n\n<p>In the previous series of articles <a href=\"#!/guide/mvc_pt1\">Part 1</a> and <a href=\"#!/guide/mvc_pt2\">Part 2</a>, we explored architecting a Pandora-style application using the new features of Ext JS 4. We started by applying the Model-View-Controller architecture to a complex UI that has multiple views, stores and models. We looked at the basic techniques of architecting your application, like controlling your views from Controllers, and firing application-wide events that controllers can listen to. In this part of the series, we will continue implementing controller logic inside of the application’s MVC architecture.</p>\n\n<h2 id='mvc_pt3-section-1'>Getting References</h2>\n\n<p>Before we continue implementing our application, we should review some of the more advanced functionality available in the Ext JS 4 MVC package. In the previous part of this series, we showed how you could automatically load <strong>stores</strong> and <strong>models</strong> in your application by adding them to the stores and models arrays in your <a href=\"#!/api/Ext-method-application\" rel=\"Ext-method-application\" class=\"docClass\">Ext.application</a> configuration. We also explained that an instance would be created for each store loaded in this way, giving it a storeId equal to its name.</p>\n\n<h3><code>app/Application.js</code></h3>\n\n<pre><code><a href=\"#!/api/Ext-method-application\" rel=\"Ext-method-application\" class=\"docClass\">Ext.application</a>({\n    ...\n    models: ['Station', 'Song'],\n    stores: ['Stations', 'RecentSongs', 'SearchResults']\n    ...\n});\n</code></pre>\n\n<p>In addition to loading and instantiating these classes, adding stores and models into these arrays also automatically creates getters for you. This is also the case for controllers and views. The stores, models, controllers and views configurations also exist in Controllers and work exactly the same way as they do in the Application instance. This means that in order to get a reference to the Stations store inside of the Station controller, all we need to do is add the store to the stores array.</p>\n\n<h3><code>app/controller/Station.js</code></h3>\n\n<pre><code>...\nstores: ['Stations'],\n...\n</code></pre>\n\n<p>Now we can get a reference to the Stations store from anywhere in the controller using the automatically generated getter named <code>getStationsStore</code>. The convention is straightforward and predictable:</p>\n\n<pre><code>views: ['StationsList'] // creates getter named 'getStationsListView' -&gt; returns reference to StationsList class\nmodels: ['Station']     // creates getter named 'getStationModel'     -&gt; returns reference to Station model class\ncontrollers: ['Song']   // creates getter named 'getSongController'   -&gt; returns the Song controller instance\nstores: ['Stations']    // creates getter named 'getStationsStore'    -&gt; returns the Stations store instance\n</code></pre>\n\n<p>It’s important to note that the getters for both views and models return a reference to the class (requiring you to instantiate your own instances), while the getters for stores and controllers return actual instances.</p>\n\n<h2 id='mvc_pt3-section-2'>Referencing view instances</h2>\n\n<p>In the previous section, we described how the stores, models, controllers and views configurations automatically create getters allowing you to easily retrieve references to them. The <code>getStationsListView</code> getter will return a reference to the view class. In our application flow, we would like to select the first item in our StationsList. In this case, we don’t want a reference to the view class; instead, we want a reference to the actual StationsList instance that is inside our viewport.</p>\n\n<p>In Ext JS 3, a very common approach to getting a reference to an existing component instance on the page was the <a href=\"#!/api/Ext-method-getCmp\" rel=\"Ext-method-getCmp\" class=\"docClass\">Ext.getCmp</a> method. While this method continues to work, it’s not the recommended method in Ext JS 4. Using <a href=\"#!/api/Ext-method-getCmp\" rel=\"Ext-method-getCmp\" class=\"docClass\">Ext.getCmp</a> requires you to give every component a unique ID in order to reference it in your application. In the new MVC package, we can put a reference to a view instance (component) inside of a controller by leveraging a new feature in Ext JS 4: <a href=\"#!/api/Ext.ComponentQuery\" rel=\"Ext.ComponentQuery\" class=\"docClass\">ComponentQuery</a>.</p>\n\n<h3><code>app/controller/Station.js</code></h3>\n\n<pre><code>...\nrefs: [{\n    // A component query\n    selector: 'viewport &gt; #west-region &gt; stationslist',\n    ref: 'stationsList'\n}]\n...\n</code></pre>\n\n<p>In the <code>refs</code> configuration, you can set up references to view instances. This allows you to retrieve and manipulate components on the page inside of your controller’s actions. To describe the component that you want to reference, you can use a ComponentQuery inside the selector property. The other required information inside of this object is the <code>ref</code> property. This will be used as part of the name of the getter that will be generated automatically for each item inside the refs array. For example, by defining <code>ref: 'stationsList'</code> (note the capital L), a getter will be generated on the controller called <code>getStationsList</code>. Alternatively, if you did not set up a reference inside your controller, you could continue to use <code><a href=\"#!/api/Ext-method-getCmp\" rel=\"Ext-method-getCmp\" class=\"docClass\">Ext.getCmp</a></code> inside of the controller actions. However, we discourage you from doing this because it forces you to manage unique component ID's in your project, often leading to problems as your project grows.</p>\n\n<p>It’s important to remember that these getters will be created independent of whether the view actually exists on the page. When you call the getter and the selector successfully matches a component on the page, it caches the result so that subsequent calls to the getter will be fast. However, when the selector doesn’t match any views on the page, the getter will return null. This means that if you have logic that depends on a view and there is a possibility that the view does not exist on the page yet, you need to add a check around your logic to ensure it only executes if the getter returned a result. In addition, if multiple components match the selector, only the first one will be returned. Thus, it’s good practice to make your selectors specific to the single view you wish to get. Lastly, when you destroy a component you are referencing, calls to the getter will start returning null again until there is another component matching the selector on the page.</p>\n\n<h2 id='mvc_pt3-section-3'>Cascading your controller logic on application launch.</h2>\n\n<p>When the application starts, we want to load the user’s existing stations. While you could put this logic inside of the application’s <code>onReady</code> method, the MVC architecture provides you with an <code>onLaunch</code> method which fires on each controller as soon as all the controllers, models and stores are instantiated, and your initial views are rendered. This provides you with a clean separation between global application logic and logic specific to a controller.</p>\n\n<h3>Step 1</h3>\n\n<h3><code>app/controller/Station.js</code></h3>\n\n<pre><code>...\nonLaunch: function() {\n    // Use the automatically generated getter to get the store\n    var stationsStore = this.getStationsStore();\n    stationsStore.load({\n        callback: this.onStationsLoad,\n        scope: this\n    });\n}\n...\n</code></pre>\n\n<p>The onLaunch method of the Station controller seems like the perfect place to call the Station store’s load method. As you can see, we have also set up a callback which gets executed as soon as our store is loaded.</p>\n\n<h3>Step 2</h3>\n\n<h3><code>app/controller/Station.js</code></h3>\n\n<pre><code>...\nonStationsLoad: function() {\n    var stationsList = this.getStationsList();\n    stationsList.getSelectionModel().select(0);\n}\n...\n</code></pre>\n\n<p>In this callback we get the StationsList instance using the automatically generated getter, and select the first item. This will trigger a <code>selectionchange</code> event on the StationsList.</p>\n\n<h3>Step 3</h3>\n\n<h3><code>app/controller/Station.js</code></h3>\n\n<pre><code>...\ninit: function() {\n    this.control({\n        'stationslist': {\n            selectionchange: this.onStationSelect\n        },\n        ...\n    });\n},\n\nonStationSelect: function(selModel, selection) {\n    this.application.fireEvent('stationstart', selection[0]);\n},\n...\n</code></pre>\n\n<p>Application events are extremely useful when you have many controllers in your application that are interested in an event. Instead of listening for the same view event in each of these controllers, only one controller will listen for the view event and fire an application-wide event that the others can listen for. This also allows controllers to communicate to one another without knowing about or depending on each other’s existence. In the <code>onStationSelect</code> action, we fire an application event called <code>stationstart</code>.</p>\n\n<h3>Step 4</h3>\n\n<h3><code>app/controller/Song.js</code></h3>\n\n<pre><code>...\nrefs: [{\n    ref: 'songInfo',\n    selector: 'songinfo'\n}, {\n    ref: 'recentlyPlayedScroller',\n    selector: 'recentlyplayedscroller'\n}],\n\nstores: ['RecentSongs'],\n\ninit: function() {\n    ...\n    // We listen for the application-wide stationstart event\n    this.application.on({\n        stationstart: this.onStationStart,\n        scope: this\n    });\n},\n\nonStationStart: function(station) {\n    var store = this.getRecentSongsStore();\n\n    store.load({\n        callback: this.onRecentSongsLoad,\n        params: {\n            station: station.get('id')\n        },\n        scope: this\n    });\n}\n...\n</code></pre>\n\n<p>As part of the init method of the Song controller, we have set up a listener to the <code>stationstart</code> application event. When this happens, we need to load the songs for this station into our RecentSongs store. We do this in the <code>onStationStart</code> method. We get a reference to the RecentSongs store and call the load method on it, defining the controller action that needs to get fired as soon as the loading has finished.</p>\n\n<h3>Step 5</h3>\n\n<h3><code>app/controller/Song.js</code></h3>\n\n<pre><code>...\nonRecentSongsLoad: function(songs, request) {\n    var store = this.getRecentSongsStore(),\n        selModel = this.getRecentlyPlayedScroller().getSelectionModel();\n\n    selModel.select(store.last());\n}\n...\n</code></pre>\n\n<p>When the songs for the station are loaded into the RecentSongs store, we select the last song in the RecentlyPlayedScroller. We do this by getting the selection model on the RecentlyPlayedScroller <code>dataview</code> and calling the select method on it, passing the last record in the RecentSongs store.</p>\n\n<h3>Step 6</h3>\n\n<h3><code>app/controller/Song.js</code></h3>\n\n<pre><code>...\ninit: function() {\n    this.control({\n        'recentlyplayedscroller': {\n            selectionchange: this.onSongSelect\n        }\n    });\n    ...\n},\n\nonSongSelect: function(selModel, selection) {\n    this.getSongInfo().update(selection[0]);\n}\n...\n</code></pre>\n\n<p>When we select the last song in the scroller, it will fire a <code>selectionchange</code> event. In the control method, we already set up a listener for this event; and in the onSongSelect method, we complete the application flow by updating the data in the SongInfo view.</p>\n\n<h2 id='mvc_pt3-section-4'>Starting a new station</h2>\n\n<p>Now, it becomes pretty easy to implement additional application flows. Adding logic to create and select a new station looks like:</p>\n\n<h3><code>app/controller/Station.js</code></h3>\n\n<pre><code>...\nrefs: [{\n    ref: 'stationsList',\n    selector: 'stationslist'\n}],\n\ninit: function() {\n    // Listen for the select event on the NewStation combobox\n    this.control({\n        ...\n        'newstation': {\n            select: this.onNewStationSelect\n        }\n    });\n},\n\nonNewStationSelect: function(field, selection) {\n    var selected = selection[0],\n        store = this.getStationsStore(),\n        list = this.getStationsList();\n\n    if (selected &amp;&amp; !store.getById(selected.get('id'))) {\n        // If the newly selected station does not exist in our station store we add it\n        store.add(selected);\n    }\n\n    // We select the station in the Station list\n    list.getSelectionModel().select(selected);\n}\n...\n</code></pre>\n\n<h2 id='mvc_pt3-section-5'>Summary</h2>\n\n<p>We have illustrated that by using some advanced controller techniques and keeping your logic separate from your views, the application’s architecture becomes easier to understand and maintain. At this stage, the application is already quite functional. We can search for and add new stations, and we can start stations by selecting them. Songs for the station will be loaded, and we show the song and artist information.</p>\n\n<p>We will continue to refine our application in the next part of this series, with the focus on styling and custom component creation.</p>\n\n<p><a href=\"guides/mvc_pt3/code.zip\">Download project files</a></p>\n","title":"App Architecture Part 3"});