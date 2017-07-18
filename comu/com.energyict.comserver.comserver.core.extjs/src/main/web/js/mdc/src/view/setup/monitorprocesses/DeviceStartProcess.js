/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.monitorprocesses.DeviceStartProcess', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-processes-start',
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
                                xtype: 'deviceMenu',
                                itemId: 'steps-Menu',
                                device: me.device,
                                toggleId: 'processesLink'
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
                itemId: 'start-processes-panel',
                properties: me.properties
            }
        ];
        me.callParent(arguments);
    }
});

