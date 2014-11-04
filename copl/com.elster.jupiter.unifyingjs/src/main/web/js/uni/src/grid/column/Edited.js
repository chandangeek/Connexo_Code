Ext.define('Uni.grid.column.Edited', {
    extend: 'Ext.grid.column.Column',
    xtype: 'edited-column',
    header: Uni.I18n.translate('editedDate.header', 'UNI', 'Edited'),
    minWidth: 100,
    align: 'left',

    requires: [
        'Uni.form.field.EditedDisplay'
    ],

    deferredRenderer: function (value, record, view) {
        try {
            var me = this,
                cmp = view.getCell(record, me).down('.x-grid-cell-inner'),
                field = new Uni.form.field.EditedDisplay({
                    fieldLabel: false
                });
            cmp.setHTML('');
            field.setValue(value);
            field.render(cmp);
        } catch (e) {
        }
    },

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        var me = metaData.column;

        Ext.defer(me.deferredRenderer, 1, me, [value, record, view]);
    }
});