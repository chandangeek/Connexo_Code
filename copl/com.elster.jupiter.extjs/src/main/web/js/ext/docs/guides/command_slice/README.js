Ext.data.JsonP.command_slice({"guide":"<h1>Sencha Cmd Image Slicer</h1>\n<div class='toc'>\n<p><strong>Contents</strong></p>\n<ol>\n<li><a href='#!/guide/command_slice-section-1'>Prerequisites</a></li>\n<li><a href='#!/guide/command_slice-section-2'>Manifest Contents</a></li>\n<li><a href='#!/guide/command_slice-section-3'>Sprites</a></li>\n</ol>\n</div>\n\n<p>The <code>sencha fs slice</code> command performs general image slicing and manipulation driven by\nthe contents of a JSON manifest file. This manifest is typically generated automatically\nby <code>sencha package build</code> for a package or <code>sencha app build</code> for an application, but for\ndiagnostic purposes the <code>\"theme-capture.json\"</code> is left on disk in the build folder. This\nguide describes the format of this manifest.</p>\n\n<h2 id='command_slice-section-1'>Prerequisites</h2>\n\n<p>The following guides are recommended reading before proceeding further:</p>\n\n<ul>\n<li><a href=\"#!/guide/command\">Introduction to Sencha Cmd</a>.</li>\n<li><a href=\"#!/guide/command_app\">Using Sencha Cmd</a>.</li>\n</ul>\n\n\n<h2 id='command_slice-section-2'>Manifest Contents</h2>\n\n<p>The manifest file contains an array of image area definitions that further contain a set\nof \"slices\" to produce.</p>\n\n<pre><code>[\n    {\n        // The border-width of the box.\n        //\n        \"border\": {\n            \"b\": 1,\n            \"l\": 1,\n            \"r\": 1,\n            \"t\": 1\n        },\n\n        // The box or coordinates of the area within the image file being\n        // processed.\n        //\n        \"box\": {\n            \"h\": 22,\n            \"w\": 46,\n            \"x\": 10,\n            \"y\": 51\n        },\n\n        // The direction of the background gradient if there is one.\n        //\n        \"gradient\": \"top\",\n\n        // The border radii for top-left, top-right, bottom-right and\n        // bottom-left corners.\n        //\n        \"radius\": {\n            \"bl\": 3,\n            \"br\": 3,\n            \"tl\": 3,\n            \"tr\": 3\n        },\n\n        // The side of the image to stretch for sliding-doors. This is currently\n        // hard-coded to stretch to 800px.\n        //\n        \"stretch\": \"bottom\",\n\n        // The slice operations to perform and the relative path to the\n        // desired image.\n        //\n        \"slices\": {\n            \"bg\": \"images/btn/btn-default-small-bg.gif\",\n            \"corners\": \"images/btn/btn-default-small-corners.gif\",\n            \"sides\": \"images/btn/btn-default-small-sides.gif\"\n        }\n    }\n]\n</code></pre>\n\n<p>The keys of the <code>\"slices\"</code> object can be any of these:</p>\n\n<ul>\n<li><code>bg</code> : Extracts the box background (inside the border)</li>\n<li><code>frame_bg</code> : Extracts the box background (inside the border and border radius)</li>\n<li><code>corners</code> : A sprite consisting of the 4 rounded corners and 2 sides.</li>\n<li><code>sides</code> : A sprite consisting of the 2 sides that are not in the <code>ccrners</code>.</li>\n</ul>\n\n\n<p>For each of the above, there is also a \"_rtl\" form that operates on the base\nwidget after first flipping the widget horizontally (e.g., \"bg_rtl\").</p>\n\n<h2 id='command_slice-section-3'>Sprites</h2>\n\n<p>There are two special sprites produced by the slicer: corners and sides. These contain\ndifferent sub-images based on the presence and type of the background gradient.</p>\n\n<p>The \"corners\" sprite actually contains more than just the 4 (rounded) corners: it also\nhas two of the sides. The two sides contained in the \"corners\" sprite are the two sides\nthat are repeated.</p>\n\n<p>The \"sides\" sprite contains the remaining two side sub-images. These may or may not be\nrepeated, depending on the presence of a background gradient.</p>\n\n<p>To understand the slicing operation, consider the starting frame:</p>\n\n<pre><code>    +----+--------------------------------------------------+----+\n    | TL |                         TC                       | TR |\n    +----+--------------------------------------------------+----+\n    |    |                                                  |    |\n    |    |                                                  |    |\n    |    |                                                  |    |\n    | ML |                                                  | MR |\n    |    |                                                  |    |\n    |    |                                                  |    |\n    |    |                                                  |    |\n    +----+--------------------------------------------------+----+\n    | BL |                         BC                       | BR |\n    +----+--------------------------------------------------+----+\n</code></pre>\n\n<p>The width and height of the cells of the sprites is equal to the maximum of the four\nborder radii and border widths. Typically these are symmetric on some axis, but in all\ncases the sprite cells are square and use the maximum required size.</p>\n\n<h3>Null or Vertical Gradient</h3>\n\n<p>When there is no gradient or if the gradient direction is vertical (that is, either \"top\"\nor \"bottom\"), the \"corners\" sprite produced is 1x6 with the following layout:</p>\n\n<pre><code>    +------+\n    |  TC  |  (top center side of frame)\n    +------+\n    |  BC  |  (bottom center side of frame)\n    +------+\n    |  TL  |  (top left corner)\n    +------+\n    |  TR  |  (top right corner)\n    +------+\n    |  BL  |  (bottom left corner)\n    +------+\n    |  BR  |  (bottom right corner)\n    +------+\n</code></pre>\n\n<p>The \"TC\" and \"BC\" cells are used with <code>repeat-x</code> styling to fill the desired width.</p>\n\n<p>The \"sides\" sprite is then a 2x1 sprite with this layout:</p>\n\n<pre><code>    +------+------+\n    |      |      |\n    |      |      |\n    |  ML  |  MR  |\n    |      |      |\n    |      |      |\n    +------+------+\n</code></pre>\n\n<p>If there is no gradient, the cells are used with <code>repeat-y</code> styling. Otherwise, the whole\nsprite is stretched to 800px and one edge is repeated based on the <code>stretch</code> property in\nthe manifest. If 'stretch' is <code>\"bottom\"</code> then the bottom row is repeated until the sprite\nhas 800px height. Otherwise, the captured gradient is placed at the bottom of the cell the\ntop row is repeated to fill the space above the gradient.</p>\n\n<h3>Horizontal Gradient</h3>\n\n<p>If there is a horizontal gradient (that is, either \"left\" or \"right\"), the corners sprite\nproduced is 3x4 with this layout:</p>\n\n<pre><code>    +------+------+------+\n    |  TL  |      |      |\n    +------+      |      |\n    |  TR  |      |      |  ML = middle left side of frame\n    +------+  ML  |  MR  |\n    |  BL  |      |      |  MR = middle right side of frame\n    +------+      |      |\n    |  BR  |      |      |\n    +------+------+------+\n</code></pre>\n\n<p>The \"ML\" and \"MR\" cells are styled with <code>repeat-y</code> to fill the desired height.</p>\n\n<p>The \"sides\" sprite is a 1x2 sprite:</p>\n\n<pre><code>    +------+\n    |  TC  |\n    +------+\n    |  BC  |\n    +------+\n</code></pre>\n\n<p>Because this layout is only used in the presence of a gradient, these sprites are always\nstretched to 800px width. The value of the <code>stretch</code> property on the manifest determines\nwhere the column of pixels is repeated. A value of <code>\"right\"</code> means the right-most column\nof pixels is repeated to fill 800px. A value of <code>\"left\"</code> will produce a cell where the\ngradient is copied to the right-most end and the left-most column of pixels is repeated\nto fill the space.</p>\n","title":"Image Slicing for IE"});