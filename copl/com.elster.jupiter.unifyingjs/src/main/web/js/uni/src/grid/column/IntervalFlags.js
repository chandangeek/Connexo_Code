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
        return new Uni.form.field.IntervalFlagsDisplay().renderer.apply(this.columns[colIndex], arguments);
    }
});