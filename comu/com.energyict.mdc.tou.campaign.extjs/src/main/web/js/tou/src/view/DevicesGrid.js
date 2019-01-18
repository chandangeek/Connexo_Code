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
                    return value ? '<a href="' + /*me.router.getRoute('devices/device/tou').buildUrl({deviceId: value.id})*/ + '1' +'">' + value.name + '</a>' : '';
                }
            },
            {
                header: 'Status',
                dataIndex: 'status',
                flex: 1,
                renderer: function (value, metaData) {
                    var iconCls = '';

                    metaData.tdCls = 'tou-campaign-status';
                    switch (value) {
                        case 'Failed':
                            iconCls = 'icon-cancel-circle';
                            break;
                        case 'Success':
                            iconCls = 'icon-checkmark-circle';
                            break;
                        case 'Ongoing':
                            iconCls = 'icon-spinner3';
                            break;
                        case 'Pending':
                            iconCls = 'icon-forward2';
                            break;
                        case 'Configuration Error':
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
                privileges: Tou.privileges.TouCampaign.administrate,
                isDisabled: function(view, rowIndex, colIndex, item, record) {
                    if (!me.campaignIsOngoing) {
                        return true;
                    }
                    switch (record.get('status')) { // current device status
                        case 'Pending':
                        case 'Ongoing':
                            return false;
                        case 'Cancelled':
                        case 'Failed':
                        case 'Configuration Error':
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