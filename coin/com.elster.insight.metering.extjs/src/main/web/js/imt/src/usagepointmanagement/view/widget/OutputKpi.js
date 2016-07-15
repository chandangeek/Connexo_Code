Ext.define('Imt.usagepointmanagement.view.widget.OutputKpi', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.output-kpi-widget',
    overflowY: 'auto',

    layout: {
        type: 'hbox',
        align: 'middle'
    },

    requires: [
        'Ext.chart.Chart',
        'Ext.chart.series.Pie'
    ],

    config: {
        output: null,
        purpose: null,
        router: null
    },

    titleAlign: 'center',
    header: {
        htmlEncode: false
    },

    initComponent: function () {
        var me = this,
            output = me.getOutput(),
            router = me.getRouter(),
            purpose = me.getPurpose(),
            url = router.getRoute('usagepoints/view/purpose/output').buildUrl({
                purposeId: purpose.id,
                outputId: output.getId()
            }),
            data = []
            ;

        output.get('statistics').map(function(item) {
            var queryParams = {};
            queryParams[item.key] = true;
            data.push({
                name: item.displayName,
                data: item.count,
                url: router.getRoute('usagepoints/view/purpose/output').buildUrl({
                    purposeId: purpose.id,
                    outputId: output.getId(),
                    tab: 'registers'
                }, queryParams)
            });
        });

        var store = Ext.create('Ext.data.JsonStore', {
            fields: ['name', 'data', 'url'],
            data: data
        });

        me.title = '<a href="' + url + '">'
            + Ext.String.htmlEncode(output.get('name'))
            + '</a>';

        //there is no possibility to display HTML inside of legend, so legend is separated from chart
        me.items = [{
            xtype: 'chart',
            width: 200,
            height: 150,
            animate: false,
            //legend: {
            //    position: 'right',
            //    boxStrokeWidth: 0
            //},
            shadow: false,
            store: store,
            theme: 'Base',
            series: [{
                type: 'pie',
                angleField: 'data',
                //showInLegend: true,
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
                        margin: 4
                    }
                }
            }]
        }, {
            xtype: 'dataview',
            store: store,
            itemSelector: 'div.legend',
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="legend">',
                        '<b>{name}</b>: <a href="{url}">{data}</a>',
                    '</div>',
                '</tpl>'
            )
        }];

        this.callParent(arguments);
    }

});