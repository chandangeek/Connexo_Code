/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.grid.column.EventType', {
    extend: 'Ext.grid.column.Column',
    xtype: 'event-type-column',
    header: Uni.I18n.translate('eventType.columnHeader', 'UNI', 'Event type'),
    align: 'left',
    emptyText: '',
    requires: [
        'Uni.form.field.EventTypeDisplay'
    ],

    renderer: function (value, metaData, record, rowIndex, colIndex) {
        if(this.$className  !== 'Uni.grid.column.EventTypes') {
            var me = Ext.Array.findBy(this.columns, function (item) {
                return item.$className === 'Uni.grid.column.EventTypes';
            });
        } else {
            me = this;
        }

        return new Uni.form.field.EventTypeDisplay().renderer.apply(me, arguments);
    }
});