Ext.define('Mdc.view.setup.deviceregisterdata.RegisterTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-registers-topfilter',

    filters: [
        {
            type: 'duration',
            dataIndex: 'interval',
            dataIndexFrom: 'intervalStart',
            dataIndexTo: 'intervalEnd',
            defaultFromDate: moment().startOf('day').subtract(1,'years').toDate(),
            defaultDuration: '1years',
            text: Uni.I18n.translate('communications.widget.topfilter.startedDate', 'DSH', 'Start date')
        },
        {
            type: 'checkbox',
            dataIndex: 'suspect',
            layout: 'hbox',
            defaults: {margin: '0 10 0 0'},
            emptyText: Uni.I18n.translate('communications.widget.topfilter.validationResult', 'MDC', 'Validation result'),
            options: [
                {
                    display: Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect'),
                    value: 'suspect'
                },
                {
                    display: Uni.I18n.translate('validationStatus.ok', 'MDC', 'Not suspect'),
                    value: 'nonSuspect'
                }
            ]
        }
    ]
});