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
    useRawValue: true,

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
        Ext.Array.forEach(this.store.getRange(), function (item) {
            me.add({
                xtype: 'checkboxfield',
                boxLabel: item.data[me.checkbox.boxLabel],
                inputValue: item.data[me.checkbox.inputValue],
                name: me.name
            });
        });
    },

    getValue: function() {
        if (this.useRawValue) {
            return this.getRawValue();
        }

        return this.callParent();
    },

    /**
     * Returns array of selected objects, from binded store
     * @returns {Ext.data.Model[]}
     */
    getRawValue: function() {
        var value = this.superclass.getValue.call(this);
        var store = this.getStore();

        var data = value[this.name];
        if (data) {
            if (!_.isArray(data)) {
                data = [data];
            }
            value[this.name] = _.map(data, function(id) {
                return store.getById(id);
            });
        }

        return value;
    },

    getStoreListeners: function () {
        return {
            load: this.onLoad
        };
    }
});