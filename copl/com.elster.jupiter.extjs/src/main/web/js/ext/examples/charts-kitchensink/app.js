/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.application({
    name: 'ChartsKitchenSink',

    requires: [
        'Ext.grid.Panel',
        'ChartsKitchenSink.view.*',
        'Ext.state.CookieProvider',
        'Ext.window.MessageBox',
        'Ext.tip.QuickTipManager'
    ],

    controllers: [
        'Main'
    ],

    autoCreateViewport: true,

    init: function() {
        Ext.tip.QuickTipManager.init();
        Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));       
    }
});
