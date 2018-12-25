/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.ProcessBulkBrowse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.process-bulk-browse',
    itemId: 'process-bulk-browse',
    componentCls: 'prc-bulk-browse',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.processes.view.bulk.ProcessesBulkWizard',
        'Mdc.processes.view.bulk.ProcessBulkNavigation'
    ],

    side: {
        itemId: 'Bulkpanel',
        xtype: 'panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'processesBulkNavigation',
                xtype: 'processses-bulk-navigation'
            }
        ]
    },

    content: [
        {
            xtype: 'process-bulk-wizard',
            itemId: 'process-bulk-wizard'
        }
    ]
});