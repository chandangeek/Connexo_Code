/**
 * @class Uni.grid.column.RemoveAction
 */
Ext.define('Uni.grid.column.RemoveAction', {
    extend: 'Uni.grid.column.Action',
    alias: 'widget.uni-actioncolumn-remove',
    width: 80,
    align: 'right',
    iconCls: 'icon-cancel-circle2',
    tooltip: Uni.I18n.translate('general.remove', 'UNI', 'Remove'),

    handler: function (grid, rowIndex, colIndex, item, e, record, row) {
        this.fireEvent('remove', record);
    }
});