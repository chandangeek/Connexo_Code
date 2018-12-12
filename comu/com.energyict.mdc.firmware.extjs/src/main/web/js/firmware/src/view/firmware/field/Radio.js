/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.view.firmware.field.Radio', {
    extend: 'Ext.form.CheckboxGroup',
    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    getStoreListeners: function () {
        return {
            refresh: this.refresh
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

        // re-populate data values
        if (me.value) {
            me.setValue(me.value);
        }
        Ext.resumeLayouts();
        me.doLayout();
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
        var me = this,
            values = {};

        values[me.name] = Ext.isArray(data)
            ? data.map(function (item) { return item[me.valueField]; })
            : data[me.valueField];

        me.value = data;
        me.callParent([values]);
    }
});
