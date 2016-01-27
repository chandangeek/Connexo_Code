Ext.define('Uni.view.search.field.internal.Criteria', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-search-internal-criteria',
    requires: [
        'Ext.util.Filter'
    ],
    minWidth: 300,
    items: [],

    reset: function () {
        this.items.filterBy(function (item) {
            return Ext.isFunction(item.reset);
        }).each(function (item) {
            item.reset();
        });

        this.fireEvent('reset', this);
    },

    getValue: function () {
        var value = [];

        this.items.filterBy(function (item) {
            return Ext.isFunction(item.getValue);
        }).each(function (item) {
            if (!Ext.isEmpty(item.getValue())) {
                value.push(item.getValue());
            }
        });

        return Ext.isEmpty(value) ? null : value;
    },

    setValue: function(value) {
        if (value) {
            this.items.filterBy(function (item) {
                return Ext.isFunction(item.setValue);
            }).each(function (item, index) {
                item.setValue(value[index]);
            });
        } else {
            this.reset();
        }
    },

    onValueChange: function() {
        this.fireEvent('change', this, this.getValue());
    },

    initComponent: function () {
        var me = this;

        me.addEvents(
            "change",
            "reset"
        );

        me.callParent(arguments);
    }
});