Ext.define('Mdc.view.setup.devicesearch.SearchResults', {
    extend: 'Ext.panel.Panel',
    xtype: 'mdc-search-results',

    requires: [
        'Mdc.view.setup.devicesearch.DevicesGrid'
    ],

    title: Uni.I18n.translate('setup.devicesearch.searchResults.title', 'MDC', 'Search'),
    ui: 'large',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    items: [
        {
            xtype: 'mdc-search-results-grid'
        }
    ]
});