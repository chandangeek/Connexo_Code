/*
    This file is generated and updated by Sencha Cmd. You can edit this file as
    needed for your application, but these edits will have to be merged by
    Sencha Cmd when upgrading.
*/

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
    loader.initI18n(['USM']);

    //Uni.I18n.currencyFormatKey = 'mtr.playground.i18n.currencyformat';
    //Uni.I18n.decimalSeparatorKey = 'mtr.playground.i18n.decimalseparator';
    //Uni.I18n.thousandsSeparatorKey = 'mtr.playground.i18n.thousandsseparator';

    loader.onReady(function () {
        Ext.Loader.setConfig({
            enabled: true,
            disableCaching: true // For debug only.
        });

        // Start up the application.
        Ext.application({
            name: 'Usr',

            extend: 'Usr.Application',

            autoCreateViewport: true
        });
    });
});


