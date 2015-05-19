Ext.define('Mdc.view.setup.deviceloadprofiles.IntervalFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-view-setup-deviceloadprofiles-intervalfilter',

    // TODO Integrate the filter on screens.
    store: 'Mdc.store.LoadProfilesOfDevice',

    filters: [
        {
            type: 'interval',
            dataIndex: 'intervalStart',
            emptyText: Uni.I18n.translate('deviceloadprofiles.intervalFilter.intervalStart', 'MDC', 'Interval from'),
            singleDate: true
        },
        {
            type: 'combobox',
            dataIndex: 'duration',
            emptyText: Uni.I18n.translate('deviceloadprofiles.intervalFilter.duration', 'MDC', 'Select a duration'),
            displayField: 'localizedValue',
            valueField: 'id',
            store: 'Mdc.store.LoadProfileDataDurations'
        },
        {
            type: 'combobox',
            dataIndex: 'validationStatus',
            emptyText: Uni.I18n.translate('deviceloadprofiles.intervalFilter.validationStatus', 'MDC', 'Select a validation status'),
            multiSelect: true,
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