Ext.define('Uni.view.search.field.internal.NumberField', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-search-internal-numberfield',
    width: '455',
    layout: 'fit',
    itemsDefaultConfig: {},

    setValue: function(value) {
        this.down('#filter-input').setValue(value);
    },

    getValue: function() {
        return this.down('#filter-input').getValue();
    },

    reset: function() {
        this.down('#filter-input').reset();
        this.fireEvent('reset', this);
    },

    onChange: function() {
        this.fireEvent('change', this, this.getValue());
    },

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change",
            "reset"
        );

        me.items = [
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'filter-input',
                width: 180,
                margin: '0 5 0 0',
                listeners: {
                    change:{
                        fn: me.onChange,
                        scope: me
                    }
                }
            }, me.itemsDefaultConfig)
        ];

        me.callParent(arguments);
    }
});