/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('AM.view.Viewport', {
    extend: 'Ext.container.Viewport',

    layout: 'fit',
    items: [{
        xtype: 'userlist'
    }]
});