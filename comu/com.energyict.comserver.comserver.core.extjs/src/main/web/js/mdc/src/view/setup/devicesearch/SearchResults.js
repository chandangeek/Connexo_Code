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
            xtype: 'preview-container',
            itemId: 'dynamic-grid-container',
            grid: {
                xtype: 'mdc-search-results-grid',
                itemId: 'dynamic-grid',
                visible: false
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('devicesearch.empty.title', 'MDC', 'No devices found'),
                reasons: [
                    Uni.I18n.translate('devicesearch.empty.list.item1', 'MDC', 'There are no devices in the system.'),
                    Uni.I18n.translate('devicesearch.empty.list.item2', 'MDC', 'No devices match your filter.')
                ]
            }
        },
        {
            xtype: 'preview-container',
            itemId: 'static-grid-container',
            selectByDefault: false,
            grid: {
                xtype: 'bulk-selection-mdc-search-results-grid',
                itemId: 'static-grid',
                visible: false,
                allChosenByDefault: true
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('devicesearch.empty.title', 'MDC', 'No devices found'),
                reasons: [
                    Uni.I18n.translate('devicesearch.empty.list.item1', 'MDC', 'There are no devices in the system.'),
                    Uni.I18n.translate('devicesearch.empty.list.item2', 'MDC', 'No devices match your filter.')
                ]
            }
        }/*,
        {
            xtype: 'mdc-search-results-grid',
            visible: false
        }/*,
        {
            xtype: 'bulk-selection-mdc-search-results-grid',
            visible: false
        }*/
    ]
});