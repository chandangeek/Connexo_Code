/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.column.Check
 */
Ext.define('Uni.grid.column.Check', {
    extend: 'Ext.grid.column.Check',
    alias: 'widget.uni-checkcolumn',
    tdCls: 'x-grid-cell-row-checker',
    maxWidth: 24,

    /**
     * A function which determines whether the checkbox item for any row is disabled and returns `true` or `false`.
     * @param {Ext.data.Model} record The record for the current row
     */
    isDisabled: function (record) {
        return false;
    },

    /**
     * @private
     */
    processEvent: function (type, view, cell, recordIndex, cellIndex, e, record, row) {
        var me = this,
            result;

        if (!me.disabled) {
            me.disabled = me.isDisabled(record);
        }

        result = me.callParent(arguments);
        me.disabled = false;

        return result;
    },

    /**
     * @private
     */
    renderer: function (value, meta, record, rowIdx, colIdx, store, view) {
        var me = this;

        meta.tdCls += ' row-checker'; // for automation tests

        if (!me.disabled && me.isDisabled(record)) {
            meta.tdCls += ' ' + me.disabledCls;
        }

        return me.callParent(arguments);
    }
});