/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.data.JsonP.upgrade_42({"guide":"<h1>Ext JS 4.2 Upgrade Guide</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/upgrade_42-section-1'>ComboBox filters</a></li>\n<li><a href='#!/guide/upgrade_42-section-2'>Menus</a></li>\n<li><a href='#!/guide/upgrade_42-section-3'>Grids</a></li>\n<li><a href='#!/guide/upgrade_42-section-4'>MVC</a></li>\n<li><a href='#!/guide/upgrade_42-section-5'>Ext.Direct</a></li>\n<li><a href='#!/guide/upgrade_42-section-6'>Containers</a></li>\n<li><a href='#!/guide/upgrade_42-section-7'>Panel</a></li>\n<li><a href='#!/guide/upgrade_42-section-8'>Buttons and Tabs</a></li>\n<li><a href='#!/guide/upgrade_42-section-9'>Glyphs</a></li>\n<li><a href='#!/guide/upgrade_42-section-10'>Class System</a></li>\n<li><a href='#!/guide/upgrade_42-section-11'>RowExpander</a></li>\n</ol>\n</div>\n\n<p>This guide will assist developers migrating from Ext JS 4.1.x to 4.2. As always our goal\nwas to maintain API compatibility as much as possible. The areas where changes were needed\nare described here to help you upgrade to Ext JS 4.2 as easily as possible.</p>\n\n<h2 id='upgrade_42-section-1'>ComboBox filters</h2>\n\n<p>Keystroke filtering of the store in a ComboBox is now implemented using the standard\nStore filtering mechanism and is independent of any other filters an application may add\nto the store.</p>\n\n<h2 id='upgrade_42-section-2'>Menus</h2>\n\n<p>Menu icon elements are now <code>div</code> elements in order that they can display the new <code>glyph</code>\nconfiguration as well as background images. This means that any high-specificity rules\nwhich made use of the <code>img</code> tag name to impose a <code>background-image</code> will have to change\nto use the <code>div</code> tag name.</p>\n\n<h2 id='upgrade_42-section-3'>Grids</h2>\n\n<p>In addition to the new <code>bufferedrenderer</code> plugin (<code><a href=\"#!/api/Ext.grid.plugin.BufferedRenderer\" rel=\"Ext.grid.plugin.BufferedRenderer\" class=\"docClass\">Ext.grid.plugin.BufferedRenderer</a></code>),\nmany more grid features work together than in previous releases. If some combination of\nstandard grid <code>features</code> or <code>plugins</code> does not work together, please report it to us -\nit is probably a bug!</p>\n\n<p>A locking grid's View now aggregates the columns from both sides, and offers more of the\ninterface of a true grid view. In particular, the <code>getGridColumns</code> method has been added.</p>\n\n<p>Row editing on a locked grid now produces one row editor which spans both sides.</p>\n\n<h2 id='upgrade_42-section-4'>MVC</h2>\n\n<ul>\n<li>The <code><a href=\"#!/api/Ext.app.EventBus\" rel=\"Ext.app.EventBus\" class=\"docClass\">Ext.app.EventBus</a></code> is now a singleton, and is always available in the application.</li>\n<li><code><a href=\"#!/api/Ext.app.Controller\" rel=\"Ext.app.Controller\" class=\"docClass\">Ext.app.Controller</a></code> no longer depends on <code><a href=\"#!/api/Ext.app.Application\" rel=\"Ext.app.Application\" class=\"docClass\">Ext.app.Application</a></code> to do things, and can\nbe instantiated without bringing up the whole dependency tree.</li>\n<li>It is now possible to create your own Application class(es) extending from\n<code><a href=\"#!/api/Ext.app.Application\" rel=\"Ext.app.Application\" class=\"docClass\">Ext.app.Application</a></code>, and include custom logic in this class.</li>\n<li><code><a href=\"#!/api/Ext-method-application\" rel=\"Ext-method-application\" class=\"docClass\">Ext.application</a></code> when passed the application class name just instantiates it. If\npassed a config object like before, it will declare new <code><a href=\"#!/api/Ext.app.Application\" rel=\"Ext.app.Application\" class=\"docClass\">Ext.app.Application</a></code> class with\nthese config options, and instantiate it - same logic as with Application class.</li>\n<li>As the result of two items above, <code><a href=\"#!/api/Ext-method-application\" rel=\"Ext-method-application\" class=\"docClass\">Ext.application</a></code> no longer requires all of the\ndependency tree synchronously.</li>\n<li>It is now possible to explicitly declare all namespaces (project parts) in the\nApplication class, to avoid any kind of ambiguities when resolving dependencies.</li>\n<li><a href=\"#!/api/Ext.app.Application\" rel=\"Ext.app.Application\" class=\"docClass\">Ext.app.Application</a> now deals properly with its own ancestry and can be used as a\ntop-level Controller in the application.</li>\n<li>A new concept of \"event domains\" was introduced; it is now possible to fire and\nlisten to events not related to Components. Several domains are provided out of the box,\nand it's easy to add your own.</li>\n<li>You can pass method names to the control or listen methods on Controller.</li>\n</ul>\n\n\n<h2 id='upgrade_42-section-5'>Ext.Direct</h2>\n\n<ul>\n<li>Direct namespaces can be nested, i.e. if the server side declares a class\n<code>Foo.Bar.Baz</code> with methods <code>foo</code> and <code>bar</code>, resulting stub methods will be\n<code>Foo.Bar.Baz.foo</code> and <code>Foo.Bar.Baz.bar</code>.</li>\n<li>Direct method callback can be canceled by listening to <code>beforecallback</code> events and\nreturning <code>false</code> from event handler. This is true for both successful and failed calls,\ni.e. in case of server failures and such.</li>\n<li>Direct method accepts extra <code>options</code> Object as last parameter (after callback and\nscope), this object is applied to resulting <code><a href=\"#!/api/Ext.Ajax-method-request\" rel=\"Ext.Ajax-method-request\" class=\"docClass\">Ext.Ajax.request</a></code> parameters so it is now\npossible to set individual timeout and any other <code><a href=\"#!/api/Ext.Ajax-method-request\" rel=\"Ext.Ajax-method-request\" class=\"docClass\">Ext.Ajax.request</a></code> parameter per-method\ncall.</li>\n<li>The <code>options</code> object is passed back to the Direct method callback function, so it is\npossible to pass any kind of data from caller to the callback.</li>\n<li>When the <code>timeout</code> option is specified, the Direct method is dispatched immediately,\nbypassing request queue.</li>\n<li>PollProvider now accepts empty \"no events\" responses without breaking.</li>\n<li>Each Provider can now be configured to have its events relayed by <code><a href=\"#!/api/Ext.direct.Manager\" rel=\"Ext.direct.Manager\" class=\"docClass\">Ext.direct.Manager</a></code>,\nso these can be listened to in centralized fashion.</li>\n<li>Direct proxy resolves string method names on first call, not at construction time as\nin previous release. This allows for late binding and fits better with MVC and builds.</li>\n</ul>\n\n\n<h2 id='upgrade_42-section-6'>Containers</h2>\n\n<p>Since version 4.0, i.e., in the Classic Theme, border:false was inherited by logic in the\n<code><a href=\"#!/api/Ext.container.AbstractContainer\" rel=\"Ext.container.AbstractContainer\" class=\"docClass\">Ext.container.AbstractContainer</a></code> base class. This conflicted with the Neptune Theme\nrequirements and was removed. To restore this behavior, apply this override in your\napplication:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('Compat.container.AbstractContainer', {\n    override: '<a href=\"#!/api/Ext.container.AbstractContainer\" rel=\"Ext.container.AbstractContainer\" class=\"docClass\">Ext.container.AbstractContainer</a>',\n\n    onBeforeAdd : function(item) {\n        this.callParent(arguments);\n\n        // If the parent has no border, only use an explicitly defined border\n        if (this.border === false || this.border === 0) {\n            var b = item.border;\n            item.border = <a href=\"#!/api/Ext-method-isDefined\" rel=\"Ext-method-isDefined\" class=\"docClass\">Ext.isDefined</a>(b) &amp;&amp; b !== false &amp;&amp; b !== 0;\n        }\n    }\n});\n</code></pre>\n\n<h2 id='upgrade_42-section-7'>Panel</h2>\n\n<p>Panel dragging has been enhanced to allow for simple, portal-like dragging. To enable this\nmode on a draggable panels which are <code>floating: true</code>, add the new <code>simpleDrag: true</code>.</p>\n\n<p>This causes the Panel to use a simple <code>ComponentDragger</code> the same as <code>Windows</code> use so\nthat it simply makes the Panel mobile. It does not use the default Panel drag mechanism\nwhich uses the <code>Ext.dd</code> package (to allow the dragged Panel to interact with other\n<code>Ext.dd</code> instances).</p>\n\n<p>The default behavior is there to allow things like portal panel dragging to interact with\nportal containers.</p>\n\n<h3>Borders in Neptune</h3>\n\n<p>Neptune is a (mostly) borderless design. Even so, you can always use the <code>border</code> config\nto enable borders and <code>bodyBorder</code> to enable borders on the body element of a panel. This\nimplies there is some inherent border styling that is being enabled which is correct. As\nin the Classic theme, there are CSS classes added to suppress the borders, but unlike in\nClassic, the Neptune theme uses one CSS class to suppress the undesired borders.</p>\n\n<p>While only one class is added to any element, there are 15 different classes declared in\n<code>\"./packages/ext-theme-base/sass/src/layout/component/Dock.scss\"</code> for this purpose. If\nnone of these classes are added, then the element will present its natural border. The\nclasses that suppress the border edges are as follows:</p>\n\n<ul>\n<li>x-noborder-t</li>\n<li>x-noborder-r</li>\n<li>x-noborder-b</li>\n<li>x-noborder-l</li>\n<li>x-noborder-tl</li>\n<li>x-noborder-rl</li>\n<li>x-noborder-bl</li>\n<li>x-noborder-tr</li>\n<li>x-noborder-tb</li>\n<li>x-noborder-rb</li>\n<li>x-noborder-tbl</li>\n<li>x-noborder-trl</li>\n<li>x-noborder-trb</li>\n<li>x-noborder-rbl</li>\n<li>x-noborder-trbl</li>\n</ul>\n\n\n<p>Also in Neptune, the borders of the panel, its body and docked items are \"collapsed\" using\nanother class, again from another set of 15 classes found in\n<code>\"./packages/ext-theme-base/sass/etc/mixins/border-management.scss\"</code>:</p>\n\n<ul>\n<li>x-panel-default-outer-border-t</li>\n<li>x-panel-default-outer-border-r</li>\n<li>x-panel-default-outer-border-b</li>\n<li>x-panel-default-outer-border-l</li>\n<li>x-panel-default-outer-border-tl</li>\n<li>x-panel-default-outer-border-rl</li>\n<li>x-panel-default-outer-border-bl</li>\n<li>x-panel-default-outer-border-tr</li>\n<li>x-panel-default-outer-border-tb</li>\n<li>x-panel-default-outer-border-rb</li>\n<li>x-panel-default-outer-border-tbl</li>\n<li>x-panel-default-outer-border-trl</li>\n<li>x-panel-default-outer-border-trb</li>\n<li>x-panel-default-outer-border-rbl</li>\n<li>x-panel-default-outer-border-trbl</li>\n</ul>\n\n\n<p>Because this class has to impose the proper component and UI styling, these classes are\ngenerated for each component/UI combination using the <code>border-management</code> mixin. So the\n\"panel\" and \"default\" pieces of the above classes will vary for other components (such as\n\"window\") and UI names.</p>\n\n<h2 id='upgrade_42-section-8'>Buttons and Tabs</h2>\n\n<p>The <code><a href=\"#!/api/Ext.button.Button\" rel=\"Ext.button.Button\" class=\"docClass\">Ext.button.Button</a></code> has changed its rendering to no longer render a <code>button</code> element.\nThis (greatly) simplifies the styling required for IE and the resulting button layout code.\nThe appropriate Aria <code>role</code> is added for accessibility concerns, but all Buttons now render\nan <code>a</code> (anchor) tag.</p>\n\n<h2 id='upgrade_42-section-9'>Glyphs</h2>\n\n<p>Buttons, tabs, panel headers and menu items now support a <code>glyph</code> configuration that is\nsimilar to <code>iconCls</code> and <code>icon</code>. The <code>glyph</code> uses Web Fonts to convert a character in to\na scalable image. To take advantage of this, you will need to pick out your own glyphs and\nproduce your own <code>font-family</code>. See the <code><a href=\"#!/api/Ext.button.Button\" rel=\"Ext.button.Button\" class=\"docClass\">Ext.button.Button</a></code> class and its <code>glyph</code> config\nfor further details.</p>\n\n<h2 id='upgrade_42-section-10'>Class System</h2>\n\n<p>We have added a <code>callSuper</code> method for situations where you need to patch/replace a method\non the target class in an <code>override</code>. This method should only be needed when an <code>override</code>\nneeds to workaround the behavior of a specific method it is designed to replace so is not\nsomething to use in most cases. The typical use cases should still use <code>callParent</code>.</p>\n\n<h2 id='upgrade_42-section-11'>RowExpander</h2>\n\n<p><code><a href=\"#!/api/Ext.ux.RowExpander\" rel=\"Ext.ux.RowExpander\" class=\"docClass\">Ext.ux.RowExpander</a></code> has been officially promoted to be part of the core framework.  This class\nis now known as <code><a href=\"#!/api/Ext.grid.plugin.RowExpander\" rel=\"Ext.grid.plugin.RowExpander\" class=\"docClass\">Ext.grid.plugin.RowExpander</a></code>.  <code><a href=\"#!/api/Ext.ux.RowExpander\" rel=\"Ext.ux.RowExpander\" class=\"docClass\">Ext.ux.RowExpander</a></code> still exists as an empty\nstub that extends <code><a href=\"#!/api/Ext.grid.plugin.RowExpander\" rel=\"Ext.grid.plugin.RowExpander\" class=\"docClass\">Ext.grid.plugin.RowExpander</a></code> for backward compatibility reasons.</p>\n","title":"Upgrade 4.1 to 4.2"});