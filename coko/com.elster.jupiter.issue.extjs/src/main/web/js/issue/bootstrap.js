var getPath = Ext.Loader.getPath;
Ext.apply(Ext.Loader, {
    basePath: '',
    setBasePath: function(path) {
        this.basePath = path;
    },
    getBasePath: function() {
        return this.basePath;
    },
    getPath: function(className) {
        var path = getPath(className);
        if (path[0] === "/" && this.getBasePath()) {
            path = this.getBasePath() + path;
        }
        return path;
    }
});

Ext.Loader.addClassPathMappings({
  "Ext": "/apps/ext/src",
  "Ext.Msg": "/apps/ext/src/window/MessageBox.js",
  "Isu": "app"
});