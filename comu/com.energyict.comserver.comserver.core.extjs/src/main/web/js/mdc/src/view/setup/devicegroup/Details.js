/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.group-details',
    xtype: 'device-groups-details',
    requires: [
        'Yfn.privileges.Yellowfin',
        'Mdc.view.setup.devicegroup.Menu',
        'Mdc.view.setup.devicegroup.DeviceGroupPreview',
        'Mdc.view.setup.devicegroup.DeviceGroupActionMenu',
        'Mdc.view.setup.devicegroup.PreviewForm',
        'Uni.view.container.EmptyGridContainer',
        'Mdc.view.setup.devicegroup.DevicesOfDeviceGroupGrid',
        'Uni.util.FormEmptyMessage'
    ],

    router: null,
    deviceGroupId: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'devicegroups-menu',
                        itemId: 'devicegroups-view-menu',
                        router: me.router,
                        deviceGroupId: me.deviceGroupId
                    }
                ]
            }
        ];

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
                            title: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                            flex: 1,
                            items: {
                                xtype: 'devicegroups-preview-form',
                                itemId: 'deviceGroupdetailsForm',
                                deviceGroupId: this.deviceGroupId
                            }
                        },
                        {
                            xtype:'button',
                            itemId:'generate-report',
                            privileges: Yfn.privileges.Yellowfin.view,
                            margin: '20 10 0 0',
                            text:Uni.I18n.translate('generatereport.generateReportButton', 'MDC', 'Generate report')
                        },
                        {
                            xtype: 'uni-button-action',
                            itemId: 'deviceGroupDetailsActionMenu',
                            margin: '20 0 0 0',
                            menu: {
                                xtype: 'device-group-action-menu'
                            }
                        }
                    ]
                },
                {
                    xtype: 'preview-container',
                    itemId: 'search-preview-container',
                    grid: {
                        xtype: 'devicesOfDeviceGroupGrid',
                        service: me.service
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('devicegroup.empty.list.message', 'MDC', 'There are no devices in your group.')
                    }
                }
            ]
        };

        this.callParent(arguments);
    }

});


