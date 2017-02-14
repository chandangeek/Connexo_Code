/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.grid.column.Edited', {
    extend: 'Ext.grid.column.Column',
    xtype: 'edited-column',
    header: '&nbsp',
    width: 30,
    align: 'left',
    emptyText: '',
    requires: [
        'Uni.form.field.EditedDisplay'
    ],

    renderer: function (value, metaData, record, rowIndex, colIndex) {
        if(this.$className  !== 'Uni.grid.column.Edited') {
            var me = Ext.Array.findBy(this.columns, function (item) {
                return item.$className === 'Uni.grid.column.Edited';
            });
        } else {
            me = this;
        }

        return new Uni.form.field.EditedDisplay().renderer.apply(me, arguments);
    }
});