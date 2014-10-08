Ext.define('Mdc.view.setup.devicesearch.SearchResults', {
    extend: 'Ext.panel.Panel',
    xtype: 'mdc-search-results',

    requires: [
        'Mdc.view.setup.devicesearch.DevicesGrid'
    ],

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