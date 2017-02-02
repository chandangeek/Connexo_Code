/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

    titleIsPartOfDataView: false,

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
            fields = ['name', 'key', 'data', 'url', 'percentage', 'detail', 'tooltip'],
            statisticsEdited = [],
            title = '<a href="' + url + '">'
                + Ext.String.htmlEncode(output.get('name'))
                + '</a>';

        if (!me.titleIsPartOfDataView) {
            me.title = title;
        }

        if (total > 0) {
            me.titleAlign = 'right';
            output.get('statistics').map(function(item) {
                var queryParams = {},
                    percentageUrl,
                    dataItem;

                if (item.key == 'statisticsSuspect' || item.key == 'statisticsMissing') {
                    percentageUrl = router.getRoute('usagepoints/view/purpose/output').buildUrl({
                        purposeId: purpose.getId(),
                        outputId: output.getId()
                    }, queryParams);
                }

                dataItem = {
                    name: item.displayName,
                    key: item.key,
                    data: item.count,
                    url: percentageUrl,
                    percentage: Math.round(item.count / total * 100) + '%',
                    detail: item.detail,
                    tooltip: me.prepareTooltip(item, 'VIEW')
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
                                renderer: function (record) {
                                    this.update(me.prepareTooltip(record, 'CHART'));
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
                    tpl: [
                        '<table>',
                        '<tpl if="this.showTitleInTable()">',
                        '<tr>',
                        '<td style="font-weight: bold; padding-bottom: 10px">{[ this.getTitle() ]}</td>',
                        '<td></td>',
                        '<td></td>',
                        '</tr>',
                        '</tpl>',
                        '<tpl for=".">',
                        '<tpl if="key == \'statisticsEdited\'">',
                        '<tr>',
                        '<td colspan="3"><hr></td>',
                        '</tr>',
                        '</tpl>',
                        '<tr class="trlegend">' +
                        '<td>{name}</td>',
                        '<td>',
                        '<tpl if="url"><a href="{url}">{percentage}</a>' +
                        '<tpl else>{percentage}</tpl>' +
                        '</td>',
                        '<td>',
                        '<tpl if="tooltip">',
                        '<span class="icon-info" data-qtip="{tooltip}"></span>',
                        '</tpl>',
                        '</td>' +
                        '</tr>',
                        '</tpl>',
                        '</table>',
                        {
                            disableFormats: true,
                            getTitle: function () {
                                return title;
                            },
                            showTitleInTable: function () {
                                return me.titleIsPartOfDataView;
                            }
                        }
                    ],
                    listeners: !me.titleIsPartOfDataView ? {
                        resize: {
                            scope: me,
                            fn: me.onDataViewResize
                        }
                    } : null
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

    prepareTooltip: function (data, type) {
        var dataObj = data.getData ? data.getData() : data,
            hasDetails = !Ext.isEmpty(dataObj.detail),
            getTooltip = function (title) {
                var result = title || '';

                if (hasDetails) {
                    Ext.Array.each(dataObj.detail, function (detailItem, index) {
                        if (index > 0) {
                            result += '<br>';
                        }
                        result += detailItem.displayName + ' ' + Math.round(detailItem.count / (dataObj.count || dataObj.data) * 100) + '%';
                    });
                }

                return result;
            },
            tooltip;

        switch (type) {
            case 'CHART':
                tooltip = getTooltip('<div style="font-weight: bold;' + (hasDetails ? 'padding-bottom: 5px;' : '') + '">'
                    + dataObj.name + ' ' + dataObj.percentage
                    + '</div>');
                break;
            case 'VIEW':
                if ((dataObj.key == 'statisticsValid' || dataObj.key == 'statisticsEdited') && hasDetails) {
                    tooltip = Ext.htmlEncode(getTooltip());
                }
                break;
        }

        return tooltip;
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