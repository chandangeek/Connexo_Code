/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.require([
    'Ext.chart.*',
    'Ext.Window', 
    'Ext.fx.target.Sprite', 
    'Ext.layout.container.Fit', 
    'Ext.window.MessageBox'
]);

Ext.onReady(function () {
    
    var chart = Ext.create('Ext.chart.Chart', {
        style: 'background:#fff',
        animate: true,
        store: store1,
        legend: {
            position: 'bottom'
        },
        axes: [{
            type: 'Numeric',
            position: 'left',
            fields: ['data1', 'data2', 'data3', 'data4', 'data5', 'data6', 'data7'],
            title: 'Number of Hits',
            grid: {
                odd: {
                    opacity: 1,
                    fill: '#ddd',
                    stroke: '#bbb',
                    'stroke-width': 1
                }
            },
            minimum: 0,
            adjustMinimumByMajorUnit: 0
        }, {
            type: 'Category',
            position: 'bottom',
            fields: ['name'],
            title: 'Month of the Year',
            grid: true,
            label: {
                rotate: {
                    degrees: 315
                }
            }
        }],
        series: [{
            type: 'area',
            highlight: false,
            axis: 'left',
            xField: 'name',
            yField: ['data1', 'data2', 'data3', 'data4', 'data5', 'data6', 'data7'],
            style: {
                opacity: 0.93
            }
        }]
    }); 

    
    var win = Ext.create('Ext.window.Window', {
        width: 800,
        height: 600,
        minHeight: 400,
        minWidth: 550,
        hidden: false,
        shadow: false,
        maximizable: true,
        title: 'Area Chart',
        constrain: true,
        autoShow: true,
        layout: 'fit',
        tbar: [{
            text: 'Save Chart',
            handler: function() {
                Ext.MessageBox.confirm('Confirm Download', 'Would you like to download the chart as an image?', function(choice){
                    if(choice == 'yes'){
                        chart.save({
                            type: 'image/png'
                        });
                    }
                });
            }
        }, {
            text: 'Reload Data',
            handler: function() {
                // Add a short delay to prevent fast sequential clicks
                window.loadTask.delay(100, function() {
                    store1.loadData(generateData());
                });
            }
        }, {
            enableToggle: true,
            pressed: true,
            text: 'Animate',
            toggleHandler: function(btn, pressed) {
                chart.animate = pressed ? { easing: 'ease', duration: 500 } : false;
            }
        }],
        items: chart
    }); 
});
