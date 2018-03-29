/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.view.bulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.certificates-bulk-browse',
    itemId: 'certificates-bulk-browse',
    requires: [
        'Pkj.view.bulk.Navigation',
        'Pkj.view.bulk.Wizard'
    ],
    side: {
        itemId: 'certificates-bulk-panel',
        xtype: 'panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'certificates-bulk-navigation',
                xtype: 'certificates-bulk-navigation'
            }
        ]
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'certificates-wizard',
                itemId: 'certificates-wizard'
            }
        ];

        this.callParent(arguments);
    }
});