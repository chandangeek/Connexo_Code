/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DeviceGeneralInformationPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceGeneralInformationPanel',
    overflowY: 'auto',
    itemId: 'devicegeneralinformationpanel',
    title: Uni.I18n.translate('deviceGeneralInformation.title', 'MDC', 'Device summary'),
    ui: 'tile',
    router: null,
    dataLoggerSlave: undefined,

    requires: [
      'Mdc.view.setup.device.DeviceAttributesForm'
    ],

    initComponent: function() {
        var me = this;

        me.items = [
            {
                xtype: 'deviceAttributesForm',
                itemId: 'deviceGeneralInformationForm',
                router: me.router,
                dataLoggerSlave: me.dataLoggerSlave
            }
        ];

        me.bbar = {
            xtype: 'container',
            itemId: 'mdc-device-summary-bbar',
            items: [
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('deviceGeneralInformation.manageLinkText', 'MDC', 'Manage device attributes'),
                    ui: 'link',
                    itemId: 'view-more-general-information-link',
                    handler: function() {
                        me.router.getRoute('devices/device/attributes').forward();
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

