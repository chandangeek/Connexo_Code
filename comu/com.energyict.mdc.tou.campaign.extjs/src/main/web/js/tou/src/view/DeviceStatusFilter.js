/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.DeviceStatusFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'tou-view-deviceStatusFilter',
    store: 'Tou.store.Devices',

    initComponent: function () {
        var me = this,
            statusOptions = [
                {
                    display: Uni.I18n.translate('touManagementDeviceStatus.successful', 'TOU', 'Successful'),
                    value: 'SUCCESSFUL'
                },
                {
                    display: Uni.I18n.translate('touManagementDeviceStatus.failed', 'TOU', 'Failed'),
                    value: 'FAILED'
                },
                {
                    display: Uni.I18n.translate('touManagementDeviceStatus.rejected', 'TOU', 'Configuration Error'),
                    value: 'REJECTED'
                },
                {
                    display: Uni.I18n.translate('touManagementDeviceStatus.ongoing', 'TOU', 'Ongoing'),
                    value: 'ONGOING'
                },
                {
                    display: Uni.I18n.translate('touManagementDeviceStatus.pending', 'TOU', 'Pending'),
                    value: 'PENDING'
                },
                {
                    display: Uni.I18n.translate('touManagementDeviceStatus.cancelled', 'TOU', 'Cancelled'),
                    value: 'CANCELLED'
                }
            ];

        Ext.Array.sort(statusOptions, function(option1, option2) { // Sort them alphabetically by display value
            return option1.display.localeCompare(option2.display);
        });
        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'TOU', 'Status'),
                itemId: 'status-filter',
                multiSelect: true,
                options: statusOptions
            }
        ];
        me.callParent(arguments);
    }
});