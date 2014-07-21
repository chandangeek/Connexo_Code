/**
 * @class Uni.grid.column.ReadingType
 */
Ext.define('Uni.grid.column.ReadingType', {
    extend: 'Ext.grid.column.Column',
    xtype: 'reading-type-column',
    header: Uni.I18n.translate('readingType.label', 'UNI', 'Reading type'),
    minWidth: 280,
    align: 'left',

    requires: [
        'Ext.panel.Tool',
        'Ext.util.Point',
        'Uni.view.window.ReadingTypeDetails',
        'Uni.form.field.ReadingTypeDisplay'
    ],

    deferredRenderer: function (value, record, view) {
        var me = this;
        var cmp = view.getCell(record, me).down('.x-grid-cell-inner');
        var field = new Uni.form.field.ReadingTypeDisplay({
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