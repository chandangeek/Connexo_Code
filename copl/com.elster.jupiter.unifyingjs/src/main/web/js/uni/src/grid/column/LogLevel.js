/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Uni.grid.column.LogLevel', {
    extend: 'Ext.grid.column.Column',
    xtype: 'log-level-column',
    header: Uni.I18n.translate('general.logLevel', 'UNI', 'Log level'),
    minWidth: 120,
    align: 'left',

    requires: [
        'Uni.form.field.LogLevelDisplay'
    ],

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        var me = undefined;
        if (this.$className === 'Uni.grid.column.LogLevel') {
            me = this;
        } else {
            me = Ext.Array.findBy(this.columns, function (item) {
                return item.$className === 'Uni.grid.column.LogLevel';
            });
        }
        return new Uni.form.field.LogLevelDisplay().renderer.apply(me, arguments);
    }
});