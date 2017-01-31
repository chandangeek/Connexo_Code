/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.changedevicelifecycle.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.change-device-life-cycle-browse',
    requires: [
        'Mdc.view.setup.devicetype.changedevicelifecycle.Navigation',
        'Mdc.view.setup.devicetype.changedevicelifecycle.Wizard'
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
                    xtype: 'change-device-life-cycle-navigation',
                    itemId: 'change-device-life-cycle-navigation'
                }
            ]
        };

        me.content = [
            {
                xtype: 'change-device-life-cycle-wizard',
                itemId: 'change-device-life-cycle-wizard',
                router: me.router
            }
        ];

        me.callParent(arguments);
    }
});