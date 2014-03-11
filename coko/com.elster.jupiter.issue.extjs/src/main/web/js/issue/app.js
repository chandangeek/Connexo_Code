/*
 This file is generated and updated by Sencha Cmd. You can edit this file as
 needed for your application, but these edits will have to be merged by
 Sencha Cmd when upgrading.
 */

// DO NOT DELETE - this directive is required for Sencha Cmd packages to work.
//@require @packageOverrides

Ext.require('Uni.Loader');

Ext.Loader.setConfig({
    enabled: true,
    disableCaching: true, // For debug only.
    paths: {
        'Uni' : '../uni/src'
    }
});

Ext.onReady(function () {
    var loader = Ext.create('Uni.Loader');
    loader.initI18n(['ISU']);

    loader.onReady(function () {
        // Start up the application.
        Ext.application({
            name: 'Isu',

            /*"requires": [
                "UnifyingJS"
            ],*/

            extend: 'Isu.Application',

            autoCreateViewport: true
        });
    });
});

