/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.eventtype.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.eventtypeBrowse',
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
        'Cfg.view.eventtype.List'
    ],

    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>Event Types</h1>'
                },
                {
                    xtype: 'eventtypeList'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
