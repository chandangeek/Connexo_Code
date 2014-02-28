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
        'Chart': 'packages/Highcharts_Sencha/Chart',
        'Uni' : '../uni/src'
    }
});

Ext.onReady(function () {
    var loader = Ext.create('Uni.Loader');
    loader.initI18n(['MTR']);

    Uni.I18n.currencyFormatKey = 'mtr.playground.i18n.currencyformat';
    Uni.I18n.decimalSeparatorKey = 'mtr.playground.i18n.decimalseparator';
    Uni.I18n.thousandsSeparatorKey = 'mtr.playground.i18n.thousandsseparator';

    loader.onReady(function () {
        // Start up the application.
        Ext.application({
            name: 'Mtr',

            /*"requires": [
                "UnifyingJS"
            ],*/

            extend: 'Mtr.Application',

            autoCreateViewport: true
        });
    });
});

