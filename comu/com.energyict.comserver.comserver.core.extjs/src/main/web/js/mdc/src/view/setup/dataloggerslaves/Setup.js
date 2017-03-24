/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dataLoggerSlavesSetup',
    itemId: 'mdc-dataLoggerSlavesSetup',
    router: null,
    device: null,
    purpose: undefined,
    requires: [
        'Mdc.util.LinkPurpose',
        'Mdc.view.setup.dataloggerslaves.DataLoggerSlavesGrid',
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    stores: [
        'Mdc.store.DataLoggerSlaves'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'device-dataLoggerSlaves-link'
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: me.purpose.pageTitle,
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'dataLoggerSlavesGrid',
                        router: me.router,
                        store: me.store
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId : 'no-items-found',
                        title: me.purpose.noItemsFoundText,
                        reasons: me.purpose.noItemsFoundReasons,
                        stepItems: [
                            {
                                text: me.purpose.displayValue,
                                itemId: 'mdc-link-dataloggerslave-btn',
                                action: 'linkDataLoggerSlave',
                                privileges: Mdc.privileges.Device.administrateDevice
                            }
                        ]
                    }
                }
            ]
        };
        me.callParent(arguments);
    }
});