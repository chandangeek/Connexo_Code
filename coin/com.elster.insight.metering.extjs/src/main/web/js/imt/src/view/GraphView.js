/*
Added in scope of http://jira.eict.vpdc/browse/JP-8566
Solution found in https://github.com/highslide-software/highcharts.com/issues/2434
Please notice, that it can break https://github.com/highslide-software/highcharts.com/issues/1514
*/
Ext.define('Imt.view.GraphView', {
    extend: 'Ext.container.Container',
    alias: 'widget.highstockFixGraphView',

    initComponent: function () {
        this.callParent(arguments);

        var hsSeriesTranslate = Highcharts.Series.prototype.translate;

        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });

        Highcharts.Series.prototype.translate = function () {

            hsSeriesTranslate.apply(this, arguments);

            var series = this,
                pointPlacement = series.options.pointPlacement,
                dynamicallyPlaced = pointPlacement === 'between' || isNumber(pointPlacement);

            if (dynamicallyPlaced) {
                var xAxis = series.xAxis,
                    points = series.points,
                    dataLength = points.length,
                    i;

                for (i = 0; i < dataLength; i += 1) {
                    var point = points[i],
                        xValue = point.x;

                    point.clientX = xAxis.translate(xValue, 0, 0, 0, 1, pointPlacement);
                }
            }
        };
    }
});