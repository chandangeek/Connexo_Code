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
                    iconCls: value.iconCls,
                    tipString: value.tipString
                });
            cmp.setHTML('');
            field.setValue(value.value);
            field.render(cmp);
        } catch (e) {
        }
    },

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        var me = metaData.column;
        var res = {};
        if (Ext.isDefined(value.editedTime)) {
            var time = Ext.isDate(value.editedTime) ? value.editedTime : new Date(value.editedTime);
            res.value = time;
            res.iconCls = 'uni-icon-edit';
            res.tipString = Uni.I18n.formatDate('editedDate.format', time, 'UNI', '\\E\\d\\i\\t\\e\\d \\o\\n F d, Y \\a\\t H:i')
        }
        if (Ext.isDefined(value.deletedTime)) {
            res.value = value.deletedTime;
            res.iconCls = 'uni-icon-deleted';
            res.tipString = Uni.I18n.formatDate('deletedDate.format', value.deletedTime, 'UNI', '\\D\\e\\l\\e\\t\\e\\d \\o\\n F d, Y \\a\\t H:i')
        }
        Ext.defer(me.deferredRenderer, 1, me, [res, record, view]);
    }
});