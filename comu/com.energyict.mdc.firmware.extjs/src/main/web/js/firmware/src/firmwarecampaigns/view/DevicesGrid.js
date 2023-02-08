/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.DevicesGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Fwc.firmwarecampaigns.view.DeviceActionMenu'
    ],
    alias: 'widget.firmware-campaign-devices-grid',
    store: 'Fwc.firmwarecampaigns.store.Devices',
    router: null,
    overflowY: 'auto',
    maxHeight: 430,
    campaignIsOngoing: null,
    manuallyCancelled: null,
    viewConfig: {
        markDirty: false
    },

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'FWC', 'Name'),
                dataIndex: 'device',
                flex: 2,
                renderer: function (value) {
                    return value && value.name ? '<a href="' + me.router.getRoute('devices/device/firmware').buildUrl({deviceId: value.name}) + '">' + value.name + '</a>' : '';
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'FWC', 'Status'),
                dataIndex: 'status',
                flex: 1,
                renderer: function (value, metaData) {
                    var iconCls = '';
                    metaData.tdCls = 'firmware-campaign-status';
                    switch (value.id) {
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
                    return value ? '<span class="' + iconCls + '"></span>' + value.name : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.startedOn', 'FWC', 'Started on'),
                dataIndex: 'startedOn',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.finishedOn', 'FWC', 'Finished on'),
                dataIndex: 'finishedOn',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                }
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: Fwc.privileges.FirmwareCampaign.administrate,
                isDisabled: function (view, rowIndex, colIndex, item, record) {
                    if (!me.campaignIsOngoing || me.manuallyCancelled) {
                        return true;
                    }
                    switch (record.get('status').id) { // current device status
                        case 'PENDING':
                        case 'ONGOING':
                        case 'CANCELLED':
                        case 'FAILED':
                        case 'REJECTED':
                            return false; // because the device can be retried or cancelled
                        default:
                            return true;
                    }
                },
                menu: {
                    xtype: 'firmware-campaigns-device-action-menu',
                    itemId: 'firmware-campaigns-device-action-menu',
                    manuallyCancelled: me.manuallyCancelled
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                itemId: 'firmware-campaigns-devices-grid-paging-toolbar-top',
                dock: 'top',
                store: me.store,
                needCustomExporter: true,
                displayMsg: Uni.I18n.translate('firmware.campaigns.devices.pagingtoolbartop.displayMsg', 'FWC', '{0} - {1} of {2} devices'),
                displayMoreMsg: Uni.I18n.translate('firmware.campaigns.devices.pagingtoolbartop.displayMoreMsg', 'FWC', '{0} - {1} of more than {2} devices'),
                emptyMsg: Uni.I18n.translate('firmware.campaigns.devices.pagingtoolbartop.emptyMsg', 'FWC', 'There are no devices to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                itemId: 'firmware-campaigns-devices-grid-paging-toolbar-bottom',
                dock: 'bottom',
                needExtendedData: true,
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('firmware.campaigns.devices.pagingtoolbarbottom.itemsPerPage', 'FWC', 'Devices per page')
            }
        ];

        me.callParent(arguments);
    }
});
