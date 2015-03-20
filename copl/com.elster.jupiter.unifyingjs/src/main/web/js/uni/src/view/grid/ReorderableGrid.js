/**
 * @class Uni.view.grid.ReorderableGrid
 *
 * Grid that allows dragging and dropping of rows in the same grid, used to reorder rows in a grid.
 */
Ext.define('Uni.view.grid.ReorderableGrid', {
    extends: 'Ext.grid.Panel',
    xtype: 'uni-grid-reorderablegrid',

    viewConfig: {
        plugins: {
            ptype: 'gridviewdragdrop',
            dragText: Uni.I18n.translate('grid.ReorderableGrid.dragtext', 'UNI', 'Reorder rows')
        }
    },

    initComponent: function () {
        var me = this;

        me.callParent(arguments);

        me.getView().on('drop', me.onDropRow, me);
    },

    /**
     * Template method to fill in to listen to changes whenever a row is dragged
     * and dropped in a grid.
     *
     * @param node
     * @param data
     * @param overModel
     * @param dropPosition
     */
    onDropRow: function (node, data, overModel, dropPosition) {
        // Template method.
    }
});