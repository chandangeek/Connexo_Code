/**
 * @class Uni.grid.column.Date
 */
Ext.define('Uni.grid.column.Date', {
    extend: 'Uni.grid.column.Default',
    xtype: 'uni-date-column',
    renderer: function (value) {
        return value ? Uni.DateTime.formatDateTimeShort(value) : '';
    }
});