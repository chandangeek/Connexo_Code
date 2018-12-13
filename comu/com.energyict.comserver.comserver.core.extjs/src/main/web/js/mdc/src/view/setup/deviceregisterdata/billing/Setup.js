/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.billing.Setup', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainSetup',
    alias: 'widget.deviceregisterreportsetup-billing',
    itemId: 'deviceregisterreportsetup',
    useMultiplier: false,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'panel',
                items: [
                    {
                        xtype: 'mdc-registers-topfilter',
                        itemId: 'deviceregistersdatafilterpanel'
                    },
                    {
                        items: [
                            {
                                xtype: 'preview-container',
                                grid: {
                                    xtype: 'deviceregisterreportgrid-billing',
                                    deviceId: me.deviceId,
                                    registerId: me.registerId,
                                    useMultiplier: me.useMultiplier
                                },
                                emptyComponent: {
                                    xtype: 'no-items-found-panel',
                                    itemId: 'ctr-no-register-data',
                                    title: Uni.I18n.translate('device.registerData.empty.title', 'MDC', 'No readings found'),
                                    reasons: [
                                        Uni.I18n.translate('device.registerData.list.item1', 'MDC', 'No readings have been defined yet.'),
                                        Uni.I18n.translate('device.registerData.list.item2', 'MDC', 'No readings comply with the filter.')
                                    ],
                                    stepItems: [
                                        {
                                            text:  Uni.I18n.translate('general.addReading','MDC','Add reading'),
                                            privileges: Mdc.privileges.Device.administrateDeviceData,
                                            href: '#/devices/' + encodeURIComponent(me.deviceId) + '/registers/' + me.registerId + '/data/add',
                                            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
                                        }
                                    ]
                                },
                                previewComponent: {
                                    xtype: 'deviceregisterreportpreview-billing',
                                    mentionDataLoggerSlave: me.mentionDataLoggerSlave,
                                    router: me.router
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});