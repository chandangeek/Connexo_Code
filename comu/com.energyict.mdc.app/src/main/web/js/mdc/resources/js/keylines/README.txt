It is recommended to serve this demo from a server rather than the file system.

If you are using NodeJS as your server, just run this file 'node server.js'

Running server.js has a dependency on the express module being available, which can be installed (once node is installed) by running

 >  npm install express

If you can't run Node or express just use a simple python server
    
 > python -m SimpleHTTPServer 8080

Then you can navigate to http://localhost:8080 in your web browser to see the demo running

Note: If the demo relies on a database (e.g. the Neo4j or Titan demos) then you will have to setup your database and load the data in order for it to work.

File Name	          Description
assets/	            KeyLines image assets directory
css/keylines.css	  KeyLines style settings
fonts/	            Default KeyLines font files
images/	            Icon, glyph and flag images for use with KeyLines
index.htm	          A 'hello world' sample file
js/keylines.js	    The KeyLines JavaScript component
map/*	              Map display code
ng/*	              AngularJS integration code (beta)
react/*	            ReactJS integration code (beta)
server.js	          A simple NodeJS webserver for testing the zip contents
