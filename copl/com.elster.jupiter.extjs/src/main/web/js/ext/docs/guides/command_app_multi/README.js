Ext.data.JsonP.command_app_multi({"guide":"<h1>Multi-page and Mixed Apps</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/command_app_multi-section-1'>Prerequisites</a></li>\n<li><a href='#!/guide/command_app_multi-section-2'>Large Application Alternatives</a></li>\n<li><a href='#!/guide/command_app_multi-section-3'>Custom Structure Application</a></li>\n<li><a href='#!/guide/command_app_multi-section-4'>Caching Shared Code</a></li>\n<li><a href='#!/guide/command_app_multi-section-5'>Beyond Two Pages</a></li>\n</ol>\n</div>\n\n<p>Build processes for large applications often require some sophisticated operations beyond\nwhat is typically needed by a single page application. This guide provides insight on how\n<a href=\"http://www.sencha.com/products/sencha-cmd/\">Sencha Cmd</a> can accomplish some of these\nmore commonly requested things. Unlike other guides, this guide is not recommending any\nparticular \"best practices\" but is rather showing how to \"think like the compiler\". The\ngoal being to suggest what is possible and how you can express complex operations.</p>\n\n<h2 id='command_app_multi-section-1'>Prerequisites</h2>\n\n<p>The following guides are recommended reading before proceeding further:</p>\n\n<ul>\n<li><a href=\"#!/guide/command_app_single\">Single-Page Apps</a>.</li>\n<li><a href=\"#!/guide/command_workspace\">Workspaces in Sencha Cmd</a>.</li>\n</ul>\n\n\n<h2 id='command_app_multi-section-2'>Large Application Alternatives</h2>\n\n<p><a href=\"#!/guide/command_workspace\">Workspaces in Sencha Cmd</a> describes the Workspace support in\nSencha Cmd that is designed specifically to facilitate large, multi-page applications. This\nguide picks up where that guide left off and describes how to use lower-level commands to\nperform some advanced build operations.</p>\n\n<p>A common desire for large applications is to generate two scripts per page instead of the\ntypical one (<code>\"all-classes.js\"</code>) to improve caching as users navigate between pages. While\nthere are many variations on the ideas this guides describes two basic approaches:</p>\n\n<ul>\n<li>Putting all code common to multiple pages in a <code>\"common.js\"</code> file</li>\n<li>Putting all framework code needed by any page in a <code>\"common.js\"</code> file</li>\n</ul>\n\n\n<p>Further, since we are using lower-level commands in this guide, we use a custom application\nfolder structure to show how Sencha Cmd can be used to fit your own specific choices for\ncode organization.</p>\n\n<p>This guide focuses primarily on Ext JS applications but the basic techniques work as well\nfor Sencha Touch applications. Many of the examples here could be (better) implemented\nas customizations to the build process but this example does not go that route and instead\nshows how to work directly with the compiler.</p>\n\n<h2 id='command_app_multi-section-3'>Custom Structure Application</h2>\n\n<p>To consider applications that do not have a workspace, lets assume that we have a two-page\napplication with the following folder structure:</p>\n\n<pre><code>build/              # The folder where build output is placed.\ncommon/             # Things common to all pages of the application.\n    src/            # Shared JavaScript code for all pages.\next/                # The framework distribution.\n    src/            # The framework source tree.\npage1/\n    index.php       # The markup file for page 1.\n    src/            # Folder containing JavaScript code unique to page 1.\npage2/\n    index.php       # The markup file for page 2.\n    src/            # Folder containing JavaScript code unique to page 2.\n</code></pre>\n\n<p>This example could be extended to cover many more pages, which would make it harder to\nfollow the example commands. There are some features that only apply to applications with\nthree or more pages so we will expand the example to illustrate that usage.</p>\n\n<h2 id='command_app_multi-section-4'>Caching Shared Code</h2>\n\n<p>If users of the application tend to visit more than one page, it may be helpful to split\nup the code such that common code is in a shared file while page-specific code is isolated\nto a second script file.</p>\n\n<p>In set operations terminology this is called a set \"intersection\". That is to say, we want to\ntake the files in the intersection of the two sets of files needed by each page and generate\na file with just those classes.</p>\n\n<p>The following command will do precisely that:</p>\n\n<pre><code>sencha -sdk ext compile -classpath=common/src,page1/src,page2/src \\\n    page -name=page1 -in page1/index.php -out build/page1/index.php \\\n         -scripts ../common.js and \\\n    page -name=page2 -in page2/index.php -out build/page2/index.php \\\n         -scripts ../common.js and \\\n    intersect -set page1,page2 and \\\n    save common and \\\n    concat -yui build/common.js and \\\n    restore page1 and \\\n    exclude -set common and \\\n    concat -yui build/page1/all-classes.js and \\\n    restore page2 and \\\n    exclude -set common and \\\n    concat -yui build/page2/all-classes.js\n</code></pre>\n\n<p>Let's look closely at what each part of this command accomplishes.</p>\n\n<p>The first thing is to create the <code>compile</code> context and tell it the <code>classpath</code> for all of\nthe source code folders:</p>\n\n<pre><code>sencha -sdk ext compile -classpath=common/src,page1/src,page2/src \\\n</code></pre>\n\n<p>Then we use two <code>page</code> commands to include the source from each page as well as generate\nthe appropriate output pages in the <code>build</code> folder. Each <code>page</code> command produces a set\nof files containing exactly the files needed by that page. These sets are given the names\n<code>page1</code> and <code>page2</code>. Finally, each generated output page will get an extra <code>script</code> tag\nwhose <code>src</code> attribute is <code>\"../common.js\"</code>.</p>\n\n<pre><code>    page -name=page1 -in page1/index.php -out build/page1/index.php \\\n         -scripts ../common.js and \\\n    page -name=page2 -in page2/index.php -out build/page2/index.php \\\n         -scripts ../common.js and \\\n</code></pre>\n\n<p>Now that all of the files needed by each page are recorded in two sets, we use <code>intersect</code>\nto determine the files needed by both pages. Only these files will be included in the\ncurrent set.</p>\n\n<pre><code>    intersect -set page1,page2 and \\\n</code></pre>\n\n<p>We use <code>save</code> to record the current set of files (the result of the intersection). These\nare the files we will put in <code>\"common.js\"</code>. The name for the new set is <code>common</code>.</p>\n\n<pre><code>    save common and \\\n</code></pre>\n\n<p>Then we use <code>concat</code> to combine the files and produce <code>\"build/common.js\"</code> (also compressing\nthe file using `-yui' to engage the YUI Compressor).</p>\n\n<pre><code>    concat -yui build/common.js and \\\n</code></pre>\n\n<p>Now we need to produce the <code>\"all-classes.js\"</code> for each page, so we use <code>restore</code> to make\nthe current set equal to the previously saved set for the page:</p>\n\n<pre><code>    restore page1 and \\\n</code></pre>\n\n<p>Then we remove from this set all of the files that we just generated in <code>\"common.js\"</code>:</p>\n\n<pre><code>    exclude -set common and \\\n</code></pre>\n\n<p>And then use <code>concat</code> again to produce <code>\"all-classes.js\"</code> for the page:</p>\n\n<pre><code>    concat -yui build/page1/all-classes.js and \\\n</code></pre>\n\n<p>We repeat the last few steps again for <code>page2</code>:</p>\n\n<pre><code>    restore page2 and \\\n    exclude -set common and \\\n    concat -yui build/page2/all-classes.js\n</code></pre>\n\n<h3>Alternative Strategy - Sharing A Framework Subset</h3>\n\n<p>A different way to partition shared code would be to isolate all of the framework code\nneeded by the application and produce a file similar to <code>\"ext-all.js\"</code> but only containing\nthose classes needed by some part of the application. This approach might load more of the\nframework than needed by each page, but the benefits of the browser cache could easily\nmake up for this increase.</p>\n\n<p>The following command contains only a slight adjustment to the above:</p>\n\n<pre><code>sencha -sdk ext compile -classpath=common/src,page1/src,page2/src \\\n    page -name=page1 -in page1/index.php -out build/page1/index.php \\\n         -scripts ../common.js and \\\n    page -name=page2 -in page2/index.php -out build/page2/index.php \\\n         -scripts ../common.js and \\\n    union -set page1,page2 and \\\n    exclude -not -namespace Ext and \\\n    save common and \\\n    concat -yui build/common.js and \\\n    restore page1 and \\\n    exclude -set common and \\\n    concat -yui build/page1/all-classes.js and \\\n    restore page2 and \\\n    exclude -set common and \\\n    concat -yui build/page2/all-classes.js\n</code></pre>\n\n<p>The difference between this command and the previous command is in how the <code>common</code> set is\ncalculated.</p>\n\n<pre><code>    union -set page1,page2 and \\\n    exclude -not -namespace Ext and \\\n</code></pre>\n\n<p>In this case the <code>union</code> command is used to include all files used by either page. This\nset is then reduced using the <code>exclude</code> command to remove all classes that are not in the\nExt namespace. This will leave only the framework code that is needed by either page in\nthe current set.</p>\n\n<p>The remainder of the command above and below these two lines is the same as before.</p>\n\n<h2 id='command_app_multi-section-5'>Beyond Two Pages</h2>\n\n<p>Applications with more than two pages can be managed as an extension of a two-page\napplication as discussed above. Just add extra <code>page</code> commands (one for each page) and\nextra set operations to produce the appropriate <code>\"all-classes.js\"</code> file for each page.</p>\n\n<p>There are interesting possibilities for code sharing among the multiple pages. For\nexample, let's consider a five-page application structured in the same basic way.</p>\n\n<p>It may be that the common set of files produced by the intersection of all pages is quite\nsmall. This will force code that is not used by all pages out of <code>\"common.js\"</code> and into an\n<code>\"all-classes.js\"</code> file for each page. One strategy for dealing with this is to manually\ndivide up similar pages and treat the application as multiple, independent, multipage\napplications.</p>\n\n<p>Another, simpler, way would be to use a \"fuzzy intersection,\" that is an operation the\nselects all classes used by a specified minimum number of pages. Here's an example:</p>\n\n<pre><code>sencha -sdk ext compile -classpath=common/src,page1/src,page2/src \\\n    page -name=page1 -in page1/index.php -out build/page1/index.php \\\n         -scripts ../common.js and \\\n    page -name=page2 -in page2/index.php -out build/page2/index.php \\\n         -scripts ../common.js and \\\n    page -name=page2 -in page3/index.php -out build/page3/index.php \\\n         -scripts ../common.js and \\\n    page -name=page2 -in page4/index.php -out build/page4/index.php \\\n         -scripts ../common.js and \\\n    page -name=page2 -in page5/index.php -out build/page5/index.php \\\n         -scripts ../common.js and \\\n    intersect -min=3 -set page1,page2,page3,page4,page5 and \\\n    save common and \\\n    concat -yui build/common.js and \\\n    restore page1 and \\\n    exclude -set common and \\\n    concat -yui build/page1/all-classes.js and \\\n    restore page2 and \\\n    exclude -set common and \\\n    concat -yui build/page2/all-classes.js and \\\n    restore page3 and \\\n    exclude -set common and \\\n    concat -yui build/page3/all-classes.js and \\\n    restore page4 and \\\n    exclude -set common and \\\n    concat -yui build/page4/all-classes.js and \\\n    restore page5 and \\\n    exclude -set common and \\\n    concat -yui build/page5/all-classes.js\n</code></pre>\n\n<p>Other than the three additional <code>page</code> commands as well as three stanzas of <code>restore</code>,\n<code>exclude</code> and <code>concat</code>, the above command only changed from the original intersection in\nthis one way:</p>\n\n<pre><code>    intersect -min=3 -set page1,page2,page3,page4,page5 and \\\n</code></pre>\n\n<p>The <code>-min</code> switch activated the fuzzy intersection method. By default, <code>intersect</code> selects\nclasses used by 100% of the specified sets or, in this case, all 5 sets. With <code>-min</code> you\ncan override this threshold. By specifying <code>-min=3</code> we are saying to include in the current\nset any class used by at least 3 sets (or 60%).</p>\n","title":"Multi-Page Ext JS Apps"});