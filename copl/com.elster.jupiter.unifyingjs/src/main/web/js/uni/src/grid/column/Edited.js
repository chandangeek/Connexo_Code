Ext.define('Uni.grid.column.Edited', {
    extend: 'Ext.grid.column.Column',
    xtype: 'edited-column',
    header: '',
    width: 30,
    align: 'left',
    emptyText: '',
    requires: [
        'Uni.form.field.EditedDisplay'
    ],

    renderer: function (value, metaData, record, rowIndex, colIndex) {
        return new Uni.form.field.EditedDisplay().renderer.apply(this.columns[colIndex], arguments);
    }
});