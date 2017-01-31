/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('ChartsKitchenSink.view.Viewport', {
    extend: 'Ext.container.Viewport',
    requires:[
        'Ext.tab.Panel',
        'Ext.layout.container.Border'
    ],

    layout: 'border',

    items: [{
        region: 'north',
        xtype: 'appHeader'
    }, {
        region: 'west',
        xtype: 'navigation',
        width: 225,
        minWidth: 100,
        height: 200,
        split: true,
        stateful: true,
        stateId: 'mainnav.west',
        collapsible: true,
        tools: [{
            type: 'gear',
            regionTool: true
        }]
    }, {
        region: 'center',
        xtype: 'contentPanel'
    }, {
        region: 'east',
        id: 'east-region',
        title: 'Code Preview',
        stateful: true,
        stateId: 'mainnav.east',
        split: true,
        collapsible: true,
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        width: 350,
        height: 200,
        minWidth: 100,
        tools: [{
            type: 'gear',
            regionTool: true
        },{
            type: 'maximize',
            tooltip: 'Maximize example code content'
        }],
        items: [{
            xtype: 'codePreview',
            flex: 1
        }]
    }]
});
