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
    viewConfig: {
        markDirty:false
    },

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'FWC', 'Name'),
                dataIndex: 'device',
                flex: 2,
                renderer: function (value) {
                    return value && value.name ? '<a href="' + me.router.getRoute('devices/device/firmware').buildUrl({deviceId: value.name}) +'">' + value.name + '</a>' : '';
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'FWC', 'Status'),
                dataIndex: 'status',
                flex: 1,
                renderer: function (value, metaData) {
                    var iconCls = '';
                    //TODO: format should be changed
                    metaData.tdCls = 'firmware-campaign-status';
                    switch (value) {
                            case 'Failed':
                                iconCls = 'icon-cancel-circle';
                                break;
                            case 'Successful':
                                iconCls = 'icon-checkmark-circle';
                                break;
                            case 'Ongoing':
                                iconCls = 'icon-spinner3';
                                break;
                            case 'Pending':
                                iconCls = 'icon-forward2';
                                break;
                            case 'Configuration error':
                                iconCls = 'icon-notification';
                                break;
                            case 'Cancelled':
                                iconCls = 'icon-blocked';
                                break;
                    }
                    return value ? '<span class="' + iconCls + '"></span>' + value : '-';
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
                isDisabled: function(view, rowIndex, colIndex, item, record) {
                    if (!me.campaignIsOngoing) {
                        return true;
                    }
                    //TODO: format should be changed
                    switch (record.get('status')) { // current device status
                        case 'Pending':
                        case 'Ongoing':
                            return false; // because the device can be skipped
                        case 'Cancelled':
                        case 'Failed':
                        case 'Configuration error':
                            return false; // because the device can be retried
                        default:
                            return true;
                    }
                },
                menu: {
                    xtype: 'firmware-campaigns-device-action-menu',
                    itemId: 'firmware-campaigns-device-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                itemId: 'firmware-campaigns-devices-grid-paging-toolbar-top',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('firmware.campaigns.devices.pagingtoolbartop.displayMsg', 'FWC', '{0} - {1} of {2} devices'),
                displayMoreMsg: Uni.I18n.translate('firmware.campaigns.devices.pagingtoolbartop.displayMoreMsg', 'FWC', '{0} - {1} of more than {2} devices'),
                emptyMsg: Uni.I18n.translate('firmware.campaigns.devices.pagingtoolbartop.emptyMsg', 'FWC', 'There are no devices to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                itemId: 'firmware-campaigns-devices-grid-paging-toolbar-bottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('firmware.campaigns.devices.pagingtoolbarbottom.itemsPerPage', 'FWC', 'Devices per page')
            }
        ];

        me.callParent(arguments);
    }
});