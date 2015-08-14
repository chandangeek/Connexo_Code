/**
 * @deprecated
 */
Ext.define('Uni.view.search.field.internal.NumberRange', {
    extend: 'Ext.container.Container',
    xtype: 'uni-search-internal-numberrange',
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
                xtype: 'uni-search-internal-numberline',
                itemId: 'from',
                default: true,
                operator: '>',
                listeners: listeners
            },
            {
                xtype: 'uni-search-internal-numberline',
                itemId: 'to',
                default: true,
                operator: '<',
                listeners: listeners
            }
        ];

        me.callParent(arguments);
    }
});