Ext.define('Mdc.view.setup.devicesearch.SearchItems', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'mdc-search-items',
    itemId: 'mdc-search-items',

    requires: [
        'Mdc.view.setup.devicesearch.SearchResults',
        'Mdc.view.setup.devicesearch.DevicesSideFilter',
        'Uni.component.filter.view.FilterTopPanel',
        'Uni.view.navigation.SubMenu'
    ],

    side: [
        {
            xtype: 'mdc-search-results-side-filter'
        }
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('setup.devicesearch.searchResults.title', 'MDC', 'Search')
        },
        {
            xtype: 'filter-top-panel'
        },
        {
            xtype: 'mdc-search-results'
        }
    ]
});