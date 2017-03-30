/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.performance.ThreadsChart', {
    extend: 'Ext.chart.Chart',
    requires: ['Ext.chart.Chart', 'Ext.chart.series.Pie', 'CSMonitor.theme.Elster'],
    xtype: 'threadsChart',
    config: {
        threadsInUseText: 'Threads in use',
        threadsNotInUseText: 'Threads not in use'
    },
    theme: 'Elster:gradient',
    border: false,
    width: 300,
    height: 100,
    animate: true,
    store: 'performance.Threads',
    shadow: true,
    legend: {
        position: 'right'
    },
    insetPadding: 10,
    series: [
        {
            type: 'pie',
            field: 'data',
            showInLegend: true,
            label: {
                field: 'name',
                renderer: function (value, label, storeRecord) {
                    return storeRecord.get('data');
                },
                display: 'rotate',
                contrast: true,
                font: '18px Arial'
            }
        }
    ]

});
