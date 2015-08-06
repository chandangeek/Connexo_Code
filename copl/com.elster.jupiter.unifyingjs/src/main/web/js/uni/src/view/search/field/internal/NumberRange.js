Ext.define('Uni.view.search.field.internal.NumberRange', {
    extend: 'Ext.container.Container',
    alias: 'widget.uni-view-search-field-number-range',
    xtype: 'uni-view-search-field-number-range',
    requires: [
        'Uni.view.search.field.internal.NumberLine'
    ],
    layout: 'vbox',
    defaults: {
        margin: '0 0 5 0'
    },

    onChange: function () {
        this.fireEvent('change', this, this.getValue());
    },

    getValue: function() {
        var value = [];
        this.items.each(function(item){
            value.push(item.getValue());
        });
        return value;
    },

    reset: function() {
        this.items.each(function(item){
            item.reset();
        });
    },

    initComponent: function () {
        var me = this,
            listeners = {
                change: {
                    fn: me.onChange,
                    scope: me
                }
            };

        me.addEvents(
            "change"
        );

        me.items = [
            {
                xtype: 'uni-view-search-field-number-line',
                itemId: 'more-value',
                default: true,
                operator: '>',
                listeners: listeners
            },
            {
                xtype: 'uni-view-search-field-number-line',
                itemId: 'smaller-value',
                default: true,
                operator: '<',
                listeners: listeners
            }
        ];

        me.callParent(arguments);
    }
});