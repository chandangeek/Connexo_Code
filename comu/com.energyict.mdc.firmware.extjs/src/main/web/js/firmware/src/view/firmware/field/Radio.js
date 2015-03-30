Ext.define('Fwc.view.firmware.field.Radio', {
    extend: 'Ext.form.RadioGroup',
    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    getStoreListeners: function () {
        return {
            refresh: this.refresh
            //beforeload: function () {
            //    this.setLoading();
            //},
            //load: function () {
            //    this.setLoading(false);
            //}
        };
    },

    name: null,

    /**
     * This field will be used as boxLabel on checkbox
     */
    displayField: 'name',

    /**
     * This field will define the value of checkbox
     */
    valueField: 'id',

    initComponent: function () {
        var me = this;
        me.bindStore(me.store || 'ext-empty-store', true);
        this.callParent(arguments);
        // todo: move?
        me.store.load();
    },

    /**
     * @private
     * Refreshes the content of the checkbox group
     */
    refresh: function () {
        var me = this;

        Ext.suspendLayouts();

        me.removeAll();
        me.store.each(function (record) {
            me.add({
                boxLabel: record.get(me.displayField),
                inputValue: record.get(me.valueField),
                getModelData: me.getFieldModelData,
                store: me.store,
                name: me.name
            });
        });

        Ext.resumeLayouts();
        me.updateLayout();

        // re-populate data values
        if (me.values) {
            me.setValue(me.values);
        }
    },

    getFieldModelData: function () {
        var value = this.getSubmitValue(),
            data = null;

        if (value) {
            data = {};
            data[this.getName()] = this.store.getById(value);
        }

        return data;
    },

    setValue: function (data) {
        var values = {};
        values[this.name] = data[this.valueField];
        this.values = data;
        this.callParent([values]);
    }
});
