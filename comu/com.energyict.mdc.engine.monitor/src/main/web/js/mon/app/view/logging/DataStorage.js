/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.logging.DataStorage', {
    extend: 'Ext.container.Container',
    xtype: 'dataStorage',
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    defaults: { flex: 1, margins: '0, 10, 0, 10' }, // top, right, bottom, left
    items: [
        {
            xtype: 'container',
            itemId: 'storageLoggingContainer',
            listeners: {
                el: {
                    mouseover: function(event, target) {
                        target.style.cursor = 'pointer';
                    },
                    mouseenter: function(event, target) {
                        target.style.backgroundColor = '#f9f9f9';
                    },
                    mouseleave: function(event, target) {
                        target.style.backgroundColor = '#ffffff';
                    },
                    click: function(event, target) {
                        window.open('#/logging/data');
                    }
                }
            },
            style: {
                backgroundColor: '#ffffff'
            },
            layout: {
                type: 'vbox',
                align: 'center'
            },
            items: [
                {
                    xtype: 'image',
                    src: 'resources/images/datastorage.png',
                    margins: '5,0,0,0',
                    itemId: 'clipImage',
                    width: 48,
                    height: 48
                },
                {
                    xtype: 'component',
                    itemId: 'dataStorageTxt',
                    html: '<b>Data storage</b>'
                },
                {
                    xtype: 'component',
                    margins: '15,0,0,0',
                    itemId: 'dataStorageInfo',
                    html: 'Opens a new browser tab containing logging about:<ul><li>Data storage</li></ul>'
                }
            ]
        }
    ]
});