/**
 * @class Uni.grid.column.DefaultColumn
 */
Ext.define('Uni.grid.column.DefaultColumn', {
    extend: 'Ext.grid.column.Column',
    xtype: 'default-column',
    header: Uni.I18n.translate('general.default', 'UNI', 'Default'),
    minWidth: 120,
    align: 'center',

    renderer: function (value, metadata) {
        if (value === true) {
           //TODO change this code so when hovering of selecting the row, the hightlighted icon is shown.
            // icon that should be shown in that case: defaultItemHighlight.png
            return '<div style="background-image: url(../ext/packages/uni-theme-skyline/resources/images/grid/defaultItem.png); height: 16px; width: 16px; "> </div>';
        } else {
            return '';
        }
    }
});