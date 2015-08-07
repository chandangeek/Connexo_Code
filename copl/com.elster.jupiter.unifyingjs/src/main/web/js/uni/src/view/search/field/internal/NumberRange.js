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
            if (!Ext.isEmpty(item.getValue())) {value.push(item.getValue());}
        });
        return Ext.isEmpty(value) ? null : value;
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
                itemId: 'from',
                default: true,
                operator: '>',
                listeners: listeners
            },
            {
                xtype: 'uni-view-search-field-number-line',
                itemId: 'to',
                default: true,
                operator: '<',
                listeners: listeners
            }
        ];

        me.callParent(arguments);
    }
});