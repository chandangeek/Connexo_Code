/**
 * @class Uni.util.GraphWithGrid
 *
 * This class contains common functions to synchronize Highstock graph and ExtJs grid interactions
 */
Ext.define('Uni.util.GraphWithGrid', {
    idProperty: null,    

    onBarSelect: function (point) {
        var me = this,            
            graphView = me.down('highstockFixGraphView'),
            grid = me.down('grid'),
            index = grid.getStore().findExact(me.idProperty, me.getValueFromPoint(point)),
            viewEl = grid.getView().getEl(),
            currentScrollTop = viewEl.getScroll().top,
            viewHeight = viewEl.getHeight(),
            rowOffsetTop = index * 29,
            newScrollTop;

        if (index > -1) {
            if (!(rowOffsetTop > currentScrollTop && rowOffsetTop < currentScrollTop + viewHeight)) {
                newScrollTop = rowOffsetTop - 15;
                if (newScrollTop > 0) {
                    grid.getView().getEl().setScrollTop(newScrollTop);
                } else {
                    grid.getView().getEl().setScrollTop(0);
                }
            }

            me.down('preview-container').suspendEvent('rowselect');
            grid.getSelectionModel().select(index);
            me.down('preview-container').resumeEvent('rowselect');
            me.setSelectionColor(graphView, point);
        }
    },

    onRowSelect: function (record) {
        var me = this,
            index = me.down('grid').getStore().indexOf(record),
            graphView = me.down('highstockFixGraphView'),
            selectPoint = function () {
                var data = graphView.chart.series[0].data,
                    intervalEnd = record.get(me.idProperty).getTime(),
                    xAxis = graphView.chart.xAxis[0],
                    currentExtremes = xAxis.getExtremes(),
                    range = currentExtremes.max - currentExtremes.min,
                    point = data[data.length - index - 1];

                if (intervalEnd + range / 2 > currentExtremes.dataMax) {
                    xAxis.setExtremes(currentExtremes.dataMax - range, currentExtremes.dataMax);
                } else if (intervalEnd - range / 2 < currentExtremes.dataMin) {
                    xAxis.setExtremes(currentExtremes.dataMin, currentExtremes.dataMin + range);
                } else if (!(intervalEnd > currentExtremes.min && intervalEnd < currentExtremes.max)) {
                    xAxis.setExtremes(intervalEnd - range / 2, intervalEnd + range / 2);
                }
                me.setSelectionColor(graphView, point);
                point.select(true, false);
            };
        
        if (index > -1) {
            if (graphView.chart) {
                selectPoint();
            } else if (graphView.rendered) {
                me.on('graphrendered', selectPoint, me, {singelton: true});
            }
        }
    },

    setSelectionColor: function (graphView, point) {        
        if (point.pointAttr) {
            point.pointAttr.select.fill = point.pointAttr.hover.fill;
            point.series.options.states = Ext.merge(point.series.options.states || {}, {
                select: {
                    color: point.pointAttr.hover.fill
                }
            });
        }
    }
});