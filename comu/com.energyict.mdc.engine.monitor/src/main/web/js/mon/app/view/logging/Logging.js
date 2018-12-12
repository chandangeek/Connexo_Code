/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.logging.Logging', {
    extend: 'Ext.container.Container',
    xtype: 'logging',
    requires: [
        'CSMonitor.view.logging.ComServer',
        'CSMonitor.view.logging.DataStorage',
        'CSMonitor.view.logging.Communication'
    ],
    layout: {
       type: 'table',
       columns: 2,
       tableAttrs: {
          style: {
             width: '100%',
             height: '100%'
          }
       }
    },
    autoScroll: true, // show scroll bars whenever needed
    items: [
        {
            xtype: 'component',
            margins: '0 0 0 10', // top, right, bottom, left
            itemId: 'comServerLogging',
            html: '<h2>Communication server logging</h2>',
            colspan: 2
        },
        {
            xtype: 'comServer',
            layout: 'fit'
        },
        {
            xtype: 'dataStorage',
            layout: 'fit'
        },
        {
            xtype: 'component',
            margins: '0 0 0 10', // top, right, bottom, left
            itemId: 'comLogging',
            html: '<h2>Communication logging</h2>',
            colspan: 2
        },
        {
            xtype: 'communication',
            width: 'fit'
        }
    ]
});
