var getPath = Ext.Loader.getPath;

/**
 * @class Uni.override.ApplicationOverride
 */
Ext.define('Isu.override.LoaderOverride', {
    override: 'Ext.Loader',

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