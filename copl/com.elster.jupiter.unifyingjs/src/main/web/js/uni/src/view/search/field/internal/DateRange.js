/**
 * @deprecated
 */
Ext.define('Uni.view.search.field.internal.DateRange', {
    extend: 'Ext.container.Container',
    xtype: 'uni-search-internal-daterange',
    requires: [
        'Uni.view.search.field.internal.DateLine'
    ],
    layout: 'vbox',
    defaults: {
        margin: '0 0 5 0'
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
        var me = this;

        me.addEvents(
            "change"
        );

        me.items = [
            {
                xtype: 'uni-search-internal-dateline',
                itemId: 'from',
                isDefault: true,
                operator: '>',
                listeners: {
                    change: function(field) {
                        me.down('#to datefield').setMinValue(field.getValue());
                        me.fireEvent('change', me, me.getValue());
                    },
                    reset: function() {
                        me.down('#to datefield').setMinValue(null);
                    }
                }
            },
            {
                xtype: 'uni-search-internal-dateline',
                itemId: 'to',
                isDefault: true,
                operator: '<',
                listeners: {
                    change: function(field) {
                        me.down('#from datefield').setMaxValue(field.getValue());
                        me.fireEvent('change', me, me.getValue());
                    },
                    reset: function() {
                        me.down('#from datefield').setMaxValue(null);
                    }
                }
            }
        ];

        me.callParent(arguments);
    }
});