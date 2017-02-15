/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.communicationsbulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communications-bulk-browse',
    requires: [
        'Dsh.view.communicationsbulk.Navigation',
        'Dsh.view.communicationsbulk.Wizard'
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
                    xtype: 'communications-bulk-navigation',
                    itemId: 'communications-bulk-navigation'
                }
            ]
        };

        me.content = [
            {
                xtype: 'communications-bulk-wizard',
                itemId: 'communications-bulk-wizard',
                router: me.router
            }
        ];

        me.callParent(arguments);
    }
});