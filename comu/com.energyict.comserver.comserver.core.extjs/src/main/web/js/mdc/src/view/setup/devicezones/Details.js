/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicezones.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.zone-details',
    xtype: 'device-zone-details',
    requires: [
        'Yfn.privileges.Yellowfin',
        'Mdc.view.setup.devicezones.DeviceZoneActionMenu',
        'Mdc.view.setup.devicezones.ZonesActionsMenuFromZone',
        'Mdc.view.setup.devicezones.DevicesOfZoneGrid',
        'Mdc.view.setup.devicezones.PreviewForm',
        'Mdc.view.setup.devicezones.DeviceZonePreview',
        'Uni.view.container.EmptyGridContainer',
        'Uni.util.FormEmptyMessage',
    ],

    router: null,
    device: null,

    initComponent: function () {
        var me = this;
        me.content = {
            xtype: 'container',
                layout: {
                type: 'vbox',
                    align: 'stretch'
            },

            items: [
                {
                    xtype: 'container',
                    layout: 'hbox',
                    items: [
                        {
                            ui: 'large',
                            title: Uni.I18n.translate('devicezones.view.Zone', 'MDC', 'Zone'),
                            flex: 1,
                            itemId: 'device-zone-title',
                            items: {
                                xtype: 'devicezones-preview-form',
                                itemId: 'deviceZoneDetailsForm',
                                deviceZoneId: this.deviceZoneId
                            }
                        },
                        {
                            xtype: 'uni-button-action',
                            itemId: 'deviceGroupDetailsActionMenu',
                            margin: '20 0 0 0',
                            menu: {
                                xtype: 'device-zones-action-menu-from-zone'
                            }
                        }
                    ]
                },
                {
                    xtype: 'preview-container',
                    itemId: 'search-preview-container',
                    grid: {
                        xtype: 'zone-details-grid',
                        itemId: 'zone-details-devices-grid'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('devicezones.empty.list.message', 'MDC', 'There are no devices linked to this zone.')
                    },
                    previewComponent: {
                        xtype: 'device-zone-preview',
                        itemId: 'device-zone-preview'
                    }
                }
            ]
        };

        this.callParent(arguments);
    }

});


