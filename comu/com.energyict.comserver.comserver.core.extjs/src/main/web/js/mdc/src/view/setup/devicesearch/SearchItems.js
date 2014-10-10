Ext.define('Mdc.view.setup.devicesearch.SearchItems', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'mdc-search-items',
    //itemId: 'mdc-search-items',

    requires: [
        'Mdc.view.setup.devicesearch.SearchResults',
        'Mdc.view.setup.devicesearch.DevicesSideFilter',
        'Uni.component.filter.view.FilterTopPanel',
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.devicesearch.SortMenu'
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
            xtype: 'filter-toolbar',
            title: Uni.I18n.translate('searchItems.filter.sort', 'MDC', 'Sort'),
            itemId: 'sortButtonsContainer',
            emptyText: 'None',
            tools: [
                {
                    xtype: 'button',
                    action: 'addSort',
                    text: 'Add sort',
                    menu: {
                        xtype: 'devices-sort-menu'
                    }
                }
            ]
        },
        {
            xtype: 'mdc-search-results'
        }
    ]
});