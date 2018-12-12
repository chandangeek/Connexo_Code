/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.Viewport', {
    extend: 'Ext.container.Viewport',
    requires: [
        '*'
    ],
    layout: 'border',

    items: [{
        region: 'north',
        xtype: 'appHeader'
    }, {
        region: 'west',
        xtype: 'navigation'
    }, {
        region: 'center',
        xtype: 'content'
    }]
});
