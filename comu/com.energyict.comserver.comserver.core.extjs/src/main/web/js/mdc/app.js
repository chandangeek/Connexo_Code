/*
    This file is generated and updated by Sencha Cmd. You can edit this file as
    needed for your application, but these edits will have to be merged by
    Sencha Cmd when upgrading.
*/



Ext.require('Uni.Loader');

Ext.onReady(function () {

    var loader = Ext.create('Uni.Loader');
    loader.onReady(function () {
        Ext.Loader.setConfig({
            enabled: true,
            disableCaching: true // For debug only.
//            paths: {
//                'Chart': 'packages/Highcharts_Sencha/Chart'
//            }
        });

        Ext.application({
            name: 'Mdc',

            extend: 'Mdc.Application',

            autoCreateViewport: true
        });
    });

});