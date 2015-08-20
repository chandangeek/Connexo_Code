/**
 * @class Uni.grid.plugin.ShowConditionalToolTip
 */
Ext.define('Uni.grid.plugin.ShowConditionalToolTip', {
    extend: 'Ext.AbstractPlugin',
    requires: [
        'Ext.util.Format',
        'Ext.tip.ToolTip'
    ],
    alias: 'plugin.showConditionalToolTip',

    /**
     * @private
     */
    init: function (grid) {
        var gridView = grid.getView();

        gridView.on('refresh', this.setTooltip);
        gridView.on('resize', this.setTooltip);
    },

    /**
     * @private
     */
    setTooltip: function (grid) {
        var gridPanel = grid.up('gridpanel');
        if (gridPanel.rendered && !grid.isHidden()) {
            Ext.suspendLayouts();
            Ext.Array.each(gridPanel.columns, function (column) {
                if (!column.isHidden()) {
                    var header = column.getEl().down('.' + Ext.baseCSSPrefix + 'column-header-inner');

                    if (column.text && (header.getWidth(true) < header.getTextWidth())) {
                        header.set({'data-qtip': column.text});
                    } else {
                        header.set({'data-qtip': undefined});
                    }

                    if (   column.$className === 'Ext.grid.column.Column'
                        || column.$className === 'Ext.grid.column.Date'
                        || column.$className === 'Ext.grid.column.Template'
                        || column.$className === 'Uni.grid.column.Duration') {

                        var first = grid.getEl().down(grid.getCellInnerSelector(column));
                        var width = first && first.getWidth(true);
                        if (width) {
                            Ext.each(grid.getEl().query(grid.getCellSelector(column)), function (el) {
                                var cell = Ext.get(el),
                                    inner = cell.down('.' + Ext.baseCSSPrefix + 'grid-cell-inner');

                                if (inner) {
                                    var text = Ext.util.Format.stripTags(inner.getHTML()),
                                        tooltip = cell.getAttribute('data-qtip');

                                    if (text && (width < cell.getTextWidth())) {
                                        cell.set({'data-qtip': tooltip || text});
                                    } else {
                                        cell.set({'data-qtip': (tooltip !== text ? tooltip : null) || undefined});
                                    }
                                }
                            });
                        }
                    }
                }
            });

            Ext.resumeLayouts(true);
        }
    }
});