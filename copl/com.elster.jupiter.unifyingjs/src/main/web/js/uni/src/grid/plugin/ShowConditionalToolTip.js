/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.plugin.ShowConditionalToolTip
 */
Ext.define('Uni.grid.plugin.ShowConditionalToolTip', {
    extend: 'Ext.AbstractPlugin',
    requires: [
        'Uni.util.String',
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

        //do not remove the check below. Only grids with 20 items or less can use conditional tooltips, as this is causing MAJOR
        //performance issues when used on large grids. !!!
        //CXO-3008 - [Performance] Rendering search result of 1000 devices takes ages
        if(!Ext.isEmpty(grid.getStore()) && grid.getStore().getCount()<=20){
            var gridPanel = grid.up('gridpanel');
            if (gridPanel.rendered && !grid.isHidden()) {
                Ext.suspendLayouts();
                var tm = new Ext.util.TextMetrics();
                Ext.Array.each(gridPanel.columns, function (column) {
                    if (!column.isHidden() && column.getEl()) {
                        var header = column.getEl().down('.' + Ext.baseCSSPrefix + 'column-header-inner');

                        if (column.text && (header !== null && header.getWidth(true) < header.getTextWidth())) {
                            header.set({'data-qtip': column.text});
                        } else if (header !== null) {
                            header.set({'data-qtip': undefined});
                        }

                        if (   column.$className === 'Ext.grid.column.Column'
                            || column.$className === 'Ext.grid.column.Date'
                            || column.$className === 'Ext.grid.column.Template'
                            || column.$className === 'Uni.grid.column.Duration'
                            || column.$className === 'Uni.grid.column.search.DeviceType'
                            || column.$className === 'Uni.grid.column.Date'
                            || column.$className === 'Uni.grid.column.search.DeviceConfiguration'
                            || column.$className === 'Uni.grid.column.search.Boolean') {

                            var first = grid.getEl().down(grid.getCellInnerSelector(column));
                            var width = first && first.getWidth(true);
                            if (width) {
                                Ext.each(grid.getEl().query(grid.getCellSelector(column)), function (el) {
                                    var cell = Ext.get(el),
                                        inner = cell.down('.' + Ext.baseCSSPrefix + 'grid-cell-inner');
                                    if (inner) {
                                        var text = Uni.util.String.stripTags(inner.getHTML()),
                                            tooltip = cell.getAttribute('data-qtip');

                                        if (text && (width < tm.getSize(text).width)) {
                                            cell.set({'data-qtip': tooltip || text});
                                        } else {
                                            cell.set({'data-qtip': (tooltip !== Ext.String.htmlEncode(text) ? tooltip : null) || undefined});
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
    }
});