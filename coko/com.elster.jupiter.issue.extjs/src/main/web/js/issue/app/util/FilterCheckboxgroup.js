Ext.define('Isu.util.FilterCheckboxgroup', {
    extend: 'Ext.form.CheckboxGroup',
    alias: 'widget.filter-checkboxgroup',
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

    beforeRender: function () {
        this.callParent(arguments);
        if (!this.store.isLoading()) {
            this.onLoad();
        }
    },

    onLoad: function () {
        var me = this;

        me.removeAll();
        Ext.Array.forEach(me.store.getRange(), function (item) {
            me.add({
                xtype: 'checkboxfield',
                boxLabel: item.data[me.checkbox.boxLabel],
                inputValue: item.data[me.checkbox.inputValue],
                name: me.name
            });
        });
        me.fireEvent('itemsadded', me, me.store);
    },

    setValue: function(data) {
        var values = {};
        values[this.name] = data;
        this.callParent([values]);
    },

    getStoreListeners: function () {
        return {
            load: this.onLoad
        };
    }
});