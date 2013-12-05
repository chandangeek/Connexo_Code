Ext.data.JsonP.command_app({"title":"Using Sencha Cmd with Ext JS","guide":"<h1>Using Sencha Cmd with Ext JS 4.2+</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/command_app-section-1'>Generating Your Application</a></li>\n<li><a href='#!/guide/command_app-section-2'>Building Your Application</a></li>\n<li><a href='#!/guide/command_app-section-3'>Extending Your Application</a></li>\n<li><a href='#!/guide/command_app-section-4'>Customizing The Build</a></li>\n<li><a href='#!/guide/command_app-section-5'>Upgrading Your Application</a></li>\n</ol>\n</div>\n\n<p>This guide walks through the process of using Sencha Cmd with Ext JS 4.2 applications\nstarting with the <code>sencha generate app</code> command and ending with a running application.</p>\n\n<p>This guide applies mostly to <em>new</em> single-page Ext JS applications. If you have an <em>existing</em>\nsingle-page application you might consider using Sencha Cmd to build an application\nskeleton based on this guide and then migrate your application code to this preferred\nstructure. This will provide you with the maximum leverage from Sencha Cmd. If this option\nis not right for your app, you can still use Sencha Cmd to help with many aspects of your\ndevelopment. For developing single-page applications with a custom folder structure, see\n<a href=\"#/guide/command_app_single\">Single-Page Ext JS Apps</a>.</p>\n\n<p>If you work with applications that have multiple pages, it will be helpful to start by\nlearning about the simple uses of Sencha Cmd described in this and the\n<a href=\"#/guide/command_app_single\">Single-Page Ext JS Apps</a> guide. For details on developing\nmore complex, multipage Ext JS applications, refer to\n<a href=\"#/guide/command_app_multi\">Multi-Page and Mixed Apps</a>.</p>\n\n<h2 id='command_app-section-1'>Generating Your Application</h2>\n\n<p>Our starting point is to generate an application skeleton. This is done using\nthe following command:</p>\n\n<pre><code>sencha generate app MyApp /path/to/MyApp\n</code></pre>\n\n<p><strong>Important.</strong> The above command must be able to determine the appropriate SDK. This can\nbe satisfied by either executing this command from a folder containing an extracted SDK\ndistribution or by using the <code>-sdk</code> switch like so:</p>\n\n<pre><code>sencha -sdk /path/to/SDK generate app MyApp /path/to/MyApp\n</code></pre>\n\n<p>The application files generated by this command should have the following structure:</p>\n\n<pre><code>.sencha/                    # Sencha-specific files (e.g. configuration)\n    app/                    # Application-specific content\n        sencha.cfg          # Application configuration file for Sencha Cmd\n        build-impl.xml      # Standard application build script\n        plugin.xml          # Application-level plugin for Sencha Cmd\n    workspace/              # Workspace-specific content (see below)\n        sencha.cfg          # Workspace configuration file for Sencha Cmd\n        plugin.xml          # Workspace-level plugin for Sencha Cmd\n\next/                        # A copy of the Ext JS SDK\n    cmd/                    # Framework-specific content for Sencha Cmd\n        sencha.cfg          # Framework configuration file for Sencha Cmd\n    packages/               # Framework supplied packages\n        ext-theme-classic/  # Ext JS Theme Package for Classic\n        ext-theme-neptune/  # Ext JS Theme Package for Neptune\n        ...                 # Other theme and locale packages\n    src/                    # The Ext JS source\n    ext-*.js                # Pre-compiled and bootstrap files\n    ...\n\nindex.html                  # The entry point to your application\napp.json                    # Application configuration\napp.js                      # Your application's initialization logic\napp/                        # Your application's source code in MVC structure\n    model/                  # Folder for application model classes.\n    store/                  # Folder for application stores\n    view/                   # Folder for application view classes.\n        Main.js             # The initial default View\n    controller/             # Folder for application controllers.\n        Main.js             # The initial default Controller\n\npackages/                   # Sencha Cmd packages\n\nbuild/                      # The folder where build output is placed.\n</code></pre>\n\n<p>There is no distinction between workspace and app content in a single-page application.\nThis distinction becomes important for multi-page applications as described in\n<a href=\"#/guide/command_app_multi\">Multi-page and Mixed Apps</a>.</p>\n\n<h2 id='command_app-section-2'>Building Your Application</h2>\n\n<p>All that is required to build your application is to run the following command:</p>\n\n<pre><code>sencha app build\n</code></pre>\n\n<p><strong>Important.</strong> In order to execute this command, the current directory <strong>must</strong> be the\ntop-level folder of your application (in this case, <code>\"/path/to/MyApp\"</code>).</p>\n\n<p>This command will build your markup page, JavaScript code, SASS and themes into the <code>\"build\"</code>\nfolder.</p>\n\n<h2 id='command_app-section-3'>Extending Your Application</h2>\n\n<p>The <code>sencha generate</code> command helps you quickly generate common MVC components such as\ncontrollers or models:</p>\n\n<pre><code>sencha help generate\n</code></pre>\n\n<p>You should see this:</p>\n\n<pre><code>Sencha Cmd v3.1.0.xxx\nsencha generate\n\nThis category contains code generators used to generate applications as well\nas add new classes to the application.\n\nCommands\n  * app - Generates a starter application\n  * controller - Generates a Controller for the current application\n  * form - Generates a Form for the current application (Sencha Touch Specific)\n  * model - Generates a Model for the current application\n  * package - Generates a starter package\n  * profile - Generates a Profile for the current application (Sencha Touch Specific)\n  * theme - Generates a theme page for slice operations (Ext JS Specific)\n  * view - Generates a View for the current application (Ext JS Specific)\n  * workspace - Initializes a multi-app workspace\n</code></pre>\n\n<p><strong>Important.</strong> In order to execute the commands discussed below, the current directory on\nthe console <strong>must</strong> be inside your application (in this case, \"/path/to/MyApp\").</p>\n\n<h3>Adding New Models</h3>\n\n<p>Adding a model to your application is done by making the <code>\"/path/to/MyApp\"</code> your current\ndirectory and running Sencha Cmd, like this:</p>\n\n<pre><code>cd /path/to/MyApp\nsencha generate model User id:int,name,email\n</code></pre>\n\n<p>This command adds a model class called <code>User</code> with the given 3 fields.</p>\n\n<h3>Adding New Controllers</h3>\n\n<p>Adding a controller is similar to adding a model:</p>\n\n<pre><code>cd /path/to/MyApp\nsencha generate controller Central\n</code></pre>\n\n<p>There are no other parameters in this case beyond the controller name.</p>\n\n<h3>Adding New Views</h3>\n\n<p>Adding a view to your application is also similar:</p>\n\n<pre><code>cd /path/to/MyApp\nsencha generate view SomeView\n</code></pre>\n\n<p>There are no other parameters in this case beyond the view name.</p>\n\n<h2 id='command_app-section-4'>Customizing The Build</h2>\n\n<p>There are various configuration options available in the <code>\".sencha/app/sencha.cfg\"</code> file. In\nthe case of a single-page application, it is best to ignore the <code>\".sencha/workspace\"</code>\nfolder, which also has a config file.</p>\n\n<p>When configuration options cannot accomplish the task, the next level of customization is\nto extend the generated \"build.xml\" <a href=\"http://ant.apache.org/\">Ant</a> script. All that the\n<code>sencha app build</code> command does inside Sencha Cmd itself is some basic validations before\ncalling in to the <code>\"build\"</code> target of this build script. This means the entire build process\ncan be examined, extended and if necessary, even modified.</p>\n\n<h3>The classpath</h3>\n\n<p>The <code>sencha app build</code> command knows where to find the source of your application due to\nthe <code>app.classpath</code> configuration value stored in <code>\".sencha/app/sencha.cfg\"</code>. By default,\nthis value is:</p>\n\n<pre><code>app.classpath=${app.dir}/app,${app.dir}/app.js\n</code></pre>\n\n<p>Adding directories to this comma-separated list informs the compiler where to find the\nsource code required to build the application.</p>\n\n<h3>Build Properties</h3>\n\n<p>Many aspects of the build performed by <code>sencha app build</code> are controlled by properties. To\nsee the current values of these you can run this command from your app folder:</p>\n\n<pre><code>sencha ant .props\n</code></pre>\n\n<p>To provide fine-grained control of your application, many different properties are used. In\nmost cases you can tell where this property comes from by its prefix:</p>\n\n<ul>\n<li><code>app.</code>  -- Check the <code>\".sencha/app/sencha.cfg\"</code> file</li>\n<li><code>build.</code>  -- Check the <code>\".sencha/app/build.properties\"</code> file</li>\n<li><code>workspace.</code> -- Check the <code>\".sencha/workspace/sencha.cfg\"</code> file</li>\n<li><code>framework.</code> -- Check the <code>\"cmd/sencha.cfg\"</code> file in the Ext JS SDK.</li>\n<li><code>cmd.</code> -- Check the Sencha Cmd install folder's <code>\"sencha.cfg\"</code> file.</li>\n</ul>\n\n\n<p>It is not required that these properties be set in these files, but it is the default and\nfollowing this convention will help you manage these settings. That said, there are times\nwhen a Workspace setting may need to be overridden by an application. To do this, the\n<code>workspace.foo</code> property must be set in <code>\".sencha/app/sencha.cfg\"</code> because that is the\napplication-level configuration file. When deviating from the convention, leaving behind\na comment would be advised.</p>\n\n<h3>Ant Extension Points</h3>\n\n<p>The generated <code>\"build.xml\"</code> <a href=\"http://ant.apache.org/\">Ant</a> script is a minimal Ant script that\nuses an Ant <code>import</code> task to import <code>\".sencha/app/build-impl.xml\"</code>. The <code>\"build.xml\"</code> file\nis intended to be edited after it is generated. The <code>\"build-impl.xml\"</code> file, however, will\nbe replaced by the <code>sencha app upgrade</code> command described below and is best left alone.</p>\n\n<p>In addition to the <code>import</code> task, <code>\"build.xml\"</code> contains a comment block describing all of\nthe various extension points it provides. These are in the form of optional Ant targets and\nare typically named after their build process step but with prefixes of <code>\"-before-\"</code> and\n<code>\"-after-\"</code>. In other words, the <code>\"sass\"</code> build step is wrapped by targets that will be invoked\nbefore and after the <code>\"sass\"</code> target named <code>\"-before-sass\"</code> and <code>\"-after-sass\"</code>.</p>\n\n<p>To perform additional processing before or after any build step, add an appropriately named\ntarget to <code>\"build.xml\"</code>. These targets will be invoked by <code>sencha app build</code>. These will also\nbe invoked if you use Ant to directly invoke a particular target.</p>\n\n<p>For example, build properties are loaded by the <code>init</code> target. This means that they can be\noverridden by setting them prior to that time (since this follows the Ant convention of\nfirst-write-wins). The most common place to do this is in the \"-before-init\" target of the\napplication's <code>\"build.xml\"</code>:</p>\n\n<pre><code>&lt;target name=\"-before-init\"&gt;\n    &lt;property name=\"foo\" value=\"42\"/&gt;\n&lt;/target&gt;\n</code></pre>\n\n<p><strong>Note.</strong> Because <code>sencha app build</code> simply invokes the <code>\"build\"</code> target of the Ant <code>\"build.xml\"</code>\nfile, you can equivalently invoke a build directly from Ant. This can be useful in IDE's\nlike Eclipse and NetBeans for invoking your builds but also in a Continuous Integration\nserver that understands Ant (which is just about all of them).</p>\n\n<h2 id='command_app-section-5'>Upgrading Your Application</h2>\n\n<p>Generate applications include two basic kinds of content relevant to Sencha Cmd: build\nscripts and the important content of the used Sencha SDK's. As such, you will occasionally\nneed to upgrade these pieces. You can do this with the <code>sencha app upgrade</code> command.</p>\n\n<h3>Preparing to Upgrade</h3>\n\n<p>When performing any bulk operation on your application source code, it is highly advisable\nto start with a \"clean\" state with respect to version control. That is, it is best to have\nno uncommitted changes before performing the upgrade. This way, you can easily review and\npossibly discard changes made by <code>sencha app upgrade</code> with minimum trouble.</p>\n\n<h3>Upgrading Just The Sencha Cmd Scaffold</h3>\n\n<p>To bring up a new version of Sencha Cmd with your application produced by a previous\nversion, you can run this command from inside your application:</p>\n\n<pre><code>sencha app upgrade --noframework\n</code></pre>\n\n<p>This will replace the content of <code>\".sencha\"</code> but will also regenerate <code>\"app.js\"</code> to pick\nup changes there. As well as a handful of other files.</p>\n\n<h3>Upgrading Frameworks</h3>\n\n<p>Since generated applications include their own copies of the SDK from which they were\noriginally generated, applications need to be updated to use a new version of the SDK.\nThe <code>sencha app upgrade</code> command will replace the old SDK copy with the new one:</p>\n\n<pre><code>sencha app upgrade ../downloads/ext-4.2.0\n</code></pre>\n\n<p>The above command points to the path to a downloaded and extracted SDK.</p>\n\n<p><strong>Important.</strong> Do not use the <code>-sdk</code> switch for this command as you would for the\n<code>sencha generate app</code> command. Instead use the command shown above.</p>\n\n<h3>Dealing With Merge Conflicts</h3>\n\n<p>In Sencha Cmd v3.1, the code generator incorporates a 3-way merge to best reconcile the\ncode it generates with the code it generated last time and the current state of the code\nas you may have edited it. This approach allows you to edit files (like <code>\"spp.js\"</code>) in\nmany ways so long as your changes don't overlap those that Sencha Cmd wants to make.</p>\n\n<p>The merge process follows this pseudo-code for <code>\"app.js\"</code> (as an example):</p>\n\n<pre><code>mv app.js app.js.$old\nregenerate last version to app.js.$base\ngenerate new version to app.js\ndiff3 app.js.$base app.js.$old app.js\n</code></pre>\n\n<p>In the ideal scenario, you won't notice this mechanism at work. There are situations,\nhowever, in which you may receive an error message telling you there was a \"Merge conflict\"\nand that you need to resolve this manually.</p>\n\n<p>In cases where the base version cannot be recreated, the <code>\".$old\"</code> file is left on disk\nand you can compare it to the current version. Or you can use your source control system\nto compare the current file to what was last committed.</p>\n\n<p>When the base version could be produced (the majority case), the <code>\".$old\"</code> file is deleted\nbecause the conflicts are annotated in the target file in the standard way:</p>\n\n<pre><code>&lt;&lt;&lt;&lt;&lt;&lt;&lt; Generated\n    // stuff that Sencha Cmd thinks belongs here\n=======\n    // stuff that you have changed which conflicts\n&gt;&gt;&gt;&gt;&gt;&gt;&gt; Custom\n</code></pre>\n\n<h3>Alternative Strategies</h3>\n\n<p>If you have heavily customized your application, it is sometimes simpler to just generate\na new application in a temporary location and start by copying its <code>\".sencha\"</code> folder to\nreplace your own.</p>\n\n<p>If you are using a workspace, you may need to copy the <code>\".sencha/workspace\"</code> folder from\nthe generated app to your workspace's <code>\".sencha\"</code> folder to replace the old version there.</p>\n\n<p><code>NOTE</code>: be careful to preserve any edits to <code>\"sencha.cfg\"</code> files you may still need.</p>\n"});