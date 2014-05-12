Ext.define('Uni.view.form.CheckboxGroup', {
    extend: 'Ext.form.CheckboxGroup',
    alias: 'widget.checkboxstore',

    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    checkbox: {
        boxLabel: 'name',
        inputValue: 'id'
    },

    initComponent: function () {
        var me = this;
        me.bindStore(me.store || 'ext-empty-store', true);
        this.callParent(arguments);
    },

    refresh: function() {
        var me = this;
        me.removeAll();
        me.store.each(function (record) {
            me.add({
                xtype: 'checkboxfield',
                boxLabel: record.get(me.checkbox.boxLabel),
                inputValue: record.get(me.checkbox.inputValue),
                name: me.name
            });
        });
    },

    setValue: function(data) {
        var values = {};
        values[this.name] = data;
        this.callParent([values]);
    },

    getStoreListeners: function () {
        return {
            load: this.refresh
        };
    }
});