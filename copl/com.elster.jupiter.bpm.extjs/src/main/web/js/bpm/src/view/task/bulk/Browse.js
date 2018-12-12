/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.bulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tasks-bulk-browse',
    requires: [
        'Bpm.view.task.bulk.Navigation',
        'Bpm.view.task.bulk.Wizard'
    ],
    router: null,
    initComponent: function () {
        var me = this;

        me.side = {
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'tasks-bulk-navigation',
                    itemId: 'tasks-bulk-navigation'
                }
            ]
        };

        me.content = [
            {
                xtype: 'tasks-bulk-wizard',
                itemId: 'tasks-bulk-wizard',
                router: me.router
            }
        ];

        me.callParent(arguments);
    }
});