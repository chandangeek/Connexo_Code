Ext.define('Mdc.view.setup.devicevalidationresults.SideFilter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.form.field.DateTime',
        'Mdc.store.ValidationResultsDurations'
    ],
    alias: 'widget.deviceValidationResultsSideFilter',
    itemId: 'deviceValidationResultsSideFilter',
    ui: 'medium',
    width: 288,
    cls: 'filter-form',
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    items: {
        xtype: 'form',
        itemId: 'deviceValidationResultsFilterForm',
        ui: 'filter',
        items: [
            {
                xtype: 'fieldcontainer',
                itemId: 'dateContainer',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                labelAlign: 'top',
                defaults: {
                    width: '100%'
                },
                items: [
                    {
                        xtype: 'date-time',
                        itemId: 'endOfInterval',
                        name: 'intervalStart',
                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From'),
                        labelAlign: 'top',
                        labelStyle: 'font-weight: normal',
                        dateConfig: {
                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                        },
                        hoursConfig: {
                            margin: '0 0 0 -10'
                        }
                    }
                ]
            },
            {
                xtype: 'combobox',
                itemId: 'sideFilterDuration',
                name: 'duration',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.duration', 'MDC', 'Duration'),
                labelAlign: 'top',
                store: 'Mdc.store.ValidationResultsDurations',
                displayField: 'localizeValue',
                valueField: 'id',
                queryMode: 'local',
                anchor: '100%'
            }
        ],
        dockedItems: [
            {
                xtype: 'toolbar',
                dock: 'bottom',
                items: [
                    {
                        itemId: 'deviceValidationResultsFilterApplyBtn',
                        ui: 'action',
                        text: Uni.I18n.translate('general.apply', 'MDC', 'Apply'),
                        action: 'applyfilter'
                    },
                    {
                        itemId: 'deviceValidationResultsFilterResetBtn',
                        text: Uni.I18n.translate('general.reset', 'MDC', 'Reset'),
                        action: 'resetfilter'
                    }
                ]
            }
        ]
    }
});