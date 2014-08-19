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
        grid.on('refresh', this.setTooltip);
        grid.on('resize', this.setTooltip);
        grid.on('beforedestroy', this.destroyTooltips, this, {single: true});
    },

    /**
     * @private
     */
    setTooltip: function (grid) {
        Ext.Array.each(grid.columns, function (column) {
            if (column.$className === 'Ext.grid.column.Column') {
                Ext.Array.each(grid.getEl().query('.x-grid-cell-headerId-' + column.id), function (item) {
                    var cell = Ext.get(item),
                        inner = cell.down('.x-grid-cell-inner'),
                        text = inner ? Ext.util.Format.stripTags(inner.getHTML()) : false;

                    cell.tooltip && cell.tooltip.destroy();

                    if (text && (cell.getWidth(true) < cell.getTextWidth())) {
                        cell.tooltip = Ext.create('Ext.tip.ToolTip', {
                            target: cell,
                            html: text
                        });
                    }
                });
            }
        });
    },

    /**
     * @private
     */
    destroyTooltips: function (grid) {
        grid.un('refresh', this.setTooltip);
        grid.un('resize', this.setTooltip);
        Ext.Array.each(grid.getEl().query('.x-grid-cell'), function (item) {
            var cell = Ext.get(item);

            cell.tooltip && cell.tooltip.destroy();
        });
    }
});