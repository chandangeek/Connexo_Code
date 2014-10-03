Ext.define('Mdc.view.setup.devicesearch.SearchItems', {
    extend: 'Uni.view.container.ContentContainer',

    requires: [
        'Mdc.view.setup.devicesearch.SearchResults',
        'Uni.view.navigation.SubMenu'
    ],

    // TODO Filter.
    side: [

    ],

    content: [
        {
            xtype: 'mdc-search-results'
        }
    ]
});