Ext.define('Imt.usagepointmanagement.view.widget.OutputKpi', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.output-kpi-widget',
    overflowY: 'auto',
    layout: 'fit',

    requires: [
        'Ext.chart.Chart',
        'Ext.chart.series.Pie'
    ],

    config: {
        output: null,
        router: null
    },

    initComponent: function () {
        var me = this,
            output = me.getOutput(),
            router = me.getRouter,
            data = []
            ;

        output.get('statistics').map(function(item){
            data.push({
                name: item.displayName,
                data: item.count
            })
        });

        var store = Ext.create('Ext.data.JsonStore', {
            fields: ['name', 'data'],
            data: data
        });

        me.title = output.get('name');
        me.items = {
            xtype: 'chart',
            width: 300,
            height: 200,
            animate: false,
            legend: true,
            store: store,
            theme: 'Base',
            series: [{
                type: 'pie',
                angleField: 'data',
                showInLegend: true,
                tips: {
                    trackMouse: true,
                    width: 140,
                    height: 28,
                    renderer: function(storeItem, item) {
                        // calculate and display percentage on hover
                        var total = 0;
                        store.each(function(rec) {
                            total += rec.get('data');
                        });
                        this.setTitle(storeItem.get('name') + ': ' + Math.round(storeItem.get('data') / total * 100) + '%');
                    }
                },
                highlight: {
                    opacity: 0.6,
                    segment: {
                        margin: 0
                    }
                },
                label: {
                    field: 'name',
                    display: 'rotate',
                    contrast: true,
                    font: '18px Arial'
                }
            }]
        };

        this.callParent(arguments);
    }

});