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
        gridView.on('beforerefresh', this.destroyTooltips);
        gridView.on('beforedestroy', this.destroyTooltips, this, {single: true});
    },

    /**
     * @private
     */
    setTooltip: function (grid) {
        var gridPanel = grid.up('gridpanel');
        Ext.Array.each(gridPanel.columns, function (column) {
            var header = Ext.get(gridPanel.getEl().query('#' + column.id + '-titleEl')[0]);

            if (column.text && (header.getWidth(true) < header.getTextWidth())) {
                header.tooltip = Ext.create('Ext.tip.ToolTip', {
                    target: header,
                    html: column.text
                });
            }

            if (column.$className === 'Ext.grid.column.Column' || column.$className === 'Ext.grid.column.Date') {
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
        Ext.Array.each(grid.getEl().query('.x-grid-cell'), function (item) {
            var cell = Ext.get(item);

            cell.tooltip && cell.tooltip.destroy();
        });
    }
});