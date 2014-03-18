/*
 This file is generated and updated by Sencha Cmd. You can edit this file as
 needed for your application, but these edits will have to be merged by
 Sencha Cmd when upgrading.
 */

// DO NOT DELETE - this directive is required for Sencha Cmd packages to work.
//@require @packageOverrides

Ext.Loader.setConfig({
    enabled: true,
    disableCaching: true // For debug only.
});

Ext.Loader.addClassPathMappings({
    "Isu": "app",
    "Ext": "/apps/ext/src",
    "Ext.Msg": "/apps/ext/src/window/MessageBox.js",
    'Uni': "/apps/uni/src",
    'Ext.ux.window.Notification': "/apps/uni/packages/Ext.ux.window.Notification/Notification.js"
});

Ext.Loader.syncRequire([
    'Isu.override.LoaderOverride',
    'Isu.override.ProxyOverride',
    'Isu.util.Config'
], function(){

    var config = new Isu.util.Config();

    config.onReady(function() {

        Ext.require('Uni.Loader');

        Ext.onReady(function () {

            var loader = Ext.create('Uni.Loader');
            loader.initI18n(['ISU']);

            // Start up the application.
            Ext.application({
                name: 'Isu',
                extend: 'Isu.Application',
                autoCreateViewport: true
            });

        });
    });
});
