/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('ChartsKitchenSink.view.ContentPanel', {
    extend: 'Ext.panel.Panel',
    xtype: 'contentPanel',
    id: 'content-panel',
    title: '&nbsp;',
    bodyStyle: 'padding: 20px',

    dockedItems: [{
		dock: 'top',
		xtype: 'descriptionPanel',
        minHeight: 50,
        height: 'auto'
    }],
    autoScroll: true
});