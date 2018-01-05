/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.bulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.reading-types-bulk-browse',
    itemId: 'reading-types-bulk-browse',
    requires: [
        'Mtr.view.bulk.Navigation',
        'Mtr.view.bulk.Wizard'
    ],
    side: {
        itemId: 'reading-types-bulk-panel',
        xtype: 'panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'reading-types-bulk-navigation',
                xtype: 'reading-types-bulk-navigation'
            }
        ]
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'reading-types-wizard',
                itemId: 'reading-types-wizard'
            }
        ];

        this.callParent(arguments);
    }
});