Ext.data.JsonP.command_packages({"title":"Sencha Cmd Packages","guide":"<h1>Sencha Cmd Packages</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/command_packages-section-1'>The \"packages\" Folder</a></li>\n<li><a href='#!/guide/command_packages-section-2'>Requiring Packages in Applications</a></li>\n<li><a href='#!/guide/command_packages-section-3'>Local Packages</a></li>\n<li><a href='#!/guide/command_packages-section-4'>Remote Packages</a></li>\n<li><a href='#!/guide/command_packages-section-5'>The Package Catalog</a></li>\n<li><a href='#!/guide/command_packages-section-6'>Version Management</a></li>\n</ol>\n</div>\n\n<p>Sencha Cmd v3.1 includes the Sencha Package Manager. There are two basic problems that\npackages are designed to solve: consumption and distribution. This guide focused on these\ntopics. See also <a href=\"#!/guide/command_package_authoring\">Creating Sencha Cmd Packages</a> for\ninformation about creating and sharing a package.</p>\n\n<h2 id='command_packages-section-1'>The \"packages\" Folder</h2>\n\n<p>All workspaces generated by Sencha Cmd have a <code>\"packages\"</code> folder at the root. The location\nof this folder can be configured, but regardless of its location, the role of the\n<code>\"packages\"</code> folder is to serve as the storage of all packages used by the applications\n(or other packages) in the workspace.</p>\n\n<p>Packages will be added to the <code>\"packages\"</code> folder when they are required by an application\nin the workspace or when you call <code>sencha generate package</code>.</p>\n\n<h2 id='command_packages-section-2'>Requiring Packages in Applications</h2>\n\n<p>Regardless of the origin of a package (whether it was created locally or downloaded from\na remote repository - see below), consuming the package is the same: you add an entry to\nthe <code>requires</code> array in <code>\"app.json\"</code>. For demonstration purposes, we have added a simple\npackage that you can experiment with:</p>\n\n<pre><code>{\n    \"name\": \"MyApp\",\n    \"requires\": [\n        \"ext-easy-button\"\n    ]\n}\n</code></pre>\n\n<p>Given the above requirements, <code>sencha app build</code> and <code>sencha app refresh</code> will both now\nperform the steps needed to integrate the package in to your application. Typically,\nafter changing package requirements, you will need to run <code>sencha app refresh</code> so that\nthe metadata required to support \"dev mode\" is up to date.</p>\n\n<p>Which ever command you run, Sencha Cmd will download and expand the package to your\n<code>\"packages\"</code> folder. After this you will find a <code>\"packages/ext-easy-button\"</code> folder in\nyour workspace.</p>\n\n<h2 id='command_packages-section-3'>Local Packages</h2>\n\n<p>One use of packages is simply to hold code or (in Ext JS 4.2) themes that are available\nfor multiple applications in a workspace. These packages need never be distributed (beyond\nsource control) to provide value to your development.</p>\n\n<p>In previous releases of Sencha Cmd, you could only share code in a workspace by using the\n<code>workspace.classpath</code> property in your <code>.sencha/workspace/sencha.cfg\"</code> file. While this\nstill works, this mechanism was limited because you could not easily share SASS/CSS\nstyling or resources such as images. Using packages, you can do all of these things.</p>\n\n<p>To add a package to your workspace, you just generate the package:</p>\n\n<pre><code>sencha generate package -type code common\n</code></pre>\n\n<p>This package is marked as <code>local: true</code> in its <code>\"package.json\"</code>. This flag prevents Sencha\nCmd from ever overwriting the package with a version from a remote repository (see below).</p>\n\n<p>Then add \"common\" as a requirement to your application's <code>\"app.json\"</code> as described above:</p>\n\n<pre><code>{\n    \"name\": \"MyApp\",\n    \"requires\": [\n        \"common\"\n    ]\n}\n</code></pre>\n\n<p>For more details, especially regarding how to distribute packages to others, see\n<a href=\"#!/guide/command_package_authoring\">Creating Sencha Cmd Packages</a>.</p>\n\n<h2 id='command_packages-section-4'>Remote Packages</h2>\n\n<p>While local packages can be very valuable in large applications, one of the most useful\naspects of packages is the ability to distribute packages for others to consume. In fact\nwe have already used a remote package: <code>\"ext-easy-button\"</code>.</p>\n\n<p>Packages are shared and distributed using package repositories. Sencha Cmd automatically\ncreates a \"Local Repository\" for caching packages as well as for publishing packages. The\nrole of the local repository for package authoring is not discussed in this guide. For\ndetails on that topic see <a href=\"#!/guide/command_package_authoring\">Creating Sencha Cmd Packages</a>.</p>\n\n<h3>Local Repository</h3>\n\n<p>Many operations implicitly use the local repository such as the <code>\"ext-easy-button\"</code> example\nabove. In that example, Sencha Cmd followed these basic steps when it encountered the\n<code>requires</code> statement in <code>\"app.json\"</code>:</p>\n\n<ul>\n<li>Look in the workspace to see if the package is already present.</li>\n<li>Check the local repository to see if there is a version already downloaded.</li>\n<li>Look at the set of remote repositories defined to see if any has the package.</li>\n<li>Download the package from the remote repository and add to the local repository.</li>\n</ul>\n\n\n<p>Once the package has been downloaded, subsequent requirements for this package will not\nrequire downloading - the package will be found in the local repository.</p>\n\n<h4>Location of the Local Repository</h4>\n\n<p>The local repository is created in a folder \"beside\" the various versions to facilitate\ncaching. For example, the default install directory of Sencha Cmd on Windows for user Foo\nmight be something like this:</p>\n\n<pre><code>C:\\Users\\Foo\\bin\\Sencha\\Cmd\\3.1.0.256\n</code></pre>\n\n<p>Given that install directory, the local repository would be located at:</p>\n\n<pre><code>C:\\Users\\Foo\\bin\\Sencha\\Cmd\\repo\n</code></pre>\n\n<p>This can be changed by editing the <code>\"sencha.cfg\"</code> in the Sencha Cmd install folder.</p>\n\n<p>The contents of the local repository are discussed further in\n<a href=\"#!/guide/command_package_authoring\">Creating Sencha Cmd Packages</a>.</p>\n\n<h3>Remote Repositories</h3>\n\n<p>In order to download packages to the local repository, Sencha Cmd must know about remote\nrepositories. By default, Sencha Cmd automatically adds the Sencha Package Repository as\na remote repository named \"sencha\".</p>\n\n<p>To see the list of remote repositories, run the <code>sencha repository list</code> command:</p>\n\n<pre><code>&gt; sencha repository list\nSencha Cmd v3.1.0.xxx\n[INF] Remote repository connections (1):\n[INF]\n[INF]     sencha - http://cdn.sencha.com/cmd/packages/\n</code></pre>\n\n<p>You can add and remove repositories from this list using <code>sencha repository add</code> and\n<code>sencha repository remove</code> commands. Your local repository is in fact a valid remote\nrepository for others if you chose to host it. For details on this, see\n<a href=\"#!/guide/command_package_authoring\">Creating Sencha Cmd Packages</a>.</p>\n\n<h2 id='command_packages-section-5'>The Package Catalog</h2>\n\n<p>The set of packages available to use is listed in the \"catalog\". You can display the\ncontents of the global catalog using this command:</p>\n\n<pre><code>sencha package list\n</code></pre>\n\n<p>When you list packages you will notice that the listing includes names and versions.</p>\n\n<h3>Name Assignment</h3>\n\n<p>Because Sencha Cmd's repository management is distributed and you can add or remove remote\nrepositories as you see fit, there is no Universal namespace of packages. If you retain\nthe default \"sencha\" connection to the Sencha Package Repository, then you can view the\ncontent of that repository as a naming standard.</p>\n\n<p>Packages published by Sencha will use names of the following forms:</p>\n\n<ul>\n<li>sencha-*</li>\n<li>ext-*</li>\n<li>touch-*</li>\n<li>cmd-*</li>\n</ul>\n\n\n<p>All package names beginning with the above prefixes are reserved by Sencha with respect\nto the Sencha Package Repository. It is recommended that you avoid conflicting with these\nnames even if you disconnect from the Sencha Package Repository.</p>\n\n<h2 id='command_packages-section-6'>Version Management</h2>\n\n<p>To connect packages and their consumers (that is, applications or other packages), it is\nimportant to consider the versions involved. To help manage this, all packages have version\nnumbers which are used by the <code>requires</code> statement to handle version restrictions.</p>\n\n<h3>Package Versioning</h3>\n\n<p>All packages are described by the combination of their name and their version. For each\nversion of a package, however, there is another version number that describes its level\nof backwards compatibility. This version is a statement made by the package creator that\nthey believe the particular version of their package to be backwards compatible to some\nspecific previous version level.</p>\n\n<p>In the <code>\"package.json\"</code> descriptor there are two important properties: <code>version</code> and\n<code>compatVersion</code>. For example:</p>\n\n<pre><code>{\n    ...\n    \"version\": \"3.5.1\",\n    \"compatVersion\": \"2.4.2\",\n    ...\n}\n</code></pre>\n\n<p>This number must be of this format:</p>\n\n<pre><code>\\d+(\\.\\d+)*\n</code></pre>\n\n<h3>Version Restrictions</h3>\n\n<p>When using the <code>requires</code> property above we only specified the package name to keep the\nexample as simple as possible. What that means precisely is \"the latest version\". In some\ncases this is acceptable, but it can be a risky requirement should that package's next\nrelease be a complete redesign and offer no level of backwards compatibility.</p>\n\n<p>To protect your application from this, we can change the <code>require</code> statement to be very\nrestrictive and lock down the version number we want:</p>\n\n<pre><code>{\n    \"name\": \"MyApp\",\n    \"requires\": [\n        \"ext-easy-button@1.0\"\n    ]\n}\n</code></pre>\n\n<p>This approach has its place, but it prevents even maintenance releases of the package\nfrom being picked up. In final stages of a project, this may be exactly what is desired,\nbut during development perhaps we want to be a little less restrictive and accept any\ncompatible version.</p>\n\n<p>The following change will do that:</p>\n\n<pre><code>{\n    \"name\": \"MyApp\",\n    \"requires\": [\n        \"ext-easy-button@1.0?\"\n    ]\n}\n</code></pre>\n\n<p>The above requests the latest available version of <code>\"ext-easy-button\"</code> that has described\nitself as backwards compatible with version 1.0.</p>\n\n<p>Other ways to restrict version matching are:</p>\n\n<ul>\n<li>-1.2 (no lower limit - up to version 1.2)</li>\n<li>1.0- (no upper limit - version 1.0 or higher)</li>\n<li>1.0+ (same as above - version 1.0 or higher)</li>\n<li>1.0-1.2 (versions 1.0 up to 1.2 inclusive)</li>\n<li>1.0-1.2? (compatible with versions 1.0 up to 1.2 inclusive)</li>\n</ul>\n\n\n<h3>Resolving Versions</h3>\n\n<p>Given all of the desired and available versions, Sencha Cmd will determine the best set\nof matching packages and ensure that these are in your workspace.</p>\n\n<p>If there are mutually exclusive requirements this process may fail. In this case, you will\nsee an error message explaining what requirements could not be satisfied.</p>\n"});