/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.controller.systeminfo.SystemInfo', {
    extend: 'Ext.app.Controller',

    stores: [
        'Sam.store.SystemInfo'
    ],

    views: [
        'Sam.view.systeminfo.SystemInfo'
    ],

    showSystemInfo: function () {
        var me = this;

        me.getStore('Sam.store.SystemInfo').load();
        me.getApplication().fireEvent('changecontentevent', Ext.widget('system-info', {itemId: 'system-info-page'}));
    }
});