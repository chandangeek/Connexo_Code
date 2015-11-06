Ext.define('Uni.view.search.field.internal.NumberField', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-search-internal-numberfield',
    width: '455',
    layout: 'hbox',

    defaults: {
        margin: '0 10 0 0'
    },

    removable: false,

    getValue: function() {
        var value = this.down('#filter-input').getValue();

        return value ? Ext.create('Uni.model.search.Value', {
            operator: this.down('#filter-operator').getValue(),
            criteria: value
        }) : null
    },

    reset: function() {
        this.down('#filter-operator').reset();
        this.down('#filter-input').reset();
        this.fireEvent('reset', this);
    },

    onChange: function() {
        this.fireEvent('change', this, this.getValue());
    },

    onRemove: Ext.emptyFn,

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change",
            "reset"
        );

        me.items = [
            {
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
            }
        ];

        me.callParent(arguments);
    }
});