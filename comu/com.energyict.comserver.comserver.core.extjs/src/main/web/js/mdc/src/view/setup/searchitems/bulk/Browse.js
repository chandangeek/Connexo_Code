/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.searchitems.bulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.searchitems-bulk-browse',
    itemId: 'searchitems-bulk-browse',
    requires: [
        'Mdc.view.setup.searchitems.bulk.Navigation',
        'Mdc.view.setup.searchitems.bulk.Wizard'
    ],
    side: {
        itemId: 'searchitemsBulkpanel',
        xtype: 'panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'searchitemsBulkNavigation',
                xtype: 'searchitems-bulk-navigation'
            }
        ]
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'searchitems-wizard',
                itemId: 'searchitemswizard',
                deviceStore: this.deviceStore,
                defaults: {
                    cls: 'content-wrapper'
                }
            }
        ];

        this.callParent(arguments);
    }
});