Ext.define('Mdc.util.UriParser', {

    protocol: null,
    hostname: null,
    port: null,
    pathname: null,
    search: null,
    hash: null,
    host: null,

    parse: function (href) {
        var parser = document.createElement('a');
        parser.href = href;

        this.protocol = parser.protocol;    // => "http:"
        this.hostname = parser.hostname;     // => "example.com"
        this.port = parser.port;             // => "3000"
        this.pathname = parser.pathname;     // => "/pathname/"
        this.search = parser.search;           // => "?search=test"
        this.hash = parser.hash;               // => "#hash"
        this.host = parser.host;               // => "example.com:3000"

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
            url += this.pathname;
        }
        if (this.search){
            url += this.search;
        }
        if (this.hash) {
            url += this.hash;
        }
        return url;
    }
});