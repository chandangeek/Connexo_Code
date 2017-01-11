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
            total = output.get('total'),
            fields = ['name', 'key', 'data', 'url', 'percentage', 'detail', 'total', 'icon'],
            statisticsEdited = [];

        me.title = '<a href="' + url + '">'
            + Ext.String.htmlEncode(output.get('name'))
            + '</a>';

        if (output.get('statistics').length) {
            me.titleAlign = 'right';
            output.get('statistics').map(function(item) {
                var queryParams = {},
                    percentageUrl,
                    icon,
                    dataItem;

                if (item.key == 'statisticsSuspect' || item.key == 'statisticsMissing') {
                    percentageUrl = router.getRoute('usagepoints/view/purpose/output').buildUrl({
                        purposeId: purpose.getId(),
                        outputId: output.getId()
                    }, queryParams);
                }
                if (item.key == 'statisticsValid' || item.key == 'statisticsEdited') {
                    icon = '<span class="icon-info" data-qtip="' + me.prepareTooltip(item) + '"></span>';
                }

                dataItem = {
                    name: item.displayName,
                    key: item.key,
                    data: item.count,
                    url: percentageUrl,
                    percentage: Math.round(item.count / total * 100) + '%',
                    detail: item.detail,
                    total: item.total,
                    icon: icon
                };

                if (item.key != 'statisticsEdited') {
                    data.push(dataItem);
                } else {
                    statisticsEdited.push(dataItem);
                }
            });

            //there is no possibility to display HTML inside of legend, so legend is separated from chart
            me.items = [
                {
                    xtype: 'chart',
                    width: 230,
                    height: 200,
                    animate: false,
                    shadow: false,
                    store: Ext.create('Ext.data.Store', {
                        fields: fields,
                        data: data
                    }),
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
                                    var details = me.prepareTooltip(storeItem),
                                        text = '<div style="font-weight: bold;' + (details ? 'padding-bottom: 5px;' : '') + '">'
                                            + storeItem.get('name') + ' ' + storeItem.get('percentage')
                                            + '</div>'
                                            + details;


                                    this.update(text);
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
                    disableSelection: true,
                    deferInitialRefresh: false,
                    store: Ext.create('Ext.data.Store', {
                        fields: fields,
                        data: Ext.Array.merge(data, statisticsEdited)
                    }),
                    itemSelector: 'tr.trlegend',
                    tpl: ['<table>',
                        '<tpl for=".">',
                        '<tpl if="key == statisticsEdited">' +
                        '<tr>' +
                        '<td colspan="3">!!!</td>' +
                        '</tr>',
                        '</tpl>',
                        '<tr class="trlegend">' +
                        '<td>{name}</td>' +
                        '<td>' +
                        '<tpl if="url"><a href="{url}">{percentage}</a>' +
                        '<tpl else>{percentage}</tpl>' +
                        '</td>' +
                        '<td>{icon}</td>' +
                        '</tr>',
                        '</tpl>',
                        '</table>'],
                    listeners: {
                        resize: {
                            scope: me,
                            fn: me.onDataViewResize
                        }
                    }
                }
            ];
        } else {
            me.titleAlign = 'left';
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

        if ((key == 'statisticsValid' || key == 'statisticsEdited') && !Ext.isEmpty(detail)) {
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
    onDataViewResize: function (dataView) {
        var me = this,
            header,
            headerEl,
            firstCellEl;

        if (me.rendered && dataView.rendered) {
            header = me.getHeader();
            headerEl = header.getEl().down('.' + Ext.baseCSSPrefix + 'header-text-container');
            firstCellEl = dataView.getEl().down('.trlegend td');

            headerEl.setStyle('padding-right', headerEl.getWidth() - (firstCellEl.getX() + firstCellEl.getWidth(true) - headerEl.getX()) + 'px');
        }
    }
});