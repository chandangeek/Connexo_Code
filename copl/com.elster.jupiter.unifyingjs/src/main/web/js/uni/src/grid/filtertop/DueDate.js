/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.grid.filtertop.DueDate', {
    extend: 'Uni.grid.filtertop.ComboBox',
    xtype: 'uni-grid-filtertop-dueDate',
    multiSelect: false,
    applyParamValue: function (params) {
        var me = this,
            value = me.multiSelect ? [] : null;

        if (me.multiSelect) {
            Ext.Array.each(me.getValue(), function (id) {
                value.push(me.idToTimestamp(id));
            });
        } else {
            value = me.idToTimestamp(me.getValue());
        }

        params[me.dataIndex] = value;
    },

    idToTimestamp: function (id) {
        var currentTimeInMs = new Date().getTime(),
            tomorrowDate = Ext.Date.add(new Date(),  Ext.Date.DAY, 1),
            theDayAfterTomorrow = Ext.Date.add(new Date(),  Ext.Date.DAY, 2),
            startOfTomorrowInMs = Ext.Date.clearTime(tomorrowDate).getTime(),
            startOfTheDayAfterTomorrowInMs = Ext.Date.clearTime(theDayAfterTomorrow).getTime();

        switch (id) {
            case 'today':
                return currentTimeInMs + ':' + startOfTomorrowInMs;
            case 'tomorrow':
                return startOfTomorrowInMs + ':' + startOfTheDayAfterTomorrowInMs;
            case 'overdue':
                return '0:' + currentTimeInMs;
        }
    }
});
