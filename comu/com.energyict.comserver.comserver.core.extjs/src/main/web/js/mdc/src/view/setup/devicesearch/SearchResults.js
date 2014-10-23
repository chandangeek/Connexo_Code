Ext.define('Mdc.view.setup.devicesearch.SearchResults', {
    extend: 'Ext.panel.Panel',
    xtype: 'mdc-search-results',

    requires: [
        'Mdc.view.setup.devicesearch.DevicesGrid',
        'Mdc.view.setup.devicesearch.BulkSelectionDevicesGrid'
    ],

    ui: 'large',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    items: [
        {
            xtype: 'mdc-search-results-grid',
            visible: false
        },
        {
            xtype: 'bulk-selection-mdc-search-results-grid',
            visible: false
        }
    ]
});