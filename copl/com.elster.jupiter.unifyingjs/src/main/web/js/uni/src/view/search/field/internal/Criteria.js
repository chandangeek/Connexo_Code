Ext.define('Uni.view.search.field.internal.Criteria', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-search-internal-criteria',
    requires: [
        'Ext.util.Filter'
    ],
    menuConfig: null,
    value: null,
    items: [],

    getValue: function() {
        return this.value
    },

    populateValue: function(value) {
        if (value) {
            this.items.each(function(item, index) {
                item.setValue(value[index]);
            });
        } else {
            this.reset();
        }
    },

    setValue: function(value) {
        if (value && !Ext.isArray(value)) {
            value = [value];
        }

        this.value = value;
        this.fireEvent('change', this, value);
    },

    getFilter: function() {
        var me = this,
            value = me.getValue();

        return new Ext.util.Filter({
            property: me.dataIndex,
            value: value ? value.map(function(v){return v.getData()}) : null,
            id: me.dataIndex
        });
    },

    reset: function() {
        this.value = null;
        this.fireEvent('reset');
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