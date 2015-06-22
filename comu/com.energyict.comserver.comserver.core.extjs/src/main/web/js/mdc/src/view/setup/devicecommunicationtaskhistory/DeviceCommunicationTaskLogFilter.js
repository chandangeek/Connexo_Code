Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskLogFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-view-setup-devicecommunicationtaskhistory-communicationlogfilter',

    store: 'DeviceCommunicationTaskLog',

    filters: [
        {
            type: 'combobox',
            dataIndex: 'logLevels',
            emptyText: Uni.I18n.translate('devicecommunicationtaskhistory.logLevel', 'MDC', 'Log level'),
            multiSelect: true,
            options: [
                {
                    display: Uni.I18n.translate('devicecommunicationtaskhistory.error', 'MDC', 'Error'),
                    value: 'Error'
                },
                {
                    display: Uni.I18n.translate('devicecommunicationtaskhistory.warning', 'MDC', 'Warning'),
                    value: 'Warning'
                },
                {
                    display: Uni.I18n.translate('devicecommunicationtaskhistory.information', 'MDC', 'Information'),
                    value: 'Information'
                },
                {
                    display: Uni.I18n.translate('devicecommunicationtaskhistory.debug', 'MDC', 'Debug'),
                    value: 'Debug'
                },
                {
                    display: Uni.I18n.translate('devicecommunicationtaskhistory.trace', 'MDC', 'Trace'),
                    value: 'Trace'
                }
            ]
        }
    ]
});