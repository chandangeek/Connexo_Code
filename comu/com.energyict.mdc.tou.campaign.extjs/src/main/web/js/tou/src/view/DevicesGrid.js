/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
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
    manuallyCancelled: null,
    viewConfig: {
        markDirty: false
    },

    initComponent: function () {
        var me = this;

        me.columns = [{
                header: Uni.I18n.translate('general.name', 'TOU', 'Name'),
                dataIndex: 'device',
                flex: 2,
                renderer: function (value) {
                    return value && value.name ? '<a href=#/devices/' + value.name.replace(" ", "%20") + '/timeofuse>' + value.name + '</a>' : '';
                }
            }, {
                header: Uni.I18n.translate('general.status', 'TOU', 'Status'),
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
                    case 'Configuration error':
                        iconCls = 'icon-notification';
                        break;
                    case 'Cancelled':
                        iconCls = 'icon-blocked';
                        break;
                    }
                    return value ? '<span class="' + iconCls + '"></span>' + value : '-';
                }
            }, {
                header: Uni.I18n.translate('general.startedOn', 'TOU', 'Started on'),
                dataIndex: 'startedOn',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                }
            }, {
                header: Uni.I18n.translate('general.finishedOn', 'TOU', 'Finished on'),
                dataIndex: 'finishedOn',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                }
            }, {
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: Tou.privileges.TouCampaign.administrate,
                isDisabled: function (view, rowIndex, colIndex, item, record) {
                    if (me.manuallyCancelled) {
                        return true;
                    }
                    switch (record.get('status')) { // current device status
                    case 'Pending':
                    case 'Cancelled':
                    case 'Failed':
                    case 'Configuration error':
                        return false; // because the device can be retried
                    default:
                        return true;
                    }
                },
                menu: {
                    xtype: 'tou-campaigns-device-action-menu',
                    itemId: 'tou-campaigns-device-action-menu',
                    manuallyCancelled: me.manuallyCancelled
                }
            }
        ];

        me.dockedItems = [{
                xtype: 'pagingtoolbartop',
                itemId: 'tou-campaigns-devices-grid-paging-toolbar-top',
                dock: 'top',
                store: me.store,
                needCustomExporter: true,
                displayMsg: Uni.I18n.translate('tou.campaigns.devices.pagingtoolbartop.displayMsg', 'TOU', '{0} - {1} of {2} devices'),
                displayMoreMsg: Uni.I18n.translate('tou.campaigns.devices.pagingtoolbartop.displayMoreMsg', 'TOU', '{0} - {1} of more than {2} devices'),
                emptyMsg: Uni.I18n.translate('tou.campaigns.devices.pagingtoolbartop.emptyMsg', 'TOU', 'There are no devices to display')
            }, {
                xtype: 'pagingtoolbarbottom',
                itemId: 'tou-campaigns-devices-grid-paging-toolbar-bottom',
                dock: 'bottom',
                store: me.store,
                needExtendedData: true,
                itemsPerPageMsg: Uni.I18n.translate('tou.campaigns.devices.pagingtoolbarbottom.itemsPerPage', 'TOU', 'Devices per page')
            }
        ];

        me.callParent(arguments);
    }
});
