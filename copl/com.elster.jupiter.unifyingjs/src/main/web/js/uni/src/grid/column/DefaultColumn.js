/**
 * @class Uni.grid.column.DefaultColumn
 */
Ext.define('Uni.grid.column.DefaultColumn', {
    extend: 'Ext.grid.column.Column',
    xtype: 'default-column',
    header: Uni.I18n.translate('obis.label', 'UNI', 'OBIS code'),
    minWidth: 120,
    align: 'left',

    renderer: function (value, metadata) {
        if (value === true) {
            metadata.style = "padding: 6px 16px 6px 16px;";
            return '<img src="../ext/packages/uni-theme-skyline/resources/images/grid/defaultItem.png">';
        } else {
            return '';
        }
    }
});