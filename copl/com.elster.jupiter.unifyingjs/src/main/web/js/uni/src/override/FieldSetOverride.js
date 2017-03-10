/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.override.FieldSetOverride
 */
Ext.define('Uni.override.FieldSetOverride', {
    override: 'Ext.form.FieldSet',

    initComponent: function () {
        this.callParent();
        this.form = new Ext.form.Basic(this);
        this.form.monitor.selector = "[isFormField]:not([excludeForm]){ownerCt.getId() === '" + this.getId() + "'}";
    },

    getValues: function () {
        var values = this.form.getValues();
        this.items.each(function (item) {
            if (_.isFunction(item.getValues)) {
                _.isEmpty(item.name) ? Ext.merge(values, item.getValues()) : values[item.name] = item.getValues();
            }
        });
        return values;
    },

    setValues: function (data) {
        this.form.setValues(data);
        this.items.each(function (item) {
            if (_.isFunction(item.setValues)) {
                _.isEmpty(item.name) ? item.setValues(data) : item.setValues(data[item.name]);
            }
        });
    }
});