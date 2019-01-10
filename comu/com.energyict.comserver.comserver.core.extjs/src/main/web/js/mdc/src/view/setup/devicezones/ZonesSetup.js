/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicezones.ZonesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-zones-setup',
    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.devicezones.ZonesGrid',
        'Mdc.view.setup.devicezones.ZonesPreview'
    ],
    deviceId: null,
    device: null,
    initComponent: function () {
        var me = this;
        me.deviceId = me.device.get('name');
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('deviceZone.overview.title', 'MDC', 'Zones'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'device-zones-grid',
                            itemId: 'grd-device-zones',
                            store: 'Mdc.store.DeviceZones',
                            device: me.device
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('deviceZones.overview.emptyMsg', 'MDC', 'No zone found'),
                            reasons: [
                                Uni.I18n.translate('deviceZones.overview.emptyReason', 'MDC', 'No zones have been created yet'),
                                Uni.I18n.translate('deviceZones.overview.emptyReason1', 'MDC', 'No zones have been linked to the device')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('deviceZones.overview.emptyStep', 'MDC', 'Add zone'),
                                    privileges: Cfg.privileges.Validation.adminZones,
                                    itemId: 'empty_grid_device-add-zone-button',
                                    deviceId: me.deviceId
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'device-zones-preview',
                            itemId: 'device-zones-preview'
                        }
                    }
                ]}
        ];
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        toggleId: 'device-zones-link',
                        device: me.device
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});


