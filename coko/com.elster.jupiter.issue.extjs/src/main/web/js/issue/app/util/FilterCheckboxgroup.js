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
            checkboxlist = me.down('[name=checkboxlist]');

        if (!checkboxlist) {
            checkboxlist = me.add({
                xtype: 'container',
                name: 'checkboxlist'
            });
        }

        checkboxlist.removeAll();

        Ext.Array.forEach(this.store.getRange(), function (item) {
            checkboxlist.add({
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