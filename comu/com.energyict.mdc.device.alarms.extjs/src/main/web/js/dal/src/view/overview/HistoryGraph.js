Ext.define('Dal.view.overview.HistoryGraph', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.chart.theme.Base'
    ],
    ui: 'tile',
    alias: 'widget.history-graph',
    store: undefined,
    defaultColors: ['#BEE64B', '#33CC99', '#00CCCC', '#7ED4E6', '#2887C8', '#C3CDE6', '#7070CC', '#C9A0DC', '#733380', '#2D383A',
        '#5E8C31', '#7BA05B', '#4D8C57', '#3AA655', '#93DFB8', '#1AB385', '#29AB87', '#00CC99', '#00755E',
        '#8DD9CC', '#01786F', '#30BFBF', '#008080', '#8FD8D8',
        '#95E0E8', '#6CDAE7', '#76D7EA', '#0095B7', '#009DC4', '#02A4D3', '#47ABCC', '#4997D0', '#339ACC',
        '#93CCEA', '#00468C', '#0066CC', '#1560BD', '#0066FF', '#A9B2C3', '#4570E6', '#7A89B8', '#4F69C6',
        '#8D90A1', '#8C90C8', '#9999CC', '#ACACE6', '#766EC8', '#6456B7', '#3F26BF', '#8B72BE', '#652DC1', '#6B3FA0',
        '#8359A3', '#8F47B3', '#BF8FCC', '#803790', '#D6AEDD', '#C154C1', '#FC74FD', '#C5E17A', '#9DE093', '#63B76C', '#6CA67C', '#5FA777'],

    colors: null,
    colorsPerReasons: null,
    showLegend: true,
    tooltipAlarmMsg: null,
    width: 800,
    height: 300,
    layout: 'fit',
    xMinValue: null,
    xMaxValue: null,
    yMaxSumPerDay: null,
    dayInMilieconds: 86400 * 1000,
    fields: null,
    defaultFields: null,
    legendTitle: undefined,
    translationFields: null,
    translations: null,

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    },

    processStore: function () {
        var me = this;
        me.store.each(function (rec) {
            me.xMinValue = (me.xMinValue == null) ? rec.get('date') : Math.min(me.xMinValue, rec.get('date'));
            me.xMaxValue = Math.max(me.xMaxValue, rec.get('date'));
        });

        me.store.each(function (rec) {
            var sumPerDay = 0;
            Ext.Array.each(me.fields, function (field) {
                sumPerDay += rec.get(field);
            });
            me.yMaxSumPerDay = Math.max(me.yMaxSumPerDay, sumPerDay);
        });
    },

    constructColors: function () {
        var me = this;

        Ext.define('Ext.chart.theme.ColumnTheme', {
            extend: 'Ext.chart.theme.Base',
            constructor: function (config) {
                this.callParent([Ext.apply({
                    colors: me.defaultColors
                }, config)]);
            }
        });

        me.colors = new Ext.util.HashMap();
        if (me.colorPerReason) { // use custom colors
            Ext.Array.each(me.colorPerReason, function (color) {
                me.colors.add(color.reason, color.color);
            });
        }
        else { // use default colors
            var pos = 0;
            Ext.Array.each(me.fields, function (field) {
                me.colors.add(field, me.defaultColors[pos++]);
            });
        }
    },

    constructTranslation: function () {
        var me = this;

        me.translations = new Ext.util.HashMap();
        if (me.translationFields) {
            Ext.Array.each(me.translationFields, function (translationField) {
                me.translations.add(translationField.reason, translationField.translation);
            });
        }
    },

    refresh: function (data) {
        var me = this, storeFields;

        if ((me.fields == null) || (me.fields.length == 0)) {
            me.fields = data.fields;
            storeFields = data.fields.slice();
            storeFields.push('date');
        }
        else {
            storeFields = me.fields.slice();
            storeFields.push('date');
        }

        me.store = Ext.create('Ext.data.JsonStore', {
            fields: storeFields,
            data: data.data
        });
        me.processStore();
        me.constructColors();
        me.constructTranslation();

        // remove previous chart
        me.remove(me.down('chart'), true);
        me.add(me.getChart(me.store));
        me.doLayout();
    },

    destroyControls: function () {
        var me = this;
        me.xMinValue = null;
        me.xMaxValue = null;
        me.yMaxSumPerDay = 0;
        me.colors = null;
        me.fields = me.defaultFields;
        me.remove(me.down('chart'), true);
    },

    getChart: function (store) {
        var me = this,
            cmp = {
                xtype: 'chart',
                store: store,
                theme: 'ColumnTheme',
                legend: {
                    visible: me.showLegend,
                    position: 'bottom',
                    boxStrokeWidth: 0,
                    calcPosition: function () {
                        var me = this;
                        if (me.position == 'bottom') {
                            var x, y,
                                legendWidth = me.width,
                                legendHeight = me.height,
                                chart = me.chart,
                                chartBBox = chart.chartBBox,
                                insets = chart.insetPadding,
                                chartWidth = chartBBox.width - (insets * 2),
                                chartHeight = chartBBox.height - (insets * 2),
                                chartX = chartBBox.x + insets,
                                chartY = chartBBox.y + insets,
                                surface = chart.surface,
                                mfloor = Math.floor;
                            x = mfloor(chartX + chartWidth - legendWidth);
                            y = mfloor(surface.height - legendHeight) - insets;
                            return {x: x, y: y};
                        }
                        return Ext.chart.Legend.prototype.calcPosition.call();
                    },
                    createLegendItem: function (series, yFieldIndex) {
                        var me = this;
                        return new Ext.chart.LegendItem({
                            legend: me,
                            series: series,
                            surface: me.chart.surface,
                            yFieldIndex: yFieldIndex,
                            onMouseDown: Ext.emptyFn,
                            onMouseOver: Ext.emptyFn,
                            onMouseOut: Ext.emptyFn
                        });
                        return legendItem;
                    }
                },
                axes: [{
                    type: 'Category',
                    position: 'bottom',
                    fields: ['date'],
                    title: false,
                    grid: false,
                    dashSize: 0,
                    label: {
                        renderer: function (v) {
                            if (((v - me.xMinValue) / me.dayInMilieconds % 5) == 0) {// decrease the number of visible labels
                                return Uni.DateTime.formatDateShort(new Date(Number(v)));
                            }
                            return "";
                        }
                    }
                }, {
                    type: 'Numeric',
                    position: 'left',
                    fields: me.fields,
                    title: false,
                    dashSize: 0,
                    label1: {
                        hidden: true
                    },
                    majorTickSteps: 0,
                    maximum: me.yMaxSumPerDay,
                    calcEnds: function () {
                        return {
                            from: 0,
                            to: me.yMaxSumPerDay,
                            power: 2,
                            step: me.yMaxSumPerDay,
                            steps: 1
                        }
                    }
                }],
                series: [{
                    type: 'column',
                    axis: 'left',
                    xField: 'date',
                    yField: me.fields,
                    stacked: true,
                    title: me.legendTitle,
                    tips: {
                        trackMouse: true,
                        layout: 'fit',
                        listeners: {
                            beforeshow: function (item) {
                                return item.layout.owner.html != undefined;
                            }
                        },
                        renderer: function (record, item) {
                            var tooltip = this,
                                sumPerDay = 0, pos = 0, html = '',
                                title = Uni.DateTime.formatDateLong(new Date(Number(record.get('date'))));

                            Ext.Array.each(me.fields, function (field) {
                                var count = record.get(field);
                                if (count == 0) {
                                    return;
                                }
                                html += '<tr>';
                                html += '<td>' + '<svg width="12" height="12" ><rect width="12" height="12" fill="' + me.colors.get(field) + '"></rect></svg>';
                                html += ' ';
                                html += Ext.String.format(me.tooltipAlarmMsg, me.translationFields ? me.translations.get(field) : field, count) + '</td>';
                                html += '</tr>';
                                sumPerDay += count;
                                pos++;
                            });

                            if (sumPerDay != 0) {
                                html = '<table>' +
                                    '<tr><td><b>' + title + '</b></td></tr>' +
                                    '<tr><td>' + Ext.String.format(Uni.I18n.translate('alarms.totalAlarmsRaised', 'DAL', 'Total alarms raised on this day: {0}'), sumPerDay) + '</td></tr>' +
                                    '<tr><td>&nbsp</td></tr>' +
                                    html +
                                    '</table>';
                                tooltip.update(html);
                            }
                            else {
                                tooltip.update(undefined);
                            }
                        }
                    },
                    renderer1: function (sprite, record, attr, index, store) {
                        return Ext.apply(attr, {
                            fill: me.colors.get(me.fields[index % me.fields.length])
                        });
                    },
                    getLegendColor: function (index) {
                        var columnBar = this;

                        if (columnBar.style && columnBar.style.fill) {
                            return columnBar.style.fill;
                        } else {
                            if (me.colors) {
                                return me.colors.get(me.fields[index % me.fields.length]);
                            }
                        }
                    }
                }]
            };
        return cmp;
    }

});