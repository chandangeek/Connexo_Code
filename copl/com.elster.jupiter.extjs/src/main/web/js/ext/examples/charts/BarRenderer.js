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
        animate: true,
        style: 'background:#fff',
        shadow: false,
        store: store1,
        axes: [{
            type: 'Numeric',
            position: 'bottom',
            fields: ['data1'],
            label: {
                renderer: Ext.util.Format.numberRenderer('0,0')
            },
            title: 'Number of Hits',
            minimum: 0
        }, {
            type: 'Category',
            position: 'left',
            fields: ['name'],
            title: 'Month of the Year'
        }],
        series: [{
            type: 'bar',
            axis: 'bottom',
            label: {
                display: 'insideEnd',
                field: 'data1',
                renderer: Ext.util.Format.numberRenderer('0'),
                orientation: 'horizontal',
                color: '#333',
                'text-anchor': 'middle',
                contrast: true
            },
            xField: 'name',
            yField: ['data1'],
            //color renderer
            renderer: function(sprite, record, attr, index, store) {
                var fieldValue = Math.random() * 20 + 10,
                    value = (record.get('data1') >> 0) % 5,
                    color = ['rgb(213, 70, 121)', 
                             'rgb(44, 153, 201)', 
                             'rgb(146, 6, 157)', 
                             'rgb(49, 149, 0)', 
                             'rgb(249, 153, 0)'][value];
                             
                return Ext.apply(attr, {
                    fill: color
                });
            }
        }]
    });


    var win = Ext.create('Ext.Window', {
        width: 800,
        height: 600,
        minHeight: 400,
        minWidth: 550,
        hidden: false,
        maximizable: true,
        title: 'Bar Renderer',
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
        }],
        items: chart
    });
});
