/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterconfiguration.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceRegisterConfigurationSetup',
    itemId: 'deviceRegisterConfigurationSetup',
    device: null,
    router: null,
    controller: undefined,

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.deviceregisterconfiguration.RegistersView',
        'Mdc.view.setup.deviceregisterconfiguration.RegisterReadingsView'
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
                        toggleId: 'registersLink'
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'tabpanel',
                itemId: 'mdc-registers-tabPanel',
                ui: 'large',
                title: Uni.I18n.translate('general.registers', 'MDC', 'Registers'),
                activeTab: 'mdc-registers-' + me.activeTab,
                items: [
                    {
                        title: Uni.I18n.translate('general.registers', 'MDC', 'Registers'),
                        itemId: 'mdc-registers-registers',
                        listeners: {
                            activate: me.controller.showRegistersTab,
                            scope: me.controller
                        }
                    },
                    {
                        title: Uni.I18n.translate('general.registerReadings', 'MDC', 'Register readings'),
                        itemId: 'mdc-registers-readings',
                        listeners: {
                            activate: me.controller.showReadingsTab,
                            scope: me.controller
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


