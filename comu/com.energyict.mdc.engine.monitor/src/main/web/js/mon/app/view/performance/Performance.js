/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.performance.Performance', {
    extend: 'Ext.panel.Panel',
    xtype: 'performance',
    layout: {
        type : 'vbox',
        align : 'stretch'
    },
    autoScroll: true, // show scroll bars whenever needed
    border: false,
    items: [
        {
            xtype: 'storage'
        },
        {
            xtype: 'connections'
        },
        {
            xtype: 'poolsPerformance'
        }
    ]
});
