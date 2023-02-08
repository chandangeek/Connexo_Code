/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.grid.column.Action',
        'Fwc.firmwarecampaigns.view.ActionMenu',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.panel.FilterToolbar'
    ],
    alias: 'widget.firmware-campaigns-grid',
    store: 'Fwc.firmwarecampaigns.store.FirmwareCampaigns',
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'FWC', 'Name'),
                dataIndex: 'name',
                flex: 2,
                renderer: function (value, metaData, record) {
                    return value ? '<a href="' + me.router.getRoute('workspace/firmwarecampaigns/firmwarecampaign').buildUrl({firmwareCampaignId: record.getId()}) + '">' + Ext.String.htmlEncode(value) + '</a>' : '';
                }
            },
            {
                header: Uni.I18n.translate('general.deviceType', 'FWC', 'Device type'),
                dataIndex: 'deviceType',
                flex: 1,
                renderer: function (value) {
                    return value ? Ext.String.htmlEncode(value.localizedValue) : '';
                }
            },
            {
                header: Uni.I18n.translate('general.firmwareType', 'FWC', 'Firmware type'),
                dataIndex: 'firmwareType',
                flex: 1,
                renderer: function (value) {
                    return value ? value.localizedValue : '';
                }
            },
            {
                header: Uni.I18n.translate('general.version', 'FWC', 'Version'),
                dataIndex: 'firmwareVersion',
                flex: 1,
                renderer: function (value) {
                    return value && value.firmwareVersion ? Ext.String.htmlEncode(value.firmwareVersion) : '';
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'FWC', 'Status'),
                dataIndex: 'status',
                flex: 1,
                renderer: function (value) {
                    return value ? value.name : '';
                }
            },
            {
                header: Uni.I18n.translate('general.devices', 'FWC', 'Devices'),
                dataIndex: 'devices',
                flex: 2,
                renderer: function (value, metaData) {
                    var result = '';

                    if (!Ext.isArray(value)) {
                        return result;
                    }

                    metaData.tdCls = 'firmware-campaign-status';
                    Ext.Array.each(value, function (devicesStatus) {
                        var iconCls = '';
                        switch (devicesStatus.status.id) {
                            case 'FAILED':
                                iconCls = 'icon-cancel-circle';
                                break;
                            case 'SUCCESSFUL':
                                iconCls = 'icon-checkmark-circle';
                                break;
                            case 'ONGOING':
                                iconCls = 'icon-spinner3';
                                break;
                            case 'PENDING':
                                iconCls = 'icon-forward2';
                                break;
                            case 'REJECTED':
                                iconCls = 'icon-notification';
                                break;
                            case 'CANCELLED':
                                iconCls = 'icon-blocked';
                                break;
                        }

                        result += '<span class="' + iconCls + '" data-qtip="' + devicesStatus.status.name + '"></span><span style="margin-right: 10px">' + devicesStatus.quantity + '</span>';
                    });
                    return result;
                }
            },
            {
                header: Uni.I18n.translate('general.startedOn', 'FWC', 'Started on'),
                dataIndex: 'startedOn',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                }
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: Fwc.privileges.FirmwareCampaign.administrate,
                isDisabled: function (view, rowIndex, colIndex, item, record) {
                    return record.get('status').id !== 'ONGOING';
                },
                menu: {
                    xtype: 'firmware-campaigns-action-menu',
                    itemId: 'firmware-campaigns-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                itemId: 'firmware-campaigns-grid-paging-toolbar-top',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('firmware.campaigns.pagingtoolbartop.displayMsg', 'FWC', '{0} - {1} of {2} firmware campaigns'),
                displayMoreMsg: Uni.I18n.translate('firmware.campaigns.pagingtoolbartop.displayMoreMsg', 'FWC', '{0} - {1} of more than {2} firmware campaigns'),
                emptyMsg: Uni.I18n.translate('firmware.campaigns.pagingtoolbartop.emptyMsg', 'FWC', 'There are no firmware campaigns to display'),
                items: [
                    {
                        itemId: 'firmware-campaigns-add-button',
                        text: Uni.I18n.translate('firmware.campaigns.addFirmwareCampaign', 'FWC', 'Add firmware campaign'),
                        action: 'addFirmwareCampaign',
                        href: me.router.getRoute('workspace/firmwarecampaigns/add').buildUrl(),
                        privileges: Fwc.privileges.FirmwareCampaign.administrate
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                itemId: 'firmware-campaigns-grid-paging-toolbar-bottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('firmware.campaigns.pagingtoolbarbottom.itemsPerPage', 'FWC', 'Firmware campaigns per page')
            }
        ];

        me.callParent(arguments);
    }
});