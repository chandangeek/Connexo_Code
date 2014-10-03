Ext.define('Mdc.view.setup.devicesearch.DevicesSideFilter', {
    extend: 'Ext.panel.Panel',
    xtype: 'mdc-search-results-side-filter',
    requires: [
        'Uni.component.filter.view.Filter'
    ],
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('searchItems.sideFilter.title', 'MDC', 'Search for devices'),
    ui: 'medium',
    items: [
        {
            xtype: 'nested-form',
            itemId: 'filter-form',
            hydrator: 'Dsh.util.FilterHydrator',
            ui: 'filter',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                xtype: 'side-filter-combo',
                labelAlign: 'top'
            },
            items: [

            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: [
                        {
                            text: Uni.I18n.translate('searchItems.sideFilter.apply', 'MDC', 'Apply'),
                            ui: 'action',
                            action: 'applyfilter'
                        },
                        {
                            text: Uni.I18n.translate('searchItems.sideFilter.clearAll', 'MDC', 'Clear all'),
                            action: 'clearfilter'
                        }
                    ]
                }
            ]
        }
    ]
});

