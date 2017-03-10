/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.text.Setup', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainSetup',
    alias: 'widget.deviceregisterreportsetup-text',
    itemId: 'deviceregisterreportsetup',

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
                                    xtype: 'deviceregisterreportgrid-text',
                                    deviceId: me.deviceId,
                                    registerId: me.registerId
                                },
                                emptyComponent: {
                                    xtype: 'no-items-found-panel',
                                    itemId: 'ctr-no-register-data',
                                    title: Uni.I18n.translate('device.registerData.noData', 'MDC', 'No readings found'),
                                    reasons: [
                                        Uni.I18n.translate('device.registerData.list.item1', 'MDC', 'No readings have been defined yet.'),
                                        Uni.I18n.translate('device.registerData.list.item2', 'MDC', 'No readings comply with the filter.')
                                    ],
                                    stepItems: [
                                        {
                                            text: Uni.I18n.translate('general.addReading', 'MDC', 'Add reading'),
                                            privileges: Mdc.privileges.Device.administrateDeviceData,
                                            href: '#/devices/' + encodeURIComponent(me.deviceId) + '/registers/' + me.registerId + '/data/add',
                                            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
                                        }
                                    ]
                                },
                                previewComponent: {
                                    xtype: 'deviceregisterreportpreview-text',
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