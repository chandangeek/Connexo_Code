Ext.data.JsonP.command_ant({"title":"Ant Integration","guide":"<h1>Ant Integration</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/command_ant-section-1'>x-sencha-init</a></li>\n<li><a href='#!/guide/command_ant-section-2'>x-sencha-command</a></li>\n<li><a href='#!/guide/command_ant-section-3'>x-extend-classpath</a></li>\n<li><a href='#!/guide/command_ant-section-4'>x-generate</a></li>\n<li><a href='#!/guide/command_ant-section-5'>x-compress-js</a></li>\n<li><a href='#!/guide/command_ant-section-6'>x-compress-css</a></li>\n<li><a href='#!/guide/command_ant-section-7'>x-strip-js</a></li>\n<li><a href='#!/guide/command_ant-section-8'>x-get-env</a></li>\n<li><a href='#!/guide/command_ant-section-9'>x-escape</a></li>\n</ol>\n</div>\n\n<p>In addition to the command line interface described in the <a href=\"#!/guide/command\">Getting Started</a>\nguide, <a href=\"http://www.sencha.com/products/sencha-cmd/download\">Sencha Cmd</a> also provides\ndirect interfaces for use in Ant.</p>\n\n<p><a href=\"http://ant.apache.org/\">Apache Ant</a> has long been a pillar of the Java development community,\nbut at its core, Ant is an XML-based, cross-platform scripting platform. We call it a\n\"platform\" rather than a \"language\" because Ant can easily incorporate libraries of code\nin JAR form, or you can use one of the many supported scripting languages as part of your\nAnt script.</p>\n\n<p>Ant can, of course, call other programs (like Sencha Cmd), passing arguments and\nprocessing exit codes, but what Ant is particularly good at is manipulating files. This\nis because Ant was designed for use with build scripts.</p>\n\n<p>As touched on in the <a href=\"#!/guide/command_advanced\">Advanced Sencha Cmd</a> guide, Sencha Cmd\nis delivered as a JAR file and it exposes its core functionality as an Ant Library (or\n<code>antlib</code>). The command line level of Sencha Cmd, as well as specific processing\nare implemented on top of this layer. So anything you can do in one, you can do in the\nother.</p>\n\n<p><p><img src=\"guides/command_ant/../command_advanced/sencha-command-diagram.png\" alt=\"\"></p></p>\n\n<p>If you are using Ant, it is better to interface to Sencha Cmd at this level rather than\nmake repeated calls through the command line interface.</p>\n\n<pre><code>&lt;taskdef resource=\"com/sencha/ant/antlib.xml\" \n         classpath=\"${cmd.dir}/sencha.jar\"/&gt;\n</code></pre>\n\n<p>When an Ant script executes using <code>sencha ant ...</code>, the <code>cmd.dir</code> property defines\non entry. Otherwise, the Ant script or the executing party must determine <code>cmd.dir</code> in an\nappropriate way for the local machine.</p>\n\n<h2 id='command_ant-section-1'>x-sencha-init</h2>\n\n<p>This task loads the configuration properties from <code>\"sencha.cfg\"</code> files based on the current\ndirectory. This is typically done by Ant scripts that require Sencha Cmd and are specific\nto builds of Sencha applications.</p>\n\n<pre><code>&lt;x-sencha-init/&gt;\n</code></pre>\n\n<p>This will also load any Ant tasks defined by any available Sencha Cmd \"extensions\" such as\n<code>x-compass-compile</code>.</p>\n\n<h2 id='command_ant-section-2'>x-sencha-command</h2>\n\n<p>This command is equivalent to the command line interface. The arguments are placed in the\nbody text of this tag, one argument per line. Spaces are trimmed at both ends, so indent\nlevel is not significant. A good use of indentation is to clarify the command structure,\nlike this:</p>\n\n<pre><code>&lt;x-sencha-command&gt;\n    compile\n        --classpath=app,sdk/src\n        page\n            --in=app/index.html\n            --out=build/index.html\n&lt;/x-sencha-command&gt;\n</code></pre>\n\n<p>Because each line is an argument, spaces are not special and should not be escaped or quoted.</p>\n\n<p>Ant properties are expanded, so the following (fairly conventional style) also works:</p>\n\n<pre><code>&lt;x-sencha-command&gt;\n    compile\n        --classpath=${app.dir},${sdk.dir}/src\n        page\n            --in=${app.dir}/index.html\n            --out=${build.dir}/index.html\n&lt;/x-sencha-command&gt;\n</code></pre>\n\n<p>Finally, comments are supported so you can document the command or disable parts temporarily\nwithout deleting them. Also, blank lines are skipped:</p>\n\n<pre><code>&lt;x-sencha-command&gt;\n    compile\n        # Include the app folder and the Cmd/src folder\n        --classpath=${app.dir},${sdk.dir}/src\n\n        # Turn off debugging (comment next line to leave debug enabled):\n        # --debug=false\n\n        page\n            # The application main page:\n            --in=${app.dir}/index.html\n\n            # The compiled page goes in build folder along with \"all-classes.js\":\n            --out=${build.dir}/index.html\n&lt;/x-sencha-command&gt;\n</code></pre>\n\n<h2 id='command_ant-section-3'>x-extend-classpath</h2>\n\n<p>This task extends the classpath of the current ClassLoader. This is sometimes necessary\nto include <code>\"sencha.jar\"</code> in the classpath but can be useful in other cases where an Ant\nscript is launched and the classpath must be extended dynamically.</p>\n\n<pre><code>    &lt;x-extend-classpath&gt;\n        &lt;jar path=\"${cmd.dir}/sencha.jar/&gt;\n    &lt;/x-extend-classpath&gt;\n</code></pre>\n\n<p>As many JAR's as needed can be listed.</p>\n\n<h2 id='command_ant-section-4'>x-generate</h2>\n\n<p>This task generates output from templates in two basic modes: <code>file</code> and <code>dir</code>. That is,\nthe template generator can be given a single source file or a source folder.</p>\n\n<h3>Templates</h3>\n\n<p>The name of the source file determines if it should be processed as a \"template\":</p>\n\n<ul>\n<li><code>.tpl</code> = <a href=\"http://docs.sencha.com/ext-js/4-2/#!/api/Ext.XTemplate\">XTemplate</a></li>\n</ul>\n\n\n<p>For example, <code>\"foo.js.tpl\"</code> would be used to generate <code>\"foo.js\"</code> using the XTemplate engine.</p>\n\n<h3>Merge Files</h3>\n\n<p>In cases where a file may need to be changed from its original generated content (i.e.,\nregenerate the target), the <code>\".merge\"</code> suffix is very helpful. The primary use case for\nthis is an application's <code>\"app.js\"</code> file.</p>\n\n<p>When processing a <code>\".merge\"</code> file, <code>x-generate</code> performs the following steps:</p>\n\n<ol>\n<li>Move the target file (for example, <code>\"app.js\"</code>) to the side (for example, as <code>\"app.js.$old\"</code>).</li>\n<li>Generate the new version of the file in the target location (for example, <code>\"app.js\"</code>).</li>\n<li>Using a data store, regenerate the base version (for example, <code>\"app.js.$base\"</code>). That is,\nthe version generated the last time.</li>\n<li>Perform a 3-way merge on these files and update the target file.</li>\n<li>Report any merge conflicts.</li>\n</ol>\n\n\n<p>It is often the case (as with <code>\"app.js\"</code>) that a <code>\".merge\"</code> file is also a <code>\".tpl\"</code>. In\nthe case of <code>\"app.js\"</code>, for example, the source file is <code>\"app.js.tpl.merge\"</code>.</p>\n\n<p>To enable this mode, <code>x-generate</code> must be given a <code>store</code> attribute which points at the\ndata store (a JSON file).</p>\n\n<h3>Sacred Files</h3>\n\n<p>When generating code, files fall into two basic categories: machine maintained and user\nmaintained. It is preferable, however, to provide a starter or skeleton file at initial\ngeneration even for files that will be ultimately user maintained.</p>\n\n<p>This is called a \"sacred\" file and is identified by the source file extension of <code>\".default\"</code>.\nThat is, the source file is just a default and will not replace an existing file.</p>\n\n<p>For example, one might want to generate a starter <code>\"readme.txt\"</code> file but preserve whatever\nthe user might have changed during a later regeneration. To do so, the source file would\nbe named <code>readme.txt.default</code>.</p>\n\n<p>There are times when a sacred file is also a template. This is done by adding both\nextensions, for example, <code>\"readme.txt.tpl.default\"</code>. This <code>\"readme.txt\"</code> file is a sacred\nfile that is initially generated from a template.</p>\n\n<h3>Parameters</h3>\n\n<p>Template generation requires data, or parameters. The simplest form of parameter uses the\n<code>param</code> attribute:</p>\n\n<pre><code>&lt;x-generate ...&gt;\n    &lt;param name=\"bar\" value=\"42\" /&gt;\n&lt;/x-generate&gt;\n</code></pre>\n\n<p>Parameters can also be loaded from a file, like so:</p>\n\n<pre><code>&lt;x-generate ...&gt;\n    &lt;load file=\"data.properties\"/&gt;\n&lt;/x-generate&gt;\n</code></pre>\n\n<p>The following file types are understood automatically:</p>\n\n<ul>\n<li><code>\".cfg\"</code> or <code>\".properties\"</code> = A standard Java Properties file.</li>\n<li><code>\".json\"</code> = A JSON data file.</li>\n</ul>\n\n\n<p>If the file does not have one of these extensions, but is a properties file or JSON, you\ncan specify the <code>type</code> attribute as <code>json</code> or <code>properties</code>, like so:</p>\n\n<pre><code>&lt;x-generate ...&gt;\n    &lt;load file=\"data.props\" type=\"properties\" /&gt;\n    &lt;load file=\"data\" type=\"json\" /&gt;\n&lt;/x-generate&gt;\n</code></pre>\n\n<p><em>Note.</em> Parameters are applied in the order specified. Duplicate names are replaced if\nthey are encountered.</p>\n\n<h3>x-generate file tofile</h3>\n\n<p>The simplest form of <code>x-generate</code> is using the <code>file</code> attribute to transform a single\ntemplate file to a specified output file:</p>\n\n<pre><code>&lt;x-generate file=\"foo.js.tpl\" tofile=\"build/foo.js\"&gt;\n    &lt;param name=\"bar\" value=\"42\" /&gt;\n&lt;/x-generate&gt;\n</code></pre>\n\n<p>The source filename determines how the process proceeds (and which template engine to use\nand if it is sacred), but that is all.</p>\n\n<h3>x-generate file todir</h3>\n\n<p>In many cases, you can leave off the target filename and just specify the folder, like this:</p>\n\n<pre><code>&lt;x-generate file=\"foo.js.tpl\" todir=\"build\"&gt;\n    &lt;param name=\"bar\" value=\"42\" /&gt;\n&lt;/x-generate&gt;\n</code></pre>\n\n<p>This generates <code>\"foo.js\"</code> (using XTemplate) in the <code>\"build\"</code> folder.</p>\n\n<p>Beyond avoiding redundancy, this form also allows the source filename to be a template,\nfor example:</p>\n\n<pre><code>&lt;x-generate file=\"{name}.js.tpl\" todir=\"build\"&gt;\n    &lt;param name=\"name\" value=\"foobar\" /&gt;\n    &lt;param name=\"bar\" value=\"42\" /&gt;\n&lt;/x-generate&gt;\n</code></pre>\n\n<p>The source file exists with the specified name (that is, <code>\"{name}.js.tpl\"</code>), but this name\nis transformed using the XTemplate engine and the provided parameters to determine the\ntarget filename.</p>\n\n<p>In the above case, <code>\"foobar.js\"</code> is generated in the <code>build</code> directory.</p>\n\n<h3>x-generate dir todir</h3>\n\n<p>The final form of <code>x-generate</code> operates on a source folder, and generates content in the\ntarget folder, for example:</p>\n\n<pre><code>&lt;x-generate dir=\"templates/foo\" todir=\"build/foo\"&gt;\n    &lt;param name=\"bar\" value=\"42\" /&gt;\n    &lt;load file=\"data.json\"/&gt;\n&lt;/x-generate&gt;\n</code></pre>\n\n<p>In this form, the generator recursively reads files and sub-folders in <code>\"templates/foo\"</code> and\napplies the appropriate template engine. It also preserves sacred files. All file and folder\nnames are processed as XTemplate templates.</p>\n\n<h2 id='command_ant-section-5'>x-compress-js</h2>\n\n<p>Compresses JavaScript source according to the following options (attributes):</p>\n\n<ul>\n<li><code>srcfile</code>: The source file to compress.</li>\n<li><code>outfile</code>: The output file to generate (defaults to srcfile).</li>\n<li><code>charset</code>: The charset of the input/output files.</li>\n<li><code>header</code>: Optional text to include in a comment block at the start of the file.</li>\n<li><code>linebreak</code>: The column number at which to break lines (default is -1, to not break lines).</li>\n<li><code>obfuscate</code>: False to not obfuscate local symbols (default is true).</li>\n<li><code>disableoptimizations</code>: True to disable all built-in optimizations.</li>\n<li><code>preservesemi</code>: True to preserve all semicolons.</li>\n<li><code>verbose</code>: True to enable extra diagnostic messages.</li>\n</ul>\n\n\n<h2 id='command_ant-section-6'>x-compress-css</h2>\n\n<p>Compresses CSS source according to the following options (attributes):</p>\n\n<ul>\n<li><code>srcfile</code>: The source file to compress.</li>\n<li><code>outfile</code>: The output file to generate (defaults to srcfile).</li>\n<li><code>charset</code>: The charset of the input/output files.</li>\n<li><code>header</code>: Optional text to include in a comment block at the start of the file.</li>\n<li><code>linebreak</code>: The column number at which to break lines.</li>\n<li><code>verbose</code>: True to enable extra diagnostic messages.</li>\n</ul>\n\n\n<h2 id='command_ant-section-7'>x-strip-js</h2>\n\n<p>This task removes comments (line and/or block) from a JS file. The following options are\nsupported:</p>\n\n<ul>\n<li><code>srcfile</code>: The source file to strip</li>\n<li><code>outfile</code>: The output file to generate (defaults to srcfile).</li>\n<li><code>header</code>: Optional text to include in a comment block at the start of the file.</li>\n<li><code>blockcomments</code>: True (the default) to strip block comments (\"/<em> ... </em>/\").</li>\n<li><code>linecomments</code>: True (the default) to strip line comments (\"//\").</li>\n<li><code>keepfirstcomment</code>: True (the default) to keep the first comment in the JS file.\n\n<pre><code>This is typically a copyright.\n</code></pre></li>\n<li><code>whitespace</code>: True to also strip whitespace.</li>\n</ul>\n\n\n<h2 id='command_ant-section-8'>x-get-env</h2>\n\n<p>Stores an environment variable value in the specified property. The name of the environment\nvariable is first matched for exact case, but if no exact case match is found, it will pick\na match ignoring case if one exists.</p>\n\n<pre><code>&lt;x-get-env name=\"PATH\" property=\"env.path\"/&gt;\n</code></pre>\n\n<p>This should be preferred over the \"properties\" task to read environment variables because\nthat reflects the exact case of the variables as Ant properties which are case sensitive but\nenvironment variables (like \"Path\") are case insensitive at least on Windows.</p>\n\n<h2 id='command_ant-section-9'>x-escape</h2>\n\n<p>This task escapes a string and stores the escaped string in a specified property.</p>\n\n<pre><code>&lt;x-escape string=\"${some.text}\" property=\"some.text.js\" type=\"json\"/&gt;\n&lt;x-escape string=\"${some.text}\" property=\"some.text.xml\" type=\"xml\"/&gt;\n</code></pre>\n"});