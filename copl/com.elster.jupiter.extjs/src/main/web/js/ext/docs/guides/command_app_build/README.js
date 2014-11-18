Ext.data.JsonP.command_app_build({"guide":"<h1>Inside The App Build Process</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/command_app_build-section-1'>Prerequisites</a></li>\n<li><a href='#!/guide/command_app_build-section-2'>Introduction</a></li>\n<li><a href='#!/guide/command_app_build-section-3'>Build Targets</a></li>\n<li><a href='#!/guide/command_app_build-section-4'>Configuration</a></li>\n<li><a href='#!/guide/command_app_build-section-5'>Customization</a></li>\n<li><a href='#!/guide/command_app_build-section-6'>Next Steps</a></li>\n</ol>\n</div>\n\n<p>The build script provided by <a href=\"http://www.sencha.com/products/sencha-cmd/\">Sencha Cmd</a> is\nthe component that ties together and automates the many low-level features of Sencha Cmd\n(such as the Compiler). There is rarely a one-size-fits-all solution for builds so the\nbuild script provides many options to configure and customize its behavior. This guide will\nexplain the principles behind the build script and where you might look should you need to\ntailor the build process to suit your needs.</p>\n\n<h2 id='command_app_build-section-1'>Prerequisites</h2>\n\n<p>The following guides are recommended reading before proceeding further:</p>\n\n<ul>\n<li><a href=\"#!/guide/command\">Introduction to Sencha Cmd</a>.</li>\n<li><a href=\"#!/guide/command_app\">Using Sencha Cmd</a>.</li>\n</ul>\n\n\n<h2 id='command_app_build-section-2'>Introduction</h2>\n\n<p>Internally, the <code>sencha app build</code> command does basic validation and calls in to the\n<a href=\"http://ant.apache.org/\">Apache Ant</a> build script found in <code>\"build.xml\"</code> at the root of\nthe application. Specifically, it calls the <code>\"build\"</code> target of this script. This means\nthe entire build process can be examined, extended and (if necessary) even modified.</p>\n\n<p>Because <code>sencha app build</code> simply invokes the <code>\"build\"</code> target of the Ant <code>\"build.xml\"</code>\nfile, you can equivalently invoke a build directly from Ant. This can be useful in IDE's\nlike Eclipse and NetBeans for invoking your builds but also in a Continuous Integration\nserver that understands Ant (which is just about all of them).</p>\n\n<p>The generated <code>\"build.xml\"</code> is a minimal Ant script that uses an Ant <code>import</code> task to\nimport <code>\".sencha/app/build-impl.xml\"</code> as well as several other files. While <code>\"build.xml\"</code>\nis intended to be edited after it is generated, the <code>\".sencha/app/*-impl.xml\" files are\nnot. These files will be replaced by</code>sencha app upgrade` and so should be edited unless\nabsolutely necessary. These files are, however, an excellent  reference but should be modified\nunless necessary.</p>\n\n<h2 id='command_app_build-section-3'>Build Targets</h2>\n\n<p>The targets below define the application build process from start to finish. With the\nexception of <code>init</code> each target has a property that can be set to 1 to disable that step.</p>\n\n<ul>\n<li>init</li>\n<li>refresh</li>\n<li>resolve (defaults to 1; set skip.resolve=0 to enable)</li>\n<li>js</li>\n<li>resources</li>\n<li>sass</li>\n<li>slice</li>\n<li>page</li>\n<li>native-package</li>\n</ul>\n\n\n<p>With the exception of <code>init</code> each of these targets can be dropped out of the default build\nby use of a build property (see below) with the target name prefixed by <code>\"skip.\"</code>. For\nexample, to disable the <code>slice</code> target:</p>\n\n<pre><code>skip.slice=1\n</code></pre>\n\n<p>These steps can also be run individually if only that piece is desired. This is often the\nuseful for rebuilding only the Sass:</p>\n\n<pre><code>sencha ant sass\n</code></pre>\n\n<p><em>Note:</em> Using <code>sencha ant</code> is equivalent to using your own version of Ant if you have Ant\n1.8 or higher installed.</p>\n\n<h2 id='command_app_build-section-4'>Configuration</h2>\n\n<p>Most aspects of the build script behind <code>sencha app build</code> are controlled by properties as\nis typical of Ant. In this case there are two kinds of properties: configuration properties\nand build properties.</p>\n\n<h3>Configuration Properties</h3>\n\n<p>Sencha Cmd configuration properties are available to the build script but also drive many\nother features of Sencha Cmd (like the compiler). To see the current set of configuration\nproperties, run this command:</p>\n\n<pre><code>sencha diag show\n</code></pre>\n\n<p>In most cases you can tell where each property comes from by its prefix:</p>\n\n<ul>\n<li><code>app.</code>  -- See <code>\"app.json\"</code> and <code>\".sencha/app/sencha.cfg\"</code>.</li>\n<li><code>workspace.</code> -- See <code>\"workspace.json\"</code> and <code>\".sencha/workspace/sencha.cfg\"</code>.</li>\n<li><code>framework.</code> -- See <code>\"cmd/sencha.cfg\"</code> in the Ext JS or Sencha Touch SDK.</li>\n<li><code>cmd.</code> -- See <code>\"sencha.cfg\"</code> in the Sencha Cmd install folder.</li>\n</ul>\n\n\n<p>The use of configuration properties is much broader than the build process and is discussed\nin some detail in <a href=\"#!/guide/command_advanced\">Advanced Sencha Cmd</a>.</p>\n\n<h3>Build Properties</h3>\n\n<p>The build script defines many other properties that are specific to builds. These build\nproperties are typically prefixed by <code>\"build.\"</code>.</p>\n\n<p>To see the current values of these you can run this command from your app folder:</p>\n\n<pre><code>sencha ant .props\n</code></pre>\n\n<h4>Setting Build Properties</h4>\n\n<p>There are many ways to configure build properties. The simplest way is to edit one of the\nbuild properties files. To decide which file to edit it is helpful to know the priority of\neach of these files and under what conditions they are loaded.</p>\n\n<ul>\n<li><code>\"local.properties\"</code> -- If present, this file is loaded first. This file is intended\nto be applied only locally (to the local machine). It should not be committed to source\ncontrol to be used by others. These settings take priority over any properties defined\nin other properties files as well as the current configuration properties.</li>\n<li><em>Sencha Cmd configuration properties</em></li>\n<li><code>\".sencha/app/${build.environment}.properties\"</code> -- Based on the value of the\n<code>build.environment</code> properties, exactly one of the following will be loaded. Setting\nproperties in these files allow you to have different values for properties based on\nthe type of build being run.\n\n<ul>\n<li><code>\".sencha/app/native.properties\"</code></li>\n<li><code>\".sencha/app/package.properties\"</code></li>\n<li><code>\".sencha/app/production.properties\"</code></li>\n<li><code>\".sencha/app/testing.properties\"</code></li>\n</ul>\n</li>\n<li><code>\".sencha/app/build.properties\"</code> -- These properties are loaded next and have lower\npriority over the build-environment-specific properties. These are properties that are\nused by all (or most) environments. This file is intended for customization.</li>\n<li><code>\".sencha/app/defaults.properties\"</code> -- These properties are the last (default) values\nto load. This file is \"owned\" by Sencha Cmd and will be updated each release as new\nproperties are added. This file serves as a reference for the defined set of build\nproperties but should not be edited; edit any of the other files instead.</li>\n</ul>\n\n\n<p>The only properties that have higher priority than <code>\"local.properties\"</code> are those passed\nin via the command line.</p>\n\n<h2 id='command_app_build-section-5'>Customization</h2>\n\n<p>Many common needs are accounted for via build properties, but it is impossible to account\nfor all use cases in this way. When configuration options cannot accomplish the task, the\nnext level of customization is to extend the generated <code>\"build.xml\"</code> Ant script.</p>\n\n<p>In addition to the <code>import</code> task, <code>\"build.xml\"</code> contains a comment block describing many of\nthe available extension points. These are in the form of optional Ant targets and are named\nafter their build process step but with prefixes of <code>\"-before-\"</code> and <code>\"-after-\"</code>. The most\ncommon extensions point then are these:</p>\n\n<ul>\n<li>init\n\n<ul>\n<li>-before-init</li>\n<li>-after-init</li>\n</ul>\n</li>\n<li>refresh\n\n<ul>\n<li>-before-refresh</li>\n<li>-after-refresh</li>\n</ul>\n</li>\n<li>resolve\n\n<ul>\n<li>-before-resolve</li>\n<li>-after-resolve</li>\n</ul>\n</li>\n<li>js\n\n<ul>\n<li>-before-js</li>\n<li>-after-js</li>\n</ul>\n</li>\n<li>resources\n\n<ul>\n<li>-before-resources</li>\n<li>-after-resources</li>\n</ul>\n</li>\n<li>sass\n\n<ul>\n<li>-before-sass</li>\n<li>-after-sass</li>\n</ul>\n</li>\n<li>slice\n\n<ul>\n<li>-before-slice</li>\n<li>-after-slice</li>\n</ul>\n</li>\n<li>page\n\n<ul>\n<li>-before-page</li>\n<li>-after-page</li>\n</ul>\n</li>\n<li>native-package\n\n<ul>\n<li>-before-native-package</li>\n<li>-after-native-package</li>\n</ul>\n</li>\n</ul>\n\n\n<p>To perform additional processing before or after any build step, add an appropriately named\ntarget to <code>\"build.xml\"</code>. These targets will be invoked by <code>sencha app build</code>. These will also\nbe invoked if you use Ant to directly invoke a particular target.</p>\n\n<p>One common use of these extensions points is to post-process the build output in the\n<code>\"all-classes.js\"</code> file. Using a few predefined Ant tasks we can, for example, put a\ncopyright header on the generated file after it is generated:</p>\n\n<pre><code>&lt;target name=\"-after-page\"&gt;\n    &lt;tstamp&gt;\n        &lt;format property=\"THISYEAR\" pattern=\"yyyy\"/&gt;\n    &lt;/tstamp&gt;\n\n    &lt;!--\n    The build.classes.file property holds the full path to the \"all-classes.js\"\n    file so we use that variable rather than hard-code the name.\n    --&gt;\n    &lt;move file=\"${build.classes.file}\" tofile=\"${build.classes.file}.tmp\"/&gt;\n\n    &lt;concat destfile=\"${build.classes.file}\"&gt;\n        &lt;header filtering=\"no\" trimleading=\"yes\"&gt;\n/*\n * Copyright (C) ${THISYEAR}. All Rights Reserved.\n * My Company Name\n */\n        &lt;/header&gt;\n        &lt;fileset file=\"${build.classes.file}.tmp\"/&gt;\n    &lt;/concat&gt;\n\n    &lt;delete file=\"${build.classes.file}.tmp\" /&gt;\n&lt;/target&gt;\n</code></pre>\n\n<h2 id='command_app_build-section-6'>Next Steps</h2>\n\n<ul>\n<li><a href=\"#!/guide/command_compiler\">Sencha Compiler Reference</a></li>\n<li><a href=\"#!/guide/command_advanced\">Advanced Sencha Cmd</a></li>\n</ul>\n\n","title":"Inside The App Build Process"});