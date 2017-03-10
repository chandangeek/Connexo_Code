/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dataLoggerSlavesSetup',
    itemId: 'mdc-dataLoggerSlavesSetup',
    router: null,
    device: null,

    requires: [
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
            title: Uni.I18n.translate('general.dataLoggerSlaves', 'MDC', 'Data logger slaves'),
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
                        title: Uni.I18n.translate('dataLoggerSlavesGrid.empty.title', 'MDC', 'No data logger slaves found'),
                        reasons: [
                            Uni.I18n.translate('dataLoggerSlavesGrid.empty.reason1', 'MDC', 'No data logger slaves have been linked yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.linkDataLoggerSlave', 'MDC', 'Link data logger slave'),
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