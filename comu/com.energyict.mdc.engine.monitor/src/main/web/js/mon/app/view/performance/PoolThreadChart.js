/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.performance.PoolThreadChart', {
    extend: 'Ext.chart.Chart',
    xtype: 'poolThreadChart',
    requires: [ 'Ext.chart.series.Bar', 'Ext.chart.axis.Category', 'CSMonitor.theme.Elster'],

    theme: 'Elster:gradient',
    animate: true,
    store: 'performance.PoolThread',
    shadow: true,
    width: 500,
    height: 400,

    axes: [
        {
            type: 'Numeric',
            position: 'bottom',
            fields: ['threads'],
            label: {
                renderer: Ext.util.Format.numberRenderer('0')
            },
            title: 'Number of threads',
            minimum: 0,
            grid: {
                odd: {
                    opacity: 1,
                    fill: '#f9f9f9',
                    stroke: '#ddd'
                }
            }
        },
        {
            type: 'Category',
            position: 'left',
            fields: ['name'],
            title: 'Pools'
        }
    ],

    initComponent: function () {
        var me = this;
        this.series = [
            {
                type: 'bar',
                axis: 'bottom',
                highlight: true,
                label: {
                    display: 'insideEnd',
                    field: 'threads',
                    orientation: 'horizontal',
                    color: '#fff',
                    'text-anchor': 'middle'
                },
                xField: 'name',
                yField: ['threads']
            }
        ];

        this.callParent(arguments);
    },

    setMajorTickSteps: function(numberOfMajorTicks) {
        this.axes.items[0].majorTickSteps = numberOfMajorTicks;
    }

});