/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        'Ext.chart.series.Pie',
        'Imt.purpose.view.NoReadingsFoundPanel'
    ],

    config: {
        output: null,
        purpose: null,
        router: null
    },

    titleAlign: 'left',
    header: {
        style: {
            marginLeft: '55px'
        },
        htmlEncode: false
    },

    initComponent: function () {
        var me = this,
            output = me.getOutput(),
            router = me.getRouter(),
            purpose = me.getPurpose(),
            url = router.getRoute('usagepoints/view/purpose/output').buildUrl({
                purposeId: purpose.getId(),
                outputId: output.getId()
            }),
            data = [],
            total = output.get('total');

        me.title = '<a href="' + url + '">'
            + Ext.String.htmlEncode(output.get('name'))
            + '</a>';
        
        if (output.get('statistics').length) {
            output.get('statistics').map(function(item) {
                var queryParams = {},
                    percentageUrl;

                if (item.key == 'statisticsSuspect' || item.key == 'statisticsMissing') {
                    percentageUrl = router.getRoute('usagepoints/view/purpose/output').buildUrl({
                        purposeId: purpose.getId(),
                        outputId: output.getId()
                    }, queryParams);
                }
                data.push({
                    name: item.displayName,
                    key: item.key,
                    data: item.count,
                    url: percentageUrl,
                    percentage: Math.round(item.count / total * 100) + '%'
                });
            });

            var store = Ext.create('Ext.data.JsonStore', {
                fields: ['name', 'key', 'data', 'url', 'percentage'],
                data: data
            });

            //there is no possibility to display HTML inside of legend, so legend is separated from chart
            me.items = [{
                xtype: 'chart',
                width: 230,
                height: 200,
                animate: false,
                shadow: false,
                store: store,
                theme: 'Base',
                series: [{
                    type: 'pie',
                    angleField: 'data',
                    tips: {
                        bodyPadding: -10,
                        trackMouse: true,
                        showDelay: 0,
                        hideDelay: 0,
                        renderer: function(storeItem) {
                            this.setTitle(storeItem.get('percentage') + ' ' + storeItem.get('name').toLowerCase());
                        }
                    },
                    renderer: function (sprite, record, attributes) {
                        var color;
                        switch (record.get('key')) {
                            case 'statisticsSuspect':
                                color = 'rgba(235, 86, 66, 1)';
                                break;
                            case 'statisticsValid':
                                color = '#70BB51';
                                break;
                            case 'statisticsNotValidated':
                                color = '#71adc7';
                                break;
                            case 'statisticsMissing':
                                color = 'rgba(235, 86, 66, 0.3)';
                                break;
                        }
                        return Ext.apply(attributes, {
                            fill: color
                        });
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
                itemSelector: 'tr.trlegend',
                tpl: new Ext.XTemplate(
                    '<table>',
                    '<tpl for=".">',
                    '<tr class="trlegend"><td style="text-align: right"><b>{name}</b></td><td style="vertical-align: top">&nbsp;&nbsp;' +
                    '<tpl if="url"><a href="{url}">{percentage}</a>' +
                    '<tpl else>{percentage}</tpl>' +
                    '</td></tr>',
                    '</tpl>',
                    '</table>'
                )
            }];
        } else {
            me.items = [{
                xtype: 'no-readings-found-panel',
                itemId: 'up-no-readings-found-panel',
                width: 380,
                margin: '0 20 0 20',
                layout: 'fit'
            }];
        }

        this.callParent(arguments);
    }
});