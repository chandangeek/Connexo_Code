Ext.define('Uni.view.search.field.internal.NumberRange', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-view-search-field-number-range',
    xtype: 'uni-view-search-field-number-range',
    requires: [
        'Uni.view.search.field.internal.NumberLine'
    ],
    layout: 'vbox',
    defaults: {
        margin: '0 0 5 0'
    },

    items: [
        {
            xtype: 'uni-view-search-field-number-line',
            itemId: 'more-value',
            default: true,
            operator: '>'
        },
        {
            xtype: 'uni-view-search-field-number-line',
            itemId: 'smaller-value',
            default: true,
            operator: '<'
        }
    ]
});