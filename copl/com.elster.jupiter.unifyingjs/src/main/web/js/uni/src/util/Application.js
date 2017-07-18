/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    },

    getAppName: function () {
        var appNamespace = this.getAppNamespace();
        if (appNamespace == 'MdcApp') {
            return 'MultiSense';
        }
        if (appNamespace == 'SystemApp') {
            return 'Admin';
        }
        if (appNamespace == 'InsightApp') {
            return 'Insight';
        }
        return appNamespace;
    }


});