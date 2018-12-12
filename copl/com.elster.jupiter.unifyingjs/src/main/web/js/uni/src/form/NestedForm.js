/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.NestedForm
 * TODO: Move functionality to Basic
 */
Ext.define('Uni.form.NestedForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.nested-form',

    initComponent: function () {
        this.callParent();
        this.getForm().monitor.selector = "[isFormField]:not([excludeForm]){ownerCt.getId() === '" + this.getId() + "'}";
    },

    getValues: function () {
        var values = this.callParent();
        this.items.each(function (item) {
            if (_.isFunction(item.getValues)) values[item.name] = item.getValues();
        });
        return values;
    },

    setValues: function (data) {
        this.form.setValues(data);
        this.items.each(function (item) {
            if (!_.isEmpty(item.name) && _.has(data, item.name)) {
                if (_.isFunction(item.setValues) && _.isObject(data[item.name])) {
                    item.setValues(data[item.name]);
                }
            }
        });
    },

    loadRecord: function (record) {
        this.form._record = record;
        var data = this.form.hydrator ? this.form.hydrator.extract(record) : record.getData();
        return this.setValues(data);
    },

    updateRecord: function (record) {
        record = record || this.getRecord();
        var data = this.getValues();
        record.beginEdit();
        this.form.hydrator ? this.form.hydrator.hydrate(data, record) : record.set(data);
        record.endEdit();
        return this;
    }
});