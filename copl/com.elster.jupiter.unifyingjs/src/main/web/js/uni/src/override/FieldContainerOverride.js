/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.override.FieldContainerOverride
 */
Ext.define('Uni.override.FieldContainerOverride', {
    override: 'Ext.form.FieldContainer',

    /**
     * Changes the default value ':'.
     */
    labelSeparator: '',

    /**
     * Changes the default value 'qtip'.
     */
    msgTarget: 'side',

    /**
     * Changes the default label alignment.
     */
    labelAlign: 'right',

    initComponent: function () {
        this.callParent();
        this.form = new Ext.form.Basic(this);
    },

    getValues: function () {
        return this.form.getValues();
    },

    setValues: function (data) {
        this.form.setValues(data);
    }
});