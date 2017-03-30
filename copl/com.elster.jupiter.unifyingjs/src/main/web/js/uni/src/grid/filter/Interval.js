/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.filter.Interval
 */
Ext.define('Uni.grid.filter.Interval', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'uni-grid-filter-interval',

    mixins: [
        'Uni.grid.filter.Base'
    ],

    fieldLabel: Uni.I18n.translate('grid.filter.interval.label', 'UNI', 'Interval'),

    requires: [
        'Uni.grid.filter.Date'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    fromParam: 'from',
    toParam: 'to',

    items: [
        {
            xtype: 'container',
            margin: '0 0 8 0',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    margin: '0 6 0 0',
                    html: 'From',
                    width: 32
                },
                {
                    xtype: 'uni-grid-filter-date',
                    fieldLabel: undefined,
                    itemId: 'from',
                    flex: 1
                }
            ]
        },
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    margin: '0 6 0 0',
                    html: 'To',
                    width: 32
                },
                {
                    xtype: 'uni-grid-filter-date',
                    fieldLabel: undefined,
                    itemId: 'to',
                    flex: 1
                }
            ]
        }
    ],

    setFilterValue: function (cfg) {
        var me = this,
            fromDateField = me.getFromDateField(),
            toDateField = me.getToDateField();

        if (Ext.isDefined(cfg[me.fromParam])) {
            fromDateField.setFilterValue(cfg[me.fromParam]);
        }

        if (Ext.isDefined(cfg[me.toParam])) {
            toDateField.setFilterValue(cfg[me.toParam]);
        }
    },

    getParamValue: function () {
        var me = this,
            fromDate = me.getFromDateField().getParamValue(),
            toDate = me.getToDateField().getParamValue(),
            result = {};

        if (Ext.isDefined(fromDate) && Ext.isDefined(toDate)) {
            result[me.fromParam] = fromDate;
            result[me.toParam] = toDate;

            return result;
        }

        return undefined;
    },

    resetValue: function () {
        var me = this;

        me.getFromDateField().resetValue();
        me.getToDateField().resetValue();
    },

    getFromDateField: function () {
        return this.down('uni-grid-filter-date#from');
    },

    getToDateField: function () {
        return this.down('uni-grid-filter-date#to');
    }
});