/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.performance.PoolPortChart', {
    extend: 'Ext.chart.Chart',
    xtype: 'poolPortChart',
    requires: [ 'Ext.chart.axis.Category', 'Ext.chart.series.Bar', 'CSMonitor.theme.Elster'],
    theme: 'Elster:gradient',
    animate: true,
    store: 'performance.PoolPort',
    shadow: true,
    width: 500,
    height: 400,

    axes: [
        {
            type: 'Numeric',
            position: 'bottom',
            fields: ['ports'],
            label: {
                renderer: Ext.util.Format.numberRenderer('0')
            },
            title: 'Number of communication ports',
            grid: {
                odd: {
                    opacity: 1,
                    fill: '#f9f9f9',
                    stroke: '#ddd'
                }
            },
            minimum: 0
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
                    field: 'ports',
                    orientation: 'horizontal',
                    color: '#fff',
                    'text-anchor': 'middle'
                },
                xField: 'name',
                yField: ['ports']
            }
        ];

        this.callParent(arguments);
    },

    setMajorTickSteps: function(numberOfMajorTicks) {
        this.axes.items[0].majorTickSteps = numberOfMajorTicks;
    }

});