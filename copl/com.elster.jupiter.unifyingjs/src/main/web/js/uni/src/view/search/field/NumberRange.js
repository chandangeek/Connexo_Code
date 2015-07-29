Ext.define('Uni.view.search.field.NumberRange', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-view-search-field-number-range',
    xtype: 'uni-view-search-field-number-range',
    requires: [
        'Uni.view.search.field.NumberLine'
    ],
    layout: 'vbox',
    items:
        [
            {
                xtype: 'uni-view-search-field-number-line',
                default: true,
                operator: '>',
                margin: '0 0 3 0'
            },
            {
                xtype: 'uni-view-search-field-number-line',
                default: true,
                operator: '<',
                margin: '0 0 3 0'
            }
        ],

    initComponent: function () {
        this.callParent(arguments);
    }
});