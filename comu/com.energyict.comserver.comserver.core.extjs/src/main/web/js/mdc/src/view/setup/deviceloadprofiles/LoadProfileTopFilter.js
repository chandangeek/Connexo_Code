Ext.define('Mdc.view.setup.deviceloadprofiles.LoadProfileTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-loadprofiles-topfilter',
    store: 'Mdc.store.LoadProfilesOfDeviceData',

    filters: [
        {
            type: 'interval',
            dataIndex: 'interval',
            dataIndexFrom: 'intervalStart',
            dataIndexTo: 'intervalEnd',
            text: Uni.I18n.translate('communications.widget.topfilter.startedBetween', 'DSH', 'Started between')
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