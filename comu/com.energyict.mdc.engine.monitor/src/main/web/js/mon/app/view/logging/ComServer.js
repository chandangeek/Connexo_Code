/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.logging.ComServer', {
    extend: 'Ext.container.Container',
    xtype: 'comServer',
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    defaults: { flex: 1, margins: '0, 10, 0, 10' }, // top, right, bottom, left
    items: [
        {
            xtype: 'container',
            itemId: 'generalLoggingContainer',
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
                        window.open('#/logging/general');
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
                    src: 'resources/images/general.png',
                    margins: '5,0,0,0',
                    itemId: 'infoImage',
                    width: 48,
                    height: 48
                },
                {
                    xtype: 'component',
                    itemId: 'generalTxt',
                    html: '<b>General</b>'
                },
                {
                    xtype: 'component',
                    margins: '15,0,0,0',
                    itemId: 'generalInfo',
                    html: 'Opens a new browser tab containing logging about:<ul><li>Communication server changes (eg. a port was added/removed)</li><li>Database</li><li>Network</li></ul>'
                }
            ]
        }
    ]
});