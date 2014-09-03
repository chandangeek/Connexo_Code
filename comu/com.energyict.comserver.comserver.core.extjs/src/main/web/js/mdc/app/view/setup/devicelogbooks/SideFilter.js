Ext.define('Mdc.view.setup.devicelogbooks.SideFilter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.component.filter.view.Filter',
        'Ext.form.field.Date'
    ],
    alias: 'widget.deviceLogbookDataSideFilter',
    itemId: 'deviceLogbookDataSideFilter',
    ui: 'medium',
    width: 250,
    items: {
        xtype: 'filter-form',
        itemId: 'deviceLogbookDataSideFilterForm',
        title: Uni.I18n.translate('general.filter', 'MDC', 'Filter'),
        ui: 'filter',
        defaults: {
          anchor: '100%'
        },
        items: [
            {
                xtype: 'component',
                html: '<h4>' + Uni.I18n.translate('devicelogbooks.eventDate', 'MDC', 'Event date') + '</h4>'
            },
            {
                xtype: 'datefield',
                itemId: 'deviceLogbookDataFilterIntervalStart',
                name: 'intervalStart',
                fieldLabel: Uni.I18n.translate('general.between', 'MDC', 'Between'),
                labelAlign: 'top',
                invalidText: Uni.I18n.translate('devicelogbooks.sideFilter.datefield.invalidText', 'MDC', 'Invalid date format. Please enter the date in the format \'dd/mm/yyyy\''),
                maxValue: new Date()
            },
            {
                xtype: 'datefield',
                itemId: 'deviceLogbookDataFilterIntervalEnd',
                name: 'intervalEnd',
                fieldLabel: Uni.I18n.translate('general.and', 'MDC', 'and').toLowerCase(),
                labelAlign: 'top',
                invalidText: Uni.I18n.translate('devicelogbooks.sideFilter.datefield.invalidText', 'MDC', 'Invalid date format. Please enter the date in the format \'dd/mm/yyyy\''),
                maxValue: new Date()
            }
        ],
        dockedItems: [
            {
                xtype: 'toolbar',
                dock: 'bottom',
                items: [
                    {
                        itemId: 'deviceLogbookDataSideFilterApplyBtn',
                        ui: 'action',
                        text: Uni.I18n.translate('general.apply', 'MDC', 'Apply'),
                        action: 'filter'
                    },
                    {
                        itemId: 'deviceLogbookDataSideFilterResetBtn',
                        text: Uni.I18n.translate('general.clearAll', 'MDC', 'Clear all'),
                        action: 'reset'
                    }
                ]
            }
        ]
    }
});