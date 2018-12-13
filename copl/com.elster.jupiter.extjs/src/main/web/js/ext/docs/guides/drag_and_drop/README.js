/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.data.JsonP.drag_and_drop({"guide":"<h1>Drag and Drop</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/drag_and_drop-section-1'>Defining drag and drop</a></li>\n<li><a href='#!/guide/drag_and_drop-section-2'>Organizing the drag and drop classes</a></li>\n<li><a href='#!/guide/drag_and_drop-section-3'>The task at hand</a></li>\n<li><a href='#!/guide/drag_and_drop-section-4'>Step 1: Starting with drag</a></li>\n<li><a href='#!/guide/drag_and_drop-section-5'>Peeking at how drag nodes are affected</a></li>\n<li><a href='#!/guide/drag_and_drop-section-6'>Step 2: Repairing an invalid drop</a></li>\n<li><a href='#!/guide/drag_and_drop-section-7'>Step 3: Configuring the drop targets</a></li>\n<li><a href='#!/guide/drag_and_drop-section-8'>Step 4: Completing the drop</a></li>\n<li><a href='#!/guide/drag_and_drop-section-9'>Step 5: Adding drop invitation</a></li>\n<li><a href='#!/guide/drag_and_drop-section-10'>It doesn't stop here</a></li>\n<li><a href='#!/guide/drag_and_drop-section-11'>Summary</a></li>\n</ol>\n</div>\n\n<hr />\n\n<p>One of the most powerful interaction design patterns available to developers is \"Drag and Drop.\" We utilize Drag and Drop without really giving it much thought - especially when its done right. Here are 5 easy steps to ensure an elegant implementation.</p>\n\n<h2 id='drag_and_drop-section-1'>Defining drag and drop</h2>\n\n<p>A drag operation, essentially, is a click gesture on some UI element while the mouse button is held down and the mouse is moved. A drop operation occurs when the mouse button is released after a drag operation. From a high level, drag and drop decisions can be summed up by the following flow chart.</p>\n\n<p><p><img src=\"guides/drag_and_drop/dnd_drag_logic.jpg\" alt=\"Ext Drag and Drop\"></p></p>\n\n<p>To speed up our development, Ext JS provides us with the Ext.dd classes to manage the basic decisions for us. In this guide, we will cover coding for the appearance and removal of the drop invitation, invalid drop repair and what happens when a successful drop occurs.</p>\n\n<h2 id='drag_and_drop-section-2'>Organizing the drag and drop classes</h2>\n\n<p>A first glance of the classes in the Ext.dd documentation might seem a bit intimidating.  But, if we take a quick moment to look at the classes, we see that they all stem from the DragDrop class and most can be categorized into Drag or Drop groups.  With a bit more time and digging, we can see that the classes can be further categorized into single node and multiple node drag or drop interactions.</p>\n\n<p><p><img src=\"guides/drag_and_drop/diagram.jpg\" alt=\"Ext Drag and Drop\"></p></p>\n\n<p>In order to learn about the basics of drag and drop we'll focus on applying single drag and drop interactions to DOM nodes.  To do this, we'll utilize the DD and DDTarget classes, which provide the base implementations for their respective drag and drop behaviors. However, we need to discuss what our objectives are before we can start implementing drag and drop.</p>\n\n<h2 id='drag_and_drop-section-3'>The task at hand</h2>\n\n<p>Lets say we've been asked to develop an application that will provide a rental car company the ability to place their cars and trucks in one of three states:  available, rented or in repair status.  The cars and trucks are only allowed to be placed in their respective \"available\" container.</p>\n\n<p><p><img src=\"guides/drag_and_drop/dnd_story.jpg\" alt=\"Ext Drag and Drop\"></p></p>\n\n<p>To get started, we must make the cars and trucks \"dragable\". For this, we'll use DD. We'll need to make the rented, repair and vehicle containers \"drop targets\".  For this we'll use DDTarget.  Lastly, we'll use different drag drop groups to help enforce the requirement that cars and trucks can only be dropped into their respective \"available\" containers. See the <a href=\"#!/example/dd/dnd_with_dom.html\">full example</a> to grab the whole HTML and images. Now we can begin coding by adding drag operations to the cars and trucks.</p>\n\n<h2 id='drag_and_drop-section-4'>Step 1: Starting with drag</h2>\n\n<p>To configure the vehicle DIVs elements as dragable, we'll need to obtain a list and loop through it to instantiate new instances of DD.  Here's how we do it.</p>\n\n<pre><code><a href=\"#!/api/Ext-method-onReady\" rel=\"Ext-method-onReady\" class=\"docClass\">Ext.onReady</a>(function() {\n    // Create an object that we'll use to implement and override drag behaviors a little later\n    var overrides = {};\n\n    // Configure the cars to be draggable\n    var carElements = <a href=\"#!/api/Ext-method-get\" rel=\"Ext-method-get\" class=\"docClass\">Ext.get</a>('cars').select('div');\n    <a href=\"#!/api/Ext-method-each\" rel=\"Ext-method-each\" class=\"docClass\">Ext.each</a>(carElements.elements, function(el) {\n        var dd = <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.dd.DD\" rel=\"Ext.dd.DD\" class=\"docClass\">Ext.dd.DD</a>', el, 'carsDDGroup', {\n            isTarget  : false\n        });\n        //Apply the overrides object to the newly created instance of DD\n        <a href=\"#!/api/Ext-method-apply\" rel=\"Ext-method-apply\" class=\"docClass\">Ext.apply</a>(dd, overrides);\n    });\n\n    var truckElements = <a href=\"#!/api/Ext-method-get\" rel=\"Ext-method-get\" class=\"docClass\">Ext.get</a>('trucks').select('div');\n    <a href=\"#!/api/Ext-method-each\" rel=\"Ext-method-each\" class=\"docClass\">Ext.each</a>(truckElements.elements, function(el) {\n        var dd = <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.dd.DD\" rel=\"Ext.dd.DD\" class=\"docClass\">Ext.dd.DD</a>', el, 'trucksDDGroup', {\n            isTarget  : false\n        });\n        <a href=\"#!/api/Ext-method-apply\" rel=\"Ext-method-apply\" class=\"docClass\">Ext.apply</a>(dd, overrides);\n    });\n});\n</code></pre>\n\n<p>All drag and drop classes are designed to be implemented by means of overriding its methods. That's why in the above code segment, we have create an empty object called overrides, which will be filled in later with overrides specific to the action we need. We get of list of car and truck elements by leveraging the DomQuery select method to query the cars container for all the child div elements. To make the cars and truck elements dragable, we create a new instance of DD, passing in the car or truck element to be dragged and the drag drop group that it is to participate in. Notice that the vehicle types have their own respective drag drop group. This will be important to remember later when we setup the rented and repair containers as drop targets. Also notice that we're applying the overrides object to the newly created instances of DD using <a href=\"#!/api/Ext-method-apply\" rel=\"Ext-method-apply\" class=\"docClass\">Ext.apply</a>., which is a handy way to add properties or methods to an existing object. Before we can continue with our implementation, we need to take a quick moment to analyze what happens when you drag an element on screen. With this understanding, the rest of the implementation will fall into place.</p>\n\n<h2 id='drag_and_drop-section-5'>Peeking at how drag nodes are affected</h2>\n\n<p>The first thing you'll notice when dragging the car or truck elements around is that they will stick wherever they are dropped. This is OK for now because we've just begun our implementation. What is important is to understand how the drag nodes are being affected. This will aid us in coding for the return to their original positions when they are dropped on anything that is a valid drop target, which is known as an \"invalid drop\". The below illustration uses FireBug's HTML inspection panel and highlights the changes being made by when a drag operation is applied to the Camaro element.</p>\n\n<p><p><img src=\"guides/drag_and_drop/dnd_inspection.jpg\" alt=\"Ext Drag and Drop\"></p></p>\n\n<p>While inspecting the drag element during a drag operation, we can see a style attribute added to the element with three CSS values populated: position, top and left. Further inspection reveals that the position attribute set to relative and top and left attributes updating while the node is being dragged around. After a the drag gesture completes, the style attribute remains along with the styles contained therein. This is what we have to clean up when we code for the repair of an invalid drop. Until we setup proper drop targets, all drop operations are considered invalid.</p>\n\n<h2 id='drag_and_drop-section-6'>Step 2: Repairing an invalid drop</h2>\n\n<p>The path of least resistance is to repair an invalid drop by reseting the style attribute that is applied during the drag operation. This means that the drag element would disappear from under the mouse and reappear where it originated and would be quite boring. To make it smoother, we'll use Ext.Fx to animate this action. Remember that the drag and drop classes were designed to have methods overridden. To implement repair, we'll need to override the b4StartDrag, onInvalidDrop and endDrag methods. Lets add the following methods to our overrides object above and we'll discuss what they are and do.</p>\n\n<pre><code>var overrides = {\n    // Called the instance the element is dragged.\n    b4StartDrag : function() {\n        // Cache the drag element\n        if (!this.el) {\n            this.el = <a href=\"#!/api/Ext-method-get\" rel=\"Ext-method-get\" class=\"docClass\">Ext.get</a>(this.getEl());\n        }\n\n        //Cache the original XY Coordinates of the element, we'll use this later.\n        this.originalXY = this.el.getXY();\n    },\n    // Called when element is dropped not anything other than a dropzone with the same ddgroup\n    onInvalidDrop : function() {\n        // Set a flag to invoke the animated repair\n        this.invalidDrop = true;\n    },\n    // Called when the drag operation completes\n    endDrag : function() {\n        // Invoke the animation if the invalidDrop flag is set to true\n        if (this.invalidDrop === true) {\n            // Remove the drop invitation\n            this.el.removeCls('dropOK');\n\n            // Create the animation configuration object\n            var animCfgObj = {\n                easing   : 'elasticOut',\n                duration : 1,\n                scope    : this,\n                callback : function() {\n                    // Remove the position attribute\n                    this.el.dom.style.position = '';\n                }\n            };\n\n            // Apply the repair animation\n            this.el.setXY(this.originalXY[0], this.originalXY[1], animCfgObj);\n            delete this.invalidDrop;\n        }\n    },\n</code></pre>\n\n<p>In the above code, we begin by overriding the b4StartDrag method, which is called the instant the drag element starts being dragged around screen and makes it an ideal place to cache the drag element and original XY coordinates - which we will use later on in this process. Next, we override onInvalidDrop, which is is called when a drag node is dropped on anything other than a drop target that is participating in the same drag drop group. This override simply sets a local invalidDrop property to true, which will be used in the next method. The last method we override is endDrag, which is called when the drag element is no longer being dragged around screen and the drag element is no longer being controlled by the mouse movements. This override will move the drag element back to its original X and Y position using animation. We configured the animation to use the elasticOut easing to provide a cool and fun bouncy effect at end of the animation.</p>\n\n<p><p><img src=\"guides/drag_and_drop/dnd_camaro_repair.jpg\" alt=\"Ext Drag and Drop\"></p></p>\n\n<p>OK, now we have the repair operation complete. In order for it to work on the drop invitation and valid drop operations, we need to setup the drop targets.</p>\n\n<h2 id='drag_and_drop-section-7'>Step 3: Configuring the drop targets</h2>\n\n<p>Our requirements dictate that we will allow cars and trucks to be in be dropped in the rented and repair containers as well as their respective original containers. To do this, we'll need to instantiate instances of the DDTarget class. Here's how its done.</p>\n\n<pre><code>// Instantiate instances of <a href=\"#!/api/Ext.dd.DDTarget\" rel=\"Ext.dd.DDTarget\" class=\"docClass\">Ext.dd.DDTarget</a> for the cars and trucks container\nvar carsDDTarget = <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.dd.DDTarget\" rel=\"Ext.dd.DDTarget\" class=\"docClass\">Ext.dd.DDTarget</a>', 'cars','carsDDGroup');\nvar trucksDDTarget = <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.dd.DDTarget\" rel=\"Ext.dd.DDTarget\" class=\"docClass\">Ext.dd.DDTarget</a>', 'trucks', 'trucksDDGroup');\n\n// Instantiate instnaces of DDTarget for the rented and repair drop target elements\nvar rentedDDTarget = <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.dd.DDTarget\" rel=\"Ext.dd.DDTarget\" class=\"docClass\">Ext.dd.DDTarget</a>', 'rented', 'carsDDGroup');\nvar repairDDTarget = <a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.dd.DDTarget\" rel=\"Ext.dd.DDTarget\" class=\"docClass\">Ext.dd.DDTarget</a>', 'repair', 'carsDDGroup');\n\n// Ensure that the rented and repair DDTargets will participate in the trucksDDGroup\nrentedDDTarget.addToGroup('trucksDDGroup');\nrepairDDTarget.addToGroup('trucksDDGroup');\n</code></pre>\n\n<p>In the above code snippet, we have setup drop targets for the cars, trucks, rented and repair elements. Notice that the cars container element only participates in the \"carsDDGroup\" and the trucks container element participates in the \"trucksDDGroup\". This helps enforce the requirement that cars and trucks can only be dropped in their originating container. Next, we instantiate instances DDTarget for the rented and repair elements. Initially, they are configured to only participate in the \"carsDDGroup\". In order to allow them to participate in the \"trucksDDGroup\", we have to add it by means of addToGroup. OK, now we've configured our drop targets. Lets see what happens when we drop the cars or trucks on a valid drop element.</p>\n\n<p><p><img src=\"guides/drag_and_drop/dnd_drops_no_onDragDrop.jpg\" alt=\"Ext Drag and Drop\"></p></p>\n\n<p>In exercising the drop targets, we see that the drag element stays exactly its dropped. That is, images can be dropped anywhere on a drop target and stay there. This means that our drop implementation is not complete. To complete it, we need to actually code for the \"complete drop\" operation, by means of another override for the instances of DD that we created some time ago.</p>\n\n<h2 id='drag_and_drop-section-8'>Step 4: Completing the drop</h2>\n\n<p>To complete the drop, we will need to actually drag the element from its parent element to the drop target element using DOM tools. This is accomplished by overriding the DD onDragDrop method. Add the following method to the overrides object.</p>\n\n<pre><code>var overrides = {\n    ...\n    // Called upon successful drop of an element on a DDTarget with the same\n    onDragDrop : function(evtObj, targetElId) {\n        // Wrap the drop target element with <a href=\"#!/api/Ext.dom.Element\" rel=\"Ext.dom.Element\" class=\"docClass\">Ext.Element</a>\n        var dropEl = <a href=\"#!/api/Ext-method-get\" rel=\"Ext-method-get\" class=\"docClass\">Ext.get</a>(targetElId);\n\n        // Perform the node move only if the drag element's\n        // parent is not the same as the drop target\n        if (this.el.dom.parentNode.id != targetElId) {\n\n            // Move the element\n            dropEl.appendChild(this.el);\n\n            // Remove the drag invitation\n            this.onDragOut(evtObj, targetElId);\n\n            // Clear the styles\n            this.el.dom.style.position ='';\n            this.el.dom.style.top = '';\n            this.el.dom.style.left = '';\n        }\n        else {\n            // This was an invalid drop, initiate a repair\n            this.onInvalidDrop();\n        }\n    },\n</code></pre>\n\n<p>In the above override, the drag element is moved to the drop target element, but only if it is not the same as the drag element's parent node. After the drag element is moved, the styles are cleared from it. If the drop element is the same as the drag element's parent, we ensure a repair operation occurs by calling this.onInvalidDrop.</p>\n\n<p><p><img src=\"guides/drag_and_drop/drag_and_drop_working_without_invitation.jpg\" alt=\"Ext Drag and Drop\"></p></p>\n\n<p>Upon a successful drop, the drag elements will now will be moved from their parent element to the drop target. How does the user know if they are hovering above a valid drop target? We'll give the user some visual feedback by configuring the drop invitation.</p>\n\n<h2 id='drag_and_drop-section-9'>Step 5: Adding drop invitation</h2>\n\n<p>In order to make drag and drop a bit more useful, we need to provide feedback to the user on whether or not a drop operation can successfully occur. This means that we'll have to override the onDragEnter and onDragOut methods Add these last two methods to the overrides object.</p>\n\n<pre><code>var overrides = {\n    ...\n    // Only called when the drag element is dragged over the a drop target with the same ddgroup\n    onDragEnter : function(evtObj, targetElId) {\n        // Colorize the drag target if the drag node's parent is not the same as the drop target\n        if (targetElId != this.el.dom.parentNode.id) {\n            this.el.addCls('dropOK');\n        }\n        else {\n            // Remove the invitation\n            this.onDragOut();\n        }\n    },\n    // Only called when element is dragged out of a dropzone with the same ddgroup\n    onDragOut : function(evtObj, targetElId) {\n        this.el.removeCls('dropOK');\n    }\n};\n</code></pre>\n\n<p>In the above code, we override the onDragEnter and onDragOut methods, both of which are only utilized when the drag element is interacting with a drop target participating in the same drag drop group. The onDragEnter method is only called when the mouse cursor first intersects the boundaries of a drop target while a drag item is in drag mode. Likewise, onDragOut is called when the mouse cursor is first dragged outside the boundaries of the drop target while in drag mode.</p>\n\n<p><p><img src=\"guides/drag_and_drop/drag_and_drop_working_with_invitation.jpg\" alt=\"Ext Drag and Drop\"></p></p>\n\n<p>By adding overrides to the onDragEnter and onDragOut methods we can see that the background of the drag element will turn green when the mouse cursor first intersects a valid drop target and will lose its green background when it leaves the drop target or is dropped. This completes our implementation of drag and drop with DOM elements.</p>\n\n<h2 id='drag_and_drop-section-10'>It doesn't stop here</h2>\n\n<p>Drag and drop can be a can be applied to mostly everything in the Ext JS framework. Here are a few examples that you can use to learn how to implement drag and drop with various widgets:</p>\n\n<ul>\n<li><a href=\"#!/example/dd/dnd_grid_to_grid.html\">GridPanel to GridPanel</a></li>\n<li><a href=\"#!/example/dd/dnd_grid_to_formpanel.html\">Grid to FormPanel</a></li>\n<li><a href=\"#!/example/dd/field-to-grid-dd.html\">Ext.form.Field to GridPanel cell</a></li>\n<li><a href=\"#!/example/dd/dragdropzones.html\">Use of DragZone and DropZone</a></li>\n<li><a href=\"#!/example/organizer/organizer.html\">DataView to TreePanel</a></li>\n</ul>\n\n\n<h2 id='drag_and_drop-section-11'>Summary</h2>\n\n<p>Today, we learned how to implement end to end drag and drop of DOM nodes using the first-level drag and drop implementation classes. From a high-level, we defined and discussed what drag and drop is and how to think about it in terms of the framework. We also learned that the drag and drop classes can be grouped by drag or drop behaviors and whether or not they support single or multiple drag or drop operations. While implementing this behavior, we illustrated that the dd classes help make some of the behavioral decisions, and that we are responsible for coding the end-behaviors. We hope you've enjoyed this thorough look at some fundamental drag and drop operations with DOM nodes. We look forward to bringing you more articles about this topic in the future.</p>\n\n<div><small class=\"author\">\n<p>Written by Jay Garcia</p>\n<p>Author of Ext JS in Action and Sencha Touch in Action, Jay Garcia has been an evangelist of Sencha-based JavaScript frameworks since 2006. Jay is also Co-Founder and CTO of Modus Create, a digital agency focused on leveraging top talent to develop high quality Sencha-based applications. Modus Create is a Sencha Premier partner.</p>\n</small></div>\n\n","title":"Drag and Drop"});