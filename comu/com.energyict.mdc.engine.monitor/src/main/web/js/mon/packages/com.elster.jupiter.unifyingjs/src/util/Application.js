/**
 * @class Uni.util.Application
 */
Ext.define('Uni.util.Application', {
    singleton: true,

    appPath: 'app',

    getAppNamespace: function () {
        var paths = Ext.Loader.getConfig().paths;

        for (var name in paths) {
            if (paths.hasOwnProperty(name) && paths[name] === this.appPath) {
                return name;
            }
        }

        return undefined;
    }
});