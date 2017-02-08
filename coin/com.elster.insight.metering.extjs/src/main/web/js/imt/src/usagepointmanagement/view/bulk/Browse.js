/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.bulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepoints-bulk-browse',
    itemId: 'usagepoints-bulk-browse',
    requires: [
        'Imt.usagepointmanagement.view.bulk.Navigation',
        'Imt.usagepointmanagement.view.bulk.Wizard'
    ],
    side: {
        itemId: 'usagePointsBulkPanel',
        xtype: 'panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'usagePointsBulkNavigation',
                xtype: 'usagepoints-bulk-navigation'
            }
        ]
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'usagepoints-wizard',
                itemId: 'usagepointswizard',
                deviceStore: this.deviceStore,
                defaults: {
                    cls: 'content-wrapper'
                }
            }
        ];

        this.callParent(arguments);
    }
});
