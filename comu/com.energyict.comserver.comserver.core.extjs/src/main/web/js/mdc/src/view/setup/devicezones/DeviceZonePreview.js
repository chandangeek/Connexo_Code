/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicezones.DeviceZonePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-zone-preview',

    requires: [
        'Mdc.view.setup.devicezones.DeviceZoneActionMenu'
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: Uni.I18n.translate('general.details','MDC','Details'),

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                itemId: 'actionsDeviceZonePreviewBtn',
                menu: {
                    xtype: 'device-of-zone-action-menu'
                }
            }
        ],

        me.items = [
            {
                xtype: 'form',
                border: false,
                itemId: 'device-zone-preview-form',
                layout: {
                    type: 'vbox'
                },
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.49,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'name',
                                        fieldLabel: Uni.I18n.translate('general.device', 'MDC', 'Device'),
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'deviceTypeName',
                                        fieldLabel: Uni.I18n.translate('general.deviceType', 'MDC', 'Device Type'),
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'deviceConfigurationName',
                                        fieldLabel: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device Configuration'),
                                    },
                                ]
                            },
                        ]
                    }
                ]
            }
        ],

        this.callParent(arguments);
    }
});