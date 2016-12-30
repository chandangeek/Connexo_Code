Ext.define('Imt.usagepointmanagement.view.widget.OutputKpi', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.output-kpi-widget',
    overflowY: 'auto',

    layout: {
        type: 'hbox',
        align: 'top'
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

    titleAlign: 'right',
    header: {
        htmlEncode: false,
        hidden: true
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
                    percentageUrl,
                    icon;

                if (item.key == 'statisticsSuspect' || item.key == 'statisticsMissing') {
                    percentageUrl = router.getRoute('usagepoints/view/purpose/output').buildUrl({
                        purposeId: purpose.getId(),
                        outputId: output.getId()
                    }, queryParams);
                }
                if (item.key == 'statisticsValid') {
                    icon = '<span class="icon-info" data-qtip="' + me.prepareTooltip(item) + '"></span>';
                }
                data.push({
                    name: item.displayName,
                    key: item.key,
                    data: item.count,
                    url: percentageUrl,
                    percentage: Math.round(item.count / total * 100) + '%',
                    detail: item.detail,
                    total: item.total,
                    icon: icon
                });
            });

            var store = Ext.create('Ext.data.JsonStore', {
                fields: ['name', 'key', 'data', 'url', 'percentage', 'detail', 'total', 'icon'],
                data: data
            });

            //there is no possibility to display HTML inside of legend, so legend is separated from chart
            me.items = [
                {
                    xtype: 'chart',
                    width: 230,
                    height: 200,
                    animate: false,
                    shadow: false,
                    store: store,
                    theme: 'Base',
                    series: [
                        {
                            type: 'pie',
                            angleField: 'data',
                            tips: {
                                trackMouse: true,
                                showDelay: 0,
                                hideDelay: 0,
                                renderer: function (storeItem) {
                                    Ext.suspendLayouts();
                                    this.setTitle(storeItem.get('name') + ' ' + storeItem.get('percentage'));
                                    this.update(me.prepareTooltip(storeItem));
                                    Ext.resumeLayouts(true);
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
                        }
                    ]
                },
                {
                    xtype: 'dataview',
                    store: store,
                    itemSelector: 'tr.trlegend',
                    tpl: new Ext.XTemplate(
                        '<table>',
                        '<tpl for=".">',
                        '<tr class="trlegend">' +
                        '<td>{name}</td>' +
                        '<td>' +
                        '<tpl if="url"><a href="{url}">{percentage}</a>' +
                        '<tpl else>{percentage}</tpl>' +
                        '</td>' +
                        '<td>{icon}</td>' +
                        '</tr>',
                        '</tpl>',
                        '</table>'
                    ),
                    listeners: {
                        refresh: {
                            scope: me,
                            fn: me.onDataViewRefresh
                        }
                    }
                }
            ];
        } else {
            me.items = [
                {
                    xtype: 'no-readings-found-panel',
                    itemId: 'up-no-readings-found-panel',
                    width: 380,
                    margin: '0 20 0 20',
                    layout: 'fit'
                }
            ];
        }

        this.callParent(arguments);
    },

    prepareTooltip: function (data) {
        var text = '',
            key = data.get ? data.get('key') : data.key,
            detail = data.get ? data.get('detail') : data.detail,
            total = data.get ? data.get('total') : data.total;

        if (key == 'statisticsValid' && !Ext.isEmpty(detail)) {
            Ext.Array.each(detail, function (detailItem, index) {
                if (index > 0) {
                    text += '<br>';
                }
                text += detailItem.displayName + ' ' + Math.round(detailItem.count / total * 100) + '%';
            });
        }

        return text;
    },

    // needs to align title
    onDataViewRefresh: function (dataView) {
        var me = this,
            header,
            headerEl,
            firstCellEl;

        if (me.rendered && dataView.rendered) {
            header = me.getHeader();
            headerEl = header.getEl().down('.' + Ext.baseCSSPrefix + 'header-text-container');
            firstCellEl = dataView.getEl().down('.trlegend td');

            header.show();
            headerEl.setStyle('padding-right', headerEl.getWidth() - (firstCellEl.getX() + firstCellEl.getWidth(true) - headerEl.getX()) + 'px');
        }
    }
});