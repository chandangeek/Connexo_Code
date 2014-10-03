Ext.define('Mdc.view.setup.devicesearch.SearchItems', {
    extend: 'Uni.view.container.ContentContainer',

    requires: [
        'Mdc.view.setup.devicesearch.SearchResults',
        'Uni.component.filter.view.FilterTopPanel',
        'Uni.view.navigation.SubMenu'
    ],

    // TODO Filter.
    side: [

    ],

    content: [
        {
            xtype: 'filter-top-panel'
        },
        {
            xtype: 'mdc-search-results'
        }
    ]
});