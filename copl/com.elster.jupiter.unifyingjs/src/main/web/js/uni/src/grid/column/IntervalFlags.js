/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.column.IntervalFlags
 */
Ext.define('Uni.grid.column.IntervalFlags', {
    extend: 'Ext.grid.column.Column',
    xtype: 'interval-flags-column',
    header: Uni.I18n.translate('intervalFlags.label', 'UNI', 'Interval flags'),
    dataIndex: 'intervalFlags',
    align: 'left',
    emptyText: '',
    requires: [
        'Uni.form.field.IntervalFlagsDisplay'
    ],

    renderer: function (value, metaData, record, rowIndex, colIndex) {
        if(this.$className  !== 'Uni.grid.column.IntervalFlags') {
            var me = Ext.Array.findBy(this.columns, function (item) {
                return item.$className === 'Uni.grid.column.IntervalFlags';
            });
        } else {
            me = this;
        }

        return new Uni.form.field.IntervalFlagsDisplay().renderer.apply(me, arguments);
    }
});