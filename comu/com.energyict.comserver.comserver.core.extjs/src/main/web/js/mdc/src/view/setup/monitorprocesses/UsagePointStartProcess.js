/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.monitorprocesses.UsagePointStartProcess', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-processes-start',
    requires: [
        'Bpm.startprocess.view.StartProcess'
    ],
    device: null,
    properties: null,
    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        ui: 'medium',
                        items: [
                            {
                                xtype: 'usage-point-management-side-menu',
                                itemId: 'usage-point-management-side-menu',
                                router: me.router,
                                deviceId: me.deviceId
                            }
                        ]
                    }
                ]
            }
        ];
        me.content = [
            {
                xtype: 'bpm-start-processes-panel',
                ui: 'large',
                itemId: 'usage-point-start-processes-panel',
                properties: me.properties
            }
        ];
        me.callParent(arguments);
    }
});

