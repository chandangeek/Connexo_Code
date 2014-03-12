Ext.define('Isu.util.FilterCheckboxgroup', {
    extend: 'Ext.container.Container',
    alias: 'widget.filter-checkboxgroup',
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    checkbox: {
        boxLabel: 'name',
        inputValue: 'id'
    },
    checkboxgroup: {
        columns: 1
    },

    initComponent: function () {
        var me = this;

        me.bindStore(me.store || 'ext-empty-store', true);

        this.callParent(arguments);

        if (me.title) {
            me.add({
                xtype: 'container',
                html: me.title
            });
        }
    },

    beforeRender: function () {
        this.callParent(arguments);
        if (this.store.isLoading()) {
            this.onLoad();
        }
    },

    onLoad: function () {
        var me = this,
            checkboxgroup = me.down('[layout=checkboxgroup]');

        if (!checkboxgroup) {
            checkboxgroup = me.add({
                xtype: 'container',
                layout: 'checkboxgroup',
                columns: me.checkboxgroup.columns,
                name: 'checkboxlist'
            });
        }

        checkboxgroup.removeAll();

        Ext.Array.forEach(this.store.getRange(), function (item) {
            checkboxgroup.add({
                xtype: 'checkboxfield',
                boxLabel: item.data[me.checkbox.boxLabel],
                inputValue: item.data[me.checkbox.inputValue]
            });
        });
    },

    getStoreListeners: function () {
        return {
            load: this.onLoad
        };
    },

    unbind: function (store) {
        this.bindStore(null);
    },

    bind: function (store) {
        this.bindStore(store);
    },

    onDestroy: function () {
        this.unbind();
        this.callParent();
    }
});