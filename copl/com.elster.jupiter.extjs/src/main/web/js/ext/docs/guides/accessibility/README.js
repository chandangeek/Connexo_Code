/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.data.JsonP.accessibility({"guide":"<h1>Creating Accessible Ext JS Applications (Section 508 and ARIA)</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/accessibility-section-1'>Overview of Accessibility</a></li>\n<li><a href='#!/guide/accessibility-section-2'>What Accessibility Support Means for Ext JS</a></li>\n<li><a href='#!/guide/accessibility-section-3'>How Accessibility Support is implemented in Ext JS</a></li>\n<li><a href='#!/guide/accessibility-section-4'>Building an Accessible Ext JS Application</a></li>\n<li><a href='#!/guide/accessibility-section-5'>Creating Your Own ARIA Theme</a></li>\n<li><a href='#!/guide/accessibility-section-6'>Adding Accessibility Support to an Existing Application - Using Sencha Cmd</a></li>\n<li><a href='#!/guide/accessibility-section-7'>Adding Accessibility Support to an Existing Application - Without Sencha Cmd</a></li>\n</ol>\n</div>\n\n<p>Ext JS 4.2.1 makes it possible to create accessible JavaScript applications by providing\nthe tools that developers need to achieve <a href=\"http://www.section508.gov\">Section 508</a> and\n<a href=\"http://www.w3.org/WAI/intro/aria\">ARIA</a> compliance.  These brand new features make it\neasier than ever before for application developers to create user interfaces that are\nusable by people with disabilities and by those who use assistive technologies to navigate\nthe web.</p>\n\n<h2 id='accessibility-section-1'>Overview of Accessibility</h2>\n\n<p>What does it mean for a software application to be Accessible? In general accessibility means\nthat the functionality and content of an application is available to people with disabilities\nespecially the visually impaired, those who rely on a screen reader to use a computer, and\nthose who cannot use a mouse to navigate the screen.  In 1998, the United States Congress\npassed the \"<a href=\"http://www.section508.gov\">Section 508 Amendment to the Rehabilitation Act of 1973</a>\"\nmore commonly referred to as just \"Section 508\", requiring Federal agencies to make all\ninformation that is electronically available accessible to people with disabilities. Because\nof Section 508 accessibility is a requirement for anyone producing software applications\nthat will be used by U.S. government agencies, however, applications not designed for\ngovernment use may also benefit since accessibility features will enable them to reach a\nlarger number of users. Web applications can make significant steps toward achieving\ncompliance with Section 508 by following the guidelines spelled out in the\n<a href=\"http://www.w3.org/WAI/\">Web Accessibility Initiative</a>'s \"Accessible Rich Internet\nApplications Suite\", otherwise known as <a href=\"http://www.w3.org/WAI/intro/aria\">WAI-ARIA</a> or\njust \"ARIA\".</p>\n\n<h2 id='accessibility-section-2'>What Accessibility Support Means for Ext JS</h2>\n\n<p>Accessibility support in Ext JS is designed with three major goals in mind:</p>\n\n<ol>\n<li>Keyboard Navigation - Components should be fully navigable using only the keyboard with\nno mouse interaction required.</li>\n<li>Focus Management - The framework should provide a clear indication of the currently\nfocused element that changes as the focused element changes.</li>\n<li><p>DOM attributes - A Component's DOM elements should use attributes that provide semantic\ninformation regarding the elements' type, state, and description.  These attributes\nare used by screen readers to provide verbal cues to the user and can be categorized into\ntwo separate groups:</p>\n\n<ol type=\"a\">\n<li><p> <a href=\"http://www.w3.org/TR/wai-aria/roles\">ARIA Roles</a> are the main indicator of a\nComponent's or Element's type.  Roles are constant and do not change as the user\ninteracts with a Component.  The most commmonly used ARIA Roles in Ext JS are\n<a href=\"http://www.w3.org/TR/wai-aria/roles#widget_roles\">Widget Roles</a>, many of which\ndirectly correspond to Ext JS components.  Some examples of widget roles are:</p>\n\n<pre><code>- button\n- checkbox\n- tabpanel\n- menuitem\n- tooltip\n</code></pre>\n\n<ol type=\"a\">\n<li><a href=\"http://www.w3.org/TR/wai-aria/states_and_properties\">ARIA States and Properties</a>\nare DOM attributes that may change in response to user interaction or application state.\nAn example of an ARIA State is the <code>\"aria-checked\"</code> attribute that is applied to a\ncheckbox component when it is checked by the user.  An example of an ARIA Property is\nthe <code>\"aria-readonly\"</code> property of a form field which may be dynamically changed based\non validation or user input.</li>\n</ol>\n</li>\n</ol>\n</li>\n</ol>\n\n\n<h2 id='accessibility-section-3'>How Accessibility Support is implemented in Ext JS</h2>\n\n<p>Accessibility support in Ext JS is implemented in three major pieces:</p>\n\n<ol>\n<li>Core Framework Support - ARIA support in Ext JS is designed to be opt-in as much as\npossible, and so is implemented primarily usisng a separate package and theme.  However,\nsupport for most <a href=\"http://www.w3.org/TR/wai-aria/roles\">ARIA Roles</a>, especially Widget Roles\nis implemented in the core framework.  This is done because ARIA Roles need to be applied\nto Components at render time and so must be included in the Components'\n<code><a href=\"#!/api/Ext.Component-cfg-renderTpl\" rel=\"Ext.Component-cfg-renderTpl\" class=\"docClass\">renderTpl</a></code>.</li>\n<li>The <code>\"ext-aria\"</code> package - <code>\"ext-aria\"</code> is a separate <a href=\"#/guide/command_packages\">Sencha Cmd Package</a>\nthat provides improved keyboard navigation, focus management, and support for\n<a href=\"http://www.w3.org/TR/wai-aria/states_and_properties\">ARIA States and Properties</a>. It is\nusually not necessary to directly require the <code>\"ext-aria\"</code> package in an application because\nit is already required by the <code>\"ext-theme-aria\"</code> theme.</li>\n<li>The <code>\"ext-theme-aria\"</code> theme - A high-contrast theme makes applications easier for visually\nimpaired users to view.  <code>\"ext-theme-aria\"</code> can be used out of the box, or extended to\ncreate a customized look and feel.</li>\n</ol>\n\n\n<h2 id='accessibility-section-4'>Building an Accessible Ext JS Application</h2>\n\n<p>Lets start by building an new application from scratch that uses the Accessibility features\nof Ext JS 4.2.1.</p>\n\n<h3>Download the Ext JS SDK</h3>\n\n<p>The first step is to download the <a href=\"http://www.sencha.com/products/extjs/download/\">Ext JS SDK</a>.\nUnzip the SDK to a location of your choosing.  For this tutorial, we assume that you\nunzipped the SDK to your home directory: <code>\"~/extjs-4.2.1/\"</code>.</p>\n\n<h3>Install Sencha Cmd</h3>\n\n<p>To build an Ext JS Application with accessibility features enabled you need to have at least\nversion 3.1.2 of Sencha Cmd installed.  For installation instructions see:\n<a href=\"#/guide/command\">Introduction to Sencha Cmd</a>.</p>\n\n<h3>Create a Workspace</h3>\n\n<p>Now that you have Sencha Cmd and the Ext JS SDK installed, you are ready to begin building\nthe application.  From the command line, enter the following command, and replace\n<code>\"~/ext-4.2.1\"</code> with the path where you unzipped the Ext JS SDK.</p>\n\n<pre><code>sencha -sdk ~/ext-4.2.1 generate workspace my-workspace\n</code></pre>\n\n<p>This generates a Sencha Cmd workspace that will contain your application and copies the\nExt JS SDK into the workspaces <code>\"ext\"</code> directory.  For more information on workspaces see:\n<a href=\"#/guide/command_workspace\">Workspaces in Sencha Cmd</a>.</p>\n\n<h3>Generate the Application</h3>\n\n<p>Sencha Cmd makes generating an application easy. Navigate into the workspace you just\ncreated:</p>\n\n<pre><code>cd my-workspace\n</code></pre>\n\n<p>Then run:</p>\n\n<pre><code>sencha -sdk ext generate app MyAriaApp my-aria-app\n</code></pre>\n\n<p>This tells Sencha Cmd to generate an application named <code>\"MyAriaApp\"</code> in a directory named\n<code>\"my-aria-app\"</code> and to find the Ext JS SDK in  the workspace's <code>\"ext\"</code> directory.  You\ncan build the application by running the following command from the newly created\n<code>\"my-aria-app\"</code> directory:</p>\n\n<pre><code>sencha app build\n</code></pre>\n\n<p>After building the application you can run it by opening\n<code>\"my-workspace/build/MyAriaApp/production/index.html\"</code> in a browser.</p>\n\n<h3>Enabling Accessibility (ARIA) Support in the Application</h3>\n\n<p>The easiest way to enable ARIA support in your application is to use the <code>\"ext-theme-aria\"</code>\ntheme. To do this, find following line in <code>\"my-aria-app/.sencha/app/sencha.cfg\"</code>:</p>\n\n<pre><code>app.theme=ext-theme-classic\n</code></pre>\n\n<p>And replace it with this:</p>\n\n<pre><code>app.theme=ext-theme-aria\n</code></pre>\n\n<p>If you want to be able to run your app in development mode, you will need to refresh the\nbootstrap files now (for more info see <a href=\"#/guide/command_app_single\">Single-Page Ext JS Apps</a>):</p>\n\n<pre><code>sencha app refresh\n</code></pre>\n\n<p>Now build your app again by running the following command:</p>\n\n<pre><code>sencha app build\n</code></pre>\n\n<p>Run your app by opening the index.html page in a browser.  You should see an empty\napplication shell with a viewport and a few components that Sencha Cmd generaated for you:</p>\n\n<p><p><img src=\"guides/accessibility/generated-app.png\" alt=\"Generated Application\"></p></p>\n\n<h3>Navigating an ARIA-Enabled Ext JS Application</h3>\n\n<p>With ARIA support turned on, an Ext JS Application should be navigable using only the keyboard,\nwith no mouse input required.  The visual indicator of which component currently has focus\nis referred to as the \"focus frame\".  In <code>\"ext-theme-aria\"</code> the focus frame is rendered\nwith an orange border.  You will notice when you load the index page of the app you just\ncreated that this border appears around the edge of the <a href=\"#!/api/Ext.container.Viewport\" rel=\"Ext.container.Viewport\" class=\"docClass\">Viewport</a>.\nThis is because the Viewport is automatically recognized as the application's main container.\nIt is the starting point of all navigation and so receives focus by default when the page\nis loaded.  If your application does not use a Viewport, you need to set the <code>ariaRole</code>\nconfig of the top-level container in your application to <code>'application'</code>. For example:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-create\" rel=\"Ext-method-create\" class=\"docClass\">Ext.create</a>('<a href=\"#!/api/Ext.panel.Panel\" rel=\"Ext.panel.Panel\" class=\"docClass\">Ext.panel.Panel</a>', {\n    renderTo: <a href=\"#!/api/Ext-method-getBody\" rel=\"Ext-method-getBody\" class=\"docClass\">Ext.getBody</a>(),\n    title: 'Main Panel',\n    ariaRole: 'application',\n    ...\n});\n</code></pre>\n\n<p>In the application there is currently only one focusable Component - the Viewport.\nLet's add some more focusable Components so we can see how keyboard navigation works.\nModify <code>\"my-aria-app/app/view/Viewport.js\"</code> to contain the following code:</p>\n\n<pre><code><a href=\"#!/api/Ext-method-define\" rel=\"Ext-method-define\" class=\"docClass\">Ext.define</a>('MyAriaApp.view.Viewport', {\n    extend: '<a href=\"#!/api/Ext.container.Viewport\" rel=\"Ext.container.Viewport\" class=\"docClass\">Ext.container.Viewport</a>',\n    requires: [\n        '<a href=\"#!/api/Ext.layout.container.Border\" rel=\"Ext.layout.container.Border\" class=\"docClass\">Ext.layout.container.Border</a>'\n    ],\n\n    layout: 'border',\n\n    defaults: {\n        split: true\n    },\n\n    items: [{\n        region: 'west',\n        width: 200,\n        title: 'West Panel',\n        ariaRole: 'region',\n        items: [{\n            xtype: 'textfield'\n        }, {\n            xtype: 'textfield'\n        }, {\n            xtype: 'button',\n            text: 'Toggle Me',\n            enableToggle: true\n        }]\n    }, {\n        xtype: 'tabpanel',\n        region: 'center',\n        title: 'Center Panel',\n        ariaRole: 'region',\n        items: [{\n            title: 'Tab 1'\n        }, {\n            title: 'Tab 2'\n        }, {\n            title: 'Tab 3'\n        }]\n    }, {\n        region: 'east',\n        width: 200,\n        title: 'East Panel',\n        ariaRole: 'region'\n    }]\n});\n</code></pre>\n\n<p>Here we create a viewport that uses a <a href=\"#!/api/Ext.layout.container.Border\" rel=\"Ext.layout.container.Border\" class=\"docClass\">Border Layout</a>\nand has three child panels laid out as east, west, and center regions.  Each region is\nmade focusable and navigable via the keyboard by configuring it with an <code>ariaRole</code> of\n<code>'region'</code>.  The center panel is a <a href=\"#!/api/Ext.tab.Panel\" rel=\"Ext.tab.Panel\" class=\"docClass\">Tab Panel</a> and has three tabs.\nTabs are focusable by default and so no special code is needed to enable keyboard navigation.\nWe've added some <a href=\"#!/api/Ext.form.field.Text\" rel=\"Ext.form.field.Text\" class=\"docClass\">Text Fields</a> and a\n<a href=\"#!/api/Ext.button.Button-cfg-enableToggle\" rel=\"Ext.button.Button-cfg-enableToggle\" class=\"docClass\">Toggle Button</a> to the <code>\"west\"</code> region, and these\ncomponents are also focusable by default.</p>\n\n<p>Let's rebuild the application and view the result:</p>\n\n<pre><code>sencha app build\n</code></pre>\n\n<p><p><img src=\"guides/accessibility/app.png\" alt=\"ARIA Application\"></p></p>\n\n<p>By default the Viewport is the focused Component.  Press the enter key to navigate into\nThe Viewport and cycle through it's children (the west, center, and east regions) using\nthe tab key.  Try pressing the enter key while the west region is focused and using\nthe tab key to cycle through the items.  When the toggle button is focused use the enter\nor space key to toggle its pressed state.  You can move back out of the west region by\npressing the \"esc\" key.  To navigate the tabs, move the focus to the center panel and\npress \"enter\", then use the arrow keys to navigate the tabs and the enter key to activate\na tab.</p>\n\n<h3>Verifying that ARIA Attributes Have Been Applied</h3>\n\n<p>You can verify that the correct ARIA Roles, States and Properties have been applied to\ncomponents by inspecting the DOM using the development tools in your browser of choice.\nFor example, inspect the <a href=\"#!/api/Ext.button.Button\" rel=\"Ext.button.Button\" class=\"docClass\">Button</a> component in your app.  In\n<a href=\"https://developers.google.com/chrome-developer-tools/\">Chrome Developer Tools</a> the Button's\nmain \"A\" element looks something like this:</p>\n\n<p><p><img src=\"guides/accessibility/button-element.png\" alt=\"Button DOM Element\"></p></p>\n\n<p>Notice how it has the ARIA Role of \"button\" (<code>role=\"button\"</code>), and an ARIA State of\n<code>aria-pressed=\"false\"</code>.  If you toggle the button either by clicking it or by pressing\nthe space or enter key while the button is focused you should see the value of the\n<code>aria-pressed</code> attribute change to <code>\"true\"</code>.</p>\n\n<h2 id='accessibility-section-5'>Creating Your Own ARIA Theme</h2>\n\n<p>The best way to create a customized ARIA Theme is to create a theme package that extends\n<code>\"ext-theme-aria\"</code>.  For instructions on theme creation see the\n<a href=\"#/guide/theming\">Theming Guide</a>.  The <code>\"ext-theme-aria\"</code> theme automatically includes\nall of the required JavaScript overrides from the <code>\"ext-aria\"</code> package, and themes that\nextend <code>\"ext-theme-aria\"</code> will as well.</p>\n\n<p>If for some reason extending <code>\"ext-theme-aria\"</code> will not work for you, then you need to\nmake sure that you correctly require the <code>\"ext-aria\"</code> package, either in your theme, or in\nyour application.  This ensures that the JavaScript overrides from the <code>\"ext-aria\"</code>\npackage are included in your app, and is done by adding the following JSON property to\neither your custom theme package's <code>\"package.json\"</code> file, or your application's <code>\"app.json\"</code>\nfile.</p>\n\n<pre><code>\"requires\": [\n    \"ext-aria\"\n]\n</code></pre>\n\n<h2 id='accessibility-section-6'>Adding Accessibility Support to an Existing Application - Using Sencha Cmd</h2>\n\n<p>We've been over how to create a new Ext JS Application with ARIA support, but adding\nARIA support to an existing application is just as easy.  First make sure your application\nis using Ext JS 4.2.1 or later.  You can upgrade an app that uses an older 4.x version of\nthe framework by downloading the <a href=\"http://www.sencha.com/products/extjs/download/\">Ext JS SDK</a>\nand then running the following command from your application's root directory:</p>\n\n<pre><code>sencha app upgrade /path/to/sdk\n</code></pre>\n\n<p>Then modify the application's <code>\".sencha/app/sencha.cfg\"</code> file and make sure the\n<code>\"app.theme\"</code> property is set to <code>\"ext-theme-aria\"</code>:</p>\n\n<pre><code>app.theme=ext-theme-aria\n</code></pre>\n\n<p>Refresh your application's bootstrap files if you want to use development mode:</p>\n\n<pre><code>sencha app refresh\n</code></pre>\n\n<p>Then build your app by running the following command from the application's root directory:</p>\n\n<pre><code>sencha app build\n</code></pre>\n\n<p>You may also set the theme from the command line if, for example, you want to build\nyour application with multiple themes:</p>\n\n<pre><code>sencha config -prop app.theme=ext-theme-aria then app build\n</code></pre>\n\n<p>To enable keyboard navigation, add the appropriate <code>\"ariaRole\"</code> configs to your application's\nComponents as described above in the section on \"Navigating an ARIA-Enabled Ext JS Application\".</p>\n\n<h2 id='accessibility-section-7'>Adding Accessibility Support to an Existing Application - Without Sencha Cmd</h2>\n\n<p>You may find yourself in the position of maintaining an older Ext JS application that\ndoes not build using Sencha Cmd.  It is a beneficial to update these applications so that\nthey can build using Sencha Cmd; however, if using Sencha Cmd to build the app is not an\noption, the application can still use the ARIA features of Ext JS by including the \"all\"\nJavaScript and CSS files of the <code>\"ext-aria\"</code> package and the <code>\"ext-theme-aria\"</code> theme.</p>\n\n<p>To use the ARIA features you will need to upgrade your application to use at least\n<a href=\"http://www.sencha.com/products/extjs/download/\">Ext JS 4.2.1</a>.  The next step is to\ndownload the <code>\"ext-aria\"</code> and <code>\"ext-theme-aria\"</code> packages.  The easiest way to do this\nis using Sencha Cmd.  If you don't have Sencha Cmd 3.2.1 or later already installed,\nuse the instructions found in the <a href=\"#/guide/command\">Introduction to Sencha Cmd Guide</a>,\nThen from the command line, run the following two command from your application's root\ndirectory:</p>\n\n<pre><code>sencha pacakge extract -todir . ext-aria\nsencha package extract -todir . ext-theme-aria\n</code></pre>\n\n<p>You can change the directory where the packages are extracted to by replacing the \".\"\nin the <code>\"sencha package\"</code> command with the path to the directory where you want the packages\nto be extracted.  After running this command you should see the following  2 directories in\nyour application root directory or the directory you specified:</p>\n\n<ul>\n<li><code>\"ext-aria\"</code></li>\n<li><code>\"ext-theme-aria\"</code>.</li>\n</ul>\n\n\n<p>An older Ext JS application typically has an index.html page that has the following structure:</p>\n\n<pre><code>&lt;html&gt;\n&lt;head&gt;\n    &lt;title&gt;My Application&lt;/title&gt;\n\n    &lt;link rel=\"stylesheet\" type=\"text/css\" href=\"ext/resources/css/ext-all.css\"&gt;\n    &lt;script src=\"ext/ext-all.js\"&gt;&lt;/script&gt;\n    &lt;script src=\"app.js\"&gt;&lt;/script&gt;\n&lt;/head&gt;\n&lt;body&gt;&lt;/body&gt;\n&lt;/html&gt;\n</code></pre>\n\n<p>Replace the href value for the <code>\"ext-all.css\"</code> link tag with\n<code>\"ext-theme-aria/build/resources/ext-theme-aria-all.css\"</code>, and add a script tag that includes\n<code>\"ext-aria/build/ext-aria-debug.js\"</code> after <code>\"ext-all.js\"</code>.  In the end your upgraded\nindex.html page should look something like this:</p>\n\n<pre><code>&lt;html&gt;\n&lt;head&gt;\n    &lt;title&gt;My Application&lt;/title&gt;\n\n    &lt;link rel=\"stylesheet\" type=\"text/css\" href=\"ext-theme-aria/build/resources/ext-theme-aria-all.css\"&gt;\n    &lt;script src=\"ext/ext-all.js\"&gt;&lt;/script&gt;\n    &lt;script src=\"ext-aria/build/ext-aria.js\"&gt;&lt;/script&gt;\n    &lt;script src=\"app.js\"&gt;&lt;/script&gt;\n&lt;/head&gt;\n&lt;body&gt;&lt;/body&gt;\n&lt;/html&gt;\n</code></pre>\n\n<p>To enable keyboard navigation, add the appropriate <code>\"ariaRole\"</code> configs to your application's\nComponents as described above in the section on \"Navigating an ARIA-Enabled Ext JS Application\".</p>\n","title":"Accessibility"});