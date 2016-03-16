Ext.define('Fwc.firmwarecampaigns.view.DevicesStatusFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'fwc-firmwarecampaigns-view-devicesStatusFilter',
    store: 'Fwc.firmwarecampaigns.store.Devices',
    filters: [
        {
            type: 'combobox',
            dataIndex: 'status',
            emptyText: Uni.I18n.translate('general.status', 'FWC', 'Status'),
            itemId: 'status-filter',
            multiSelect: true,
            options: [
                {
                    display: Uni.I18n.translate('firmwareManagementDeviceStatus.success', 'FWC', 'Success'),
                    value: 'success'
                },
                {
                    display: Uni.I18n.translate('firmwareManagementDeviceStatus.failed', 'FWC', 'Failed'),
                    value: 'failed'
                },
                {
                    display: Uni.I18n.translate('firmwareManagementDeviceStatus.configurationError', 'FWC', 'Configuration Error'),
                    value: 'configurationError'
                },
                {
                    display: Uni.I18n.translate('firmwareManagementDeviceStatus.ongoing', 'FWC', 'On going'),
                    value: 'ongoing'
                },
                {
                    display: Uni.I18n.translate('firmwareManagementDeviceStatus.pending', 'FWC', 'Pending'),
                    value: 'pending'
                },
                {
                    display: Uni.I18n.translate('firmwareManagementDeviceStatus.cancelled', 'FWC', 'Cancelled'),
                    value: 'cancelled'
                }
            ]
        }
    ]
});