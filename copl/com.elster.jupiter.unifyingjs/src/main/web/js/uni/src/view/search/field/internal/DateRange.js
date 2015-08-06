Ext.define('Uni.view.search.field.internal.DateRange', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-view-search-field-date-range',
    xtype: 'uni-view-search-field-date-range',
    requires: [
        'Uni.view.search.field.internal.DateLine'
    ],
    layout: 'vbox',
    items: [
        {
            xtype: 'uni-view-search-field-date-line',
            itemId: 'more-value',
            default: true,
            operator: '>',
            margin: '0 0 3 0'
        },
        {
            xtype: 'uni-view-search-field-date-line',
            itemId: 'smaller-value',
            default: true,
            operator: '<',
            margin: '0 0 3 0'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});