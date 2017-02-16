/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.connectionsbulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.connections-bulk-browse',
    requires: [
        'Dsh.view.connectionsbulk.Navigation',
        'Dsh.view.connectionsbulk.Wizard'
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
                    xtype: 'connections-bulk-navigation',
                    itemId: 'connections-bulk-navigation'
                }
            ]
        };

        me.content = [
            {
                xtype: 'connections-bulk-wizard',
                itemId: 'connections-bulk-wizard',
                router: me.router
            }
        ];

        me.callParent(arguments);
    }
});