Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionLogFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-view-setup-deviceconnectionhistory-connectionlogfilter',

    store: 'DeviceConnectionLog',

    filters: [
        {
            type: 'combobox',
            dataIndex: 'logLevels',
            emptyText: Uni.I18n.translate('deviceconnectionhistory.logLevel', 'MDC', 'Log level'),
            multiSelect: true,
            options: [
                {
                    display: Uni.I18n.translate('deviceconnectionhistory.error', 'MDC', 'Error'),
                    value: 'Error'
                },
                {
                    display: Uni.I18n.translate('deviceconnectionhistory.warning', 'MDC', 'Warning'),
                    value: 'Warning'
                },
                {
                    display: Uni.I18n.translate('deviceconnectionhistory.information', 'MDC', 'Information'),
                    value: 'Information'
                },
                {
                    display: Uni.I18n.translate('deviceconnectionhistory.debug', 'MDC', 'Debug'),
                    value: 'Debug'
                },
                {
                    display: Uni.I18n.translate('deviceconnectionhistory.trace', 'MDC', 'Trace'),
                    value: 'Trace'
                }
            ]
        },
        {
            type: 'combobox',
            dataIndex: 'communications',
            emptyText: Uni.I18n.translate('deviceconnectionhistory.logType', 'MDC', 'Log type'),
            multiSelect: true,
            options: [
                {
                    display: Uni.I18n.translate('deviceconnectionhistory.connection', 'MDC', 'Connections'),
                    value: 'Connections'
                },
                {
                    display: Uni.I18n.translate('deviceconnectionhistory.communicationTask', 'MDC', 'Communications'),
                    value: 'Communications'
                }
            ]
        }
    ]
});