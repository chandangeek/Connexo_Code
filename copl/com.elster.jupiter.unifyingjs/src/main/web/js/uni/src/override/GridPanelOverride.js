/**
 * @class Uni.override.GridPanelOverride
 */
Ext.define('Uni.override.GridPanelOverride', {
    override: 'Ext.grid.Panel',
    requires: [
        'Uni.grid.plugin.ShowConditionalToolTip'
    ],
    plugins: ['showConditionalToolTip'],

    /**
     * Do not select row when column is of type actioncolumn.
     */
    listeners: {
        cellclick: function (gridView, htmlElement, columnIndex, dataRecord) {
            var type = gridView.getHeaderCt().getHeaderAtIndex(columnIndex).getXType();
            if (type === 'actioncolumn') {
                return true;
            }
        }
    }
});