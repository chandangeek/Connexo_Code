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
        xtype: 'form',
        itemId: 'deviceLogbookDataSideFilterForm',
        title: Uni.I18n.translate('general.filter', 'MDC', 'Filter'),
        ui: 'filter',
        defaults: {
            xtype: 'combobox',
            labelAlign: 'top',
            forceSelection: true,
            anchor: '100%',
            queryMode: 'local',
            displayField: 'localizedValue'
        },
        items: [
            {
                xtype: 'fieldcontainer',
                itemId: 'event-date-container',
                fieldLabel: Uni.I18n.translate('devicelogbooks.eventDate', 'MDC', 'Event date'),
                layout: 'form',
                margin: '0 0 25 0',
                defaults: {
                    xtype: 'datefield',
                    labelAlign: 'top',
                    invalidText: Uni.I18n.translate('devicelogbooks.sideFilter.datefield.invalidText', 'MDC', 'Invalid date format. Please enter the date in the format \'dd/mm/yyyy\''),
                    emptyText: '        /        /',
                    maxValue: new Date(),
                    anchor: '100%'
                },
                items: [
                    {

                        itemId: 'deviceLogbookDataFilterIntervalStart',
                        name: 'intervalStart',
                        fieldLabel: Uni.I18n.translate('general.from', 'MDC', 'From')
                    },
                    {
                        itemId: 'deviceLogbookDataFilterIntervalEnd',
                        name: 'intervalEnd',
                        fieldLabel: Uni.I18n.translate('general.to', 'MDC', 'To')
                    }
                ]
            },
            {
                itemId: 'deviceLogbookDataFilterDomain',
                name: 'domain',
                fieldLabel: Uni.I18n.translate('devicelogbooks.domain', 'MDC', 'Domain'),
                emptyText: Uni.I18n.translate('devicelogbooks.selectDomain', 'MDC', 'Select a domain'),
                store: 'Mdc.store.Domains',
                valueField: 'domain'
            },
            {
                itemId: 'deviceLogbookDataFilterSubDomain',
                name: 'subDomain',
                fieldLabel: Uni.I18n.translate('devicelogbooks.subDomain', 'MDC', 'Subdomain'),
                emptyText: Uni.I18n.translate('devicelogbooks.selectSubdomain', 'MDC', 'Select a subdomain'),
                store: 'Mdc.store.Subdomains',
                valueField: 'subDomain'
            },
            {
                itemId: 'deviceLogbookDataFilterEventOrAction',
                name: 'eventOrAction',
                fieldLabel: Uni.I18n.translate('devicelogbooks.eventOrAction', 'MDC', 'Event or action'),
                emptyText: Uni.I18n.translate('devicelogbooks.selectEventOrAction', 'MDC', 'Select an event or action'),
                store: 'Mdc.store.EventsOrActions',
                valueField: 'eventOrAction'
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