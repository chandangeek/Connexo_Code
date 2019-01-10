/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.DevicesGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Tou.view.DeviceActionMenu'
    ],
    alias: 'widget.tou-campaign-devices-grid',
    store: 'Tou.store.Devices',
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
                header: 'Name',
                dataIndex: 'device',
                flex: 2,
                renderer: function (value) {
                    return value ? '<a href="' + /*me.router.getRoute('devices/device/firmware').buildUrl({deviceId: value.id})*/ + '1' +'">' + value.name + '</a>' : '';
                }
            },
            {
                header: 'Status',
                dataIndex: 'status',
                flex: 1,
                renderer: function (value, metaData) {
                    var iconCls = '';

                    metaData.tdCls = 'firmware-campaign-status';
                    switch (value.id) {
                        case 'failed':
                            iconCls = 'icon-cancel-circle';
                            break;
                        case 'success':
                            iconCls = 'icon-checkmark-circle';
                            break;
                        case 'ongoing':
                            iconCls = 'icon-spinner3';
                            break;
                        case 'pending':
                            iconCls = 'icon-forward2';
                            break;
                        case 'configurationError':
                            iconCls = 'icon-notification';
                            break;
                        case 'cancelled':
                            iconCls = 'icon-blocked';
                            break;
                    }
                    return value ? '<span class="' + iconCls + '"></span>' + value.name : '-';
                }
            },
            {
                header: 'Started on',
                dataIndex: 'startedOn',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                }
            },
            {
                header: 'Finished on',
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
                    switch (record.get('status').id) { // current device status
                        case 'pending':
                        case 'ongoing':
                            return false; // because the device can be skipped
                        case 'cancelled':
                        case 'failed':
                        case 'configurationError':
                            return false; // because the device can be retried
                        default:
                            return true;
                    }
                },
                menu: {
                    xtype: 'tou-campaigns-device-action-menu',
                    itemId: 'tou-campaigns-device-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                itemId: 'tou-campaigns-devices-grid-paging-toolbar-top',
                dock: 'top',
                store: me.store,
                displayMsg: '{0} - {1} of {2} devices',
                displayMoreMsg: '{0} - {1} of more than {2} devices',
                emptyMsg: 'There are no devices to display'
            },
            {
                xtype: 'pagingtoolbarbottom',
                itemId: 'tou-campaigns-devices-grid-paging-toolbar-bottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: 'Devices per page'
            }
        ];

        me.callParent(arguments);
    }
});