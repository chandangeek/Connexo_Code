/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicezones.ZonesPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-zones-preview',
    requires: [
        'Mdc.view.setup.devicezones.ZonesActionsMenu'
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
                    itemId: 'actionsPreviewBtn',
                    menu: {
                        xtype: 'device-zones-action-menu'
                    }
                }
            ],

            me.items = [
            {
                xtype: 'form',
                border: false,
                itemId: 'device-zones-preview-form',
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
                                        name: 'zoneTypeName',
                                        fieldLabel: Uni.I18n.translate('devicezones.zoneType', 'MDC', 'Zone type'),
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'zoneName',
                                        fieldLabel: Uni.I18n.translate('general.zone', 'MDC', 'Zone'),

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