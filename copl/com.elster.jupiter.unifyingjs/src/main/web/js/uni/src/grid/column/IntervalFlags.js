Ext.define('Uni.grid.column.IntervalFlags', {
    extend: 'Ext.grid.column.Column',
    xtype: 'interval-flags-column',
    header: Uni.I18n.translate('intervalFlags.label', 'UNI', 'Interval flags'),
    minWidth: 60,
    align: 'left',

    requires: [
        'Uni.form.field.IntervalFlagsDisplay'
    ],

    deferredRenderer: function (value, record, view) {
        var me = this;
        var cmp = view.getCell(record, me).down('.x-grid-cell-inner');
        var field = new Uni.form.field.IntervalFlagsDisplay({
            fieldLabel: false
        });
        cmp.setHTML('');
        field.setValue(value);
        field.render(cmp);

        Ext.defer(view.updateLayout, 10, view);
    },

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        var me = metaData.column;
        Ext.defer(me.deferredRenderer, 1, me, [value, record, view]);
    }
});