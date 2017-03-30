/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.util.UriParser', {
    alternateClassName: ['UriParser'],
    protocol: null,
    hostname: null,
    port: null,
    pathname: null,
    search: null,
    hash: null,
    host: null,

    parse: function (href) {
        var parser = document.createElement('a');
        if (Ext.isIE){
            document.body.appendChild(parser);
        }
        parser.href = href;

        this.protocol = parser.protocol;    // => "http:"
        this.hostname = parser.hostname;     // => "example.com"
        this.port = parser.port;             // => "3000"
        if (!this.port) this.port = "80";
        this.pathname = parser.pathname;     // => "/pathname/"
        this.search = parser.search;           // => "?search=test"
        this.hash = parser.hash;               // => "#hash"
        this.host = parser.host;               // => "example.com:3000"

        if (Ext.isIE){
            document.body.removeChild(parser);
        }

        return this;
    },

    withProtocol: function (protocol) {
        this.protocol = protocol;
        return this;
    },

    withHostName: function (hostname){
        this.hostname = hostname;
        this.host = this.hostname + ":" + this.port;
        return this;
    },

    withPort: function (port){
        this.port = port;
        this.host = this.hostname + ":" + this.port;
        return this;
    },

    withPath: function (path){
        this.pathname = path;
        return this;
    },

    buildUrl: function(){
        var url = this.protocol + "//" + this.host;
        if (this.pathname){
            if (!Ext.String.startsWith(this.pathname, "/")) {
                url += "/";
            }
            url += this.pathname;
        }
        if (this.search){
            if (!Ext.String.startsWith(this.search, "?")){
                url += "?";
            }
            url += this.search;
        }
        if (this.hash) {
            if (!Ext.String.startsWith(this.hash, "#")){
                url += "#";
            }
            url += this.hash;
        }
        return url;
    }
});