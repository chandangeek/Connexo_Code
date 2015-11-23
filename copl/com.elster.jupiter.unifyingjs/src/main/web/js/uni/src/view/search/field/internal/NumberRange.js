Ext.define('Uni.view.search.field.internal.NumberRange', {
    extend: 'Ext.form.FieldSet',
    xtype: 'uni-search-internal-numberrange',
    requires: [
        'Uni.view.search.field.internal.NumberField'
    ],
    width: '455',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        margin: '0 0 5 0'
    },
    margin: 0,
    padding: 0,
    border: false,
    itemsDefaultConfig: {},

    setValue: function(value) {
        this.items.each(function(item, index) {
            item.setValue(value[index]);
        });
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
                xtype: 'uni-search-internal-numberfield',
                itemId: 'from',
                listeners: listeners,
                itemsDefaultConfig: me.itemsDefaultConfig
            },
            {
                xtype: 'uni-search-internal-numberfield',
                itemId: 'to',
                listeners: listeners,
                itemsDefaultConfig: me.itemsDefaultConfig
            }
        ];

        me.callParent(arguments);
    }
});