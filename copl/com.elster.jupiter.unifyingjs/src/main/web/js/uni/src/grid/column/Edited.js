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
        var me = Ext.Array.findBy(this.columns, function (item) {
            return item.$className === 'Uni.grid.column.Edited';
        });

        return new Uni.form.field.EditedDisplay().renderer.apply(me, arguments);
    }
});