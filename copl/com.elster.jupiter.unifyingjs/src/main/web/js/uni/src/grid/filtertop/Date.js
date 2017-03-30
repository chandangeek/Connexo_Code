/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.filtertop.Date
 */
Ext.define('Uni.grid.filtertop.Date', {
    extend: 'Ext.form.field.Date',
    xtype: 'uni-grid-filtertop-date',

    mixins: [
        'Uni.grid.filtertop.Base'
    ],

    editable: false,
    value: undefined,
    emptyText: Uni.I18n.translate('grid.filter.date.label', 'UNI', 'Date'),
    format: Uni.util.Preferences.lookup(Uni.DateTime.dateLongKey, Uni.DateTime.dateLongDefault),

    setFilterValue: function (date) {
        var me = this;

        if (Object.prototype.toString.call(date) !== '[object Date]') {
            date = new Date(parseInt(date));
        }

        me.setValue(date);
    },

    getParamValue: function () {
        var me = this,
            date = me.getValue();

        if (Ext.isDefined(date) && !Ext.isEmpty(date)) {
            date.setHours(0);
            date.setMinutes(0);
            date.setSeconds(0);
            date.setMilliseconds(0);

            return date.getTime();
        }

        return undefined;
    },

    resetValue: function () {
        this.reset();
    }
});