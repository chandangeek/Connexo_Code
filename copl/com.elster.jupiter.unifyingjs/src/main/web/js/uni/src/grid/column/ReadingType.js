/**
 * @class Uni.grid.column.ReadingType
 */
Ext.define('Uni.grid.column.ReadingType', {
    extend: 'Ext.grid.column.Column',
    xtype: 'reading-type-column',
    header: Uni.I18n.translate('readingType.label', 'UNI', 'Reading type'),
    minWidth: 280,
    align: 'left',
    showTimeAttribute: true,

    requires: [
        'Ext.panel.Tool',
        'Ext.util.Point',
        'Uni.view.window.ReadingTypeDetails',
        'Uni.form.field.ReadingTypeDisplay'
    ],

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        var me = Ext.Array.findBy(this.columns, function (item) {
                return item.$className === 'Uni.grid.column.ReadingType';
            }),
            field = new Uni.form.field.ReadingTypeDisplay();

        return field.renderer.apply(me, [value, field, view, record]);
    },

    // If need to make a link from reading type display field override this method and provide url inside
    // See example in Mdc.view.setup.deviceloadprofilechannels.Grid 26:17
    makeLink: function (record) {
        return null; // Link url
    }
});