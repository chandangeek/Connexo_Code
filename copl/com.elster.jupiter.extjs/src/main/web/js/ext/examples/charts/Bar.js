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
    
    Ext.define('Ext.chart.theme.CustomBlue', {
        extend: 'Ext.chart.theme.Base',
        
        constructor: function(config) {
            var titleLabel = {
                font: 'bold 18px Arial'
            }, axisLabel = {
                fill: 'rgb(8,69,148)',
                font: '12px Arial',
                spacing: 2,
                padding: 5
            };
            
            this.callParent([Ext.apply({
               axis: {
                   stroke: '#084594'
               },
               axisLabelLeft: axisLabel,
               axisLabelBottom: axisLabel,
               axisTitleLeft: titleLabel,
               axisTitleBottom: titleLabel
           }, config)]);
        }
    });
    
    var chart = Ext.create('Ext.chart.Chart', {
        animate: true,
        shadow: true,
        store: store1,
        axes: [{
            type: 'Numeric',
            position: 'bottom',
            fields: ['data1'],
            label: {
                renderer: Ext.util.Format.numberRenderer('0,0')
            },
            title: 'Number of Hits',
            grid: true,
            minimum: 0
        }, {
            type: 'Category',
            position: 'left',
            fields: ['name'],
            title: 'Month of the Year'
        }],
        theme: 'CustomBlue',
        background: {
            gradient: {
                id: 'backgroundGradient',
                angle: 45,
                stops: {
                    0: {
                        color: '#ffffff'
                    },
                    100: {
                        color: '#eaf1f8'
                    }
                }
            }
        },
        series: [{
            type: 'bar',
            axis: 'bottom',
            highlight: true,
            tips: {
                trackMouse: true,
                renderer: function(storeItem, item) {
                    this.setTitle(storeItem.get('name') + ': ' + storeItem.get('data1') + ' views');
                }
            },
            label: {
              display: 'insideEnd',
                  field: 'data1',
                  renderer: Ext.util.Format.numberRenderer('0'),
                  orientation: 'horizontal',
                  color: '#333',
                'text-anchor': 'middle'
            },
            xField: 'name',
            yField: ['data1']
        }]
    });
        
    var win = Ext.create('Ext.window.Window', {
        width: 800,
        height: 600,
        minHeight: 400,
        minWidth: 550,
        hidden: false,
        maximizable: true,
        title: 'Bar Chart',
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
