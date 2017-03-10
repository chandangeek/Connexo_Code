/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.DeviceTypesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTypesSetup',
    itemId: 'deviceTypeSetup',
    purposeStore: null,

    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
                items: [
                    {
                        xtype: 'preview-container',
                        margin: '0 1 0 0',
                        grid: {
                            xtype: 'deviceTypesGrid',
                            purposeStore: me.purposeStore,
                            itemId: 'devicetypegrid'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-no-device-type',
                            title: Uni.I18n.translate('deviceType.empty.title', 'MDC', 'No device types found'),
                            reasons: [
                                Uni.I18n.translate('deviceType.empty.list.item1', 'MDC', 'No device types have been defined yet.'),
                                Uni.I18n.translate('deviceType.empty.list.item2', 'MDC', 'Device types exist, but you do not have permission to view them.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('deviceType.add', 'MDC', 'Add device type'),
                                    privileges: Mdc.privileges.DeviceType.admin,
                                    action: 'createDeviceType'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceTypePreview',
                            purposeStore: me.purposeStore
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


