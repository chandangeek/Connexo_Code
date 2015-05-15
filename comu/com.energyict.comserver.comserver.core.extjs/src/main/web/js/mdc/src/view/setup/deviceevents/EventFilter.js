Ext.define('Mdc.view.setup.deviceevents.EventFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-view-setup-deviceevents-eventfilter',

    store: 'Mdc.store.LogbookOfDeviceData',

    filters: [
        {
            type: 'date',
            dataIndex: 'intervalStart',
            emptyText: Uni.I18n.translate('deviceevents.eventfilter.eventFrom', 'MDC', 'Event from')
        },
        {
            type: 'date',
            dataIndex: 'intervalEnd',
            emptyText: Uni.I18n.translate('deviceevents.eventfilter.eventTo', 'MDC', 'Event to')
        },
        {
            type: 'combobox',
            dataIndex: 'domain',
            emptyText: Uni.I18n.translate('deviceevents.eventfilter.domain', 'MDC', 'Select a domain'),
            displayField: 'localizedValue',
            valueField: 'domain',
            store: 'Mdc.store.Domains'
        },
        {
            type: 'combobox',
            dataIndex: 'subDomain',
            emptyText: Uni.I18n.translate('deviceevents.eventfilter.subdomain', 'MDC', 'Select a subdomain'),
            displayField: 'localizedValue',
            valueField: 'subDomain',
            store: 'Mdc.store.Subdomains'
        },
        {
            type: 'combobox',
            dataIndex: 'eventOrAction',
            emptyText: Uni.I18n.translate('deviceevents.eventfilter.eventOrAction', 'MDC', 'Select an event or action'),
            displayField: 'localizedValue',
            valueField: 'eventOrAction',
            store: 'Mdc.store.EventsOrActions'
        }
    ]
});