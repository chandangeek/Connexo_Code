Ext.define('Mdc.view.setup.searchitems.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.search-side-filter',
    title: Uni.I18n.translate('searchItems.searchFor', 'MDC', 'Search for devices'),
    //  cls: 'logbook-filter-form',
    //width: 180,
    padding: '10 10 10 10',

    requires: [
        'Uni.component.filter.view.Filter'

    ],

    items: [
        {
        xtype: 'filter-form',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
                    {
                        xtype: 'textfield',
                        name: 'mrid',
                        itemId: 'mrid',
                        fieldLabel: Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID'),
                        labelAlign: 'top'
                    },
                    {
                        xtype: 'textfield',
                        name: 'sn',
                        itemId: 'sn',
                        fieldLabel: Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number'),
                        labelAlign: 'top'
                    }
                ]
            }
        ],

    buttons: [
        {
            text: Uni.I18n.translate('searchItems.clearAll', 'MDC', 'Search'),
            itemId: 'searchAllItems',
            action: 'applyfilter'
        },
        {
            text: Uni.I18n.translate('searchItems.clearAll', 'MDC', 'Clear all'),
            itemId: 'clearAllItems',
            action: 'clearfilter'
        }
    ]
});