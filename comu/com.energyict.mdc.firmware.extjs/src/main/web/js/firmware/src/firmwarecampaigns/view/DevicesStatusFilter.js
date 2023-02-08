/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.DevicesStatusFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'fwc-firmwarecampaigns-view-devicesStatusFilter',
    store: 'Fwc.firmwarecampaigns.store.Devices',

    initComponent: function () {
        var me = this,
            statusOptions = [
                {
                    display: Uni.I18n.translate('firmwareManagementDeviceStatus.successful', 'FWC', 'Successful'),
                    value: 'SUCCESSFUL'
                },
                {
                    display: Uni.I18n.translate('firmwareManagementDeviceStatus.failed', 'FWC', 'Failed'),
                    value: 'FAILED'
                },
                {
                    display: Uni.I18n.translate('firmwareManagementDeviceStatus.configurationError', 'FWC', 'Configuration Error'),
                    value: 'REJECTED'
                },
                {
                    display: Uni.I18n.translate('firmwareManagementDeviceStatus.ongoing', 'FWC', 'Ongoing'),
                    value: 'ONGOING'
                },
                {
                    display: Uni.I18n.translate('firmwareManagementDeviceStatus.pending', 'FWC', 'Pending'),
                    value: 'PENDING'
                },
                {
                    display: Uni.I18n.translate('firmwareManagementDeviceStatus.cancelled', 'FWC', 'Cancelled'),
                    value: 'CANCELLED'
                }
            ];

        Ext.Array.sort(statusOptions, function (option1, option2) { // Sort them alphabetically by display value
            return option1.display.localeCompare(option2.display);
        });
        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'FWC', 'Status'),
                itemId: 'status-filter',
                multiSelect: true,
                options: statusOptions
            }
        ];
        me.callParent(arguments);
    }
});