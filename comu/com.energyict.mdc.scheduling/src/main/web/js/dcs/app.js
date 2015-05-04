/*
 This file is generated and updated by Sencha Cmd. You can edit this file as
 needed for your application, but these edits will have to be merged by
 Sencha Cmd when upgrading.
 */

// DO NOT DELETE - this directive is required for Sencha Cmd packages to work.
//@require @packageOverrides

Ext.require('Uni.Loader');

Ext.onReady(function () {

    var loader = Ext.create('Uni.Loader');
    loader.initI18n(['DCS']);
    loader.onReady(function () {
        Ext.Loader.setConfig({
            enabled: true,
            disableCaching: true // For debug only.
        });

        // Start up the application.
        Ext.application({
            name: 'Dcs',

            extend: 'Dcs.Application',

            autoCreateViewport: true
        });
    });

});



