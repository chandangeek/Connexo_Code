Ext.define('Uni.grid.column.Icon', {
    extend: 'Ext.grid.column.Column',
    xtype: 'icon-column',
    header: null,
    minWidth: 100,
    align: 'left',
    requires: [
        'Uni.form.field.IconDisplay'
    ],

    deferredRenderer: function (value, record, view) {
        try {
            var me = this,
                cmp = view.getCell(record, me).down('.x-grid-cell-inner'),
                field = new Uni.form.field.IconDisplay({
                    fieldLabel: false,
                    iconCls: 'swfwse',
                    tipString: Uni.I18n.formatDate('editedDate.format', value, 'MDC', '\\E\\d\\i\\t\\e\\d \\o\\n F d, Y \\a\\t H:i')
                });
            cmp.setHTML('');
            field.setValue(value);
            field.render(cmp);
        } catch (e) {
        }
    },

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        var me = metaData.column;
        if (value instanceof Object && value.value) {
            Ext.defer(me.deferredRenderer, 1, me, [value.value, record, view]);
        }
    }
});