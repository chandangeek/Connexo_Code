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
                    display: 'Successful',
                    value: 'SUCCESSFUL'
                },
                {
                    display: 'Failed',
                    value: 'FAILED'
                },
                {
                    display: 'Configuration Error',
                    value: 'REJECTED'
                },
                {
                    display: 'Ongoing',
                    value: 'ONGOING'
                },
                {
                    display: 'Pending',
                    value: 'PENDING'
                },
                {
                    display: 'Cancelled',
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
                emptyText: 'Status',
                itemId: 'status-filter',
                multiSelect: true,
                options: statusOptions
            }
        ];
        me.callParent(arguments);
    }
});