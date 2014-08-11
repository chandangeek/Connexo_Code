Ext.define('Dsh.view.widget.ReadOutsOverTime', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.read-outs-over-time',
    itemId: 'read-outs-over-time',
    title: Uni.I18n.translate('overview.widget.readOutsOverTime.title', 'DSH', 'Read-outs over time'),
    title: 'Read-outs over time', // TODO: localize
    ui: 'medium',
    layout: 'fit',
    items: [
        {
            xtype: 'chart',
            store: Ext.create('Ext.data.JsonStore', {
                fields: ['name', 'data1', 'data2', 'data3', 'data4', 'data5'],
                data: [
                    {
                        'name': 'metric one',
                        'data1': 10,
                        'data2': 12,
                        'data3': 14,
                        'data4': 8,
                        'data5': 13
                    },
                    {
                        'name': 'metric two',
                        'data1': 7,
                        'data2': 8,
                        'data3': 16,
                        'data4': 10,
                        'data5': 3
                    },
                    {
                        'name': 'metric three',
                        'data1': 5,
                        'data2': 2,
                        'data3': 14,
                        'data4': 12,
                        'data5': 7
                    },
                    {
                        'name': 'metric four',
                        'data1': 2,
                        'data2': 14,
                        'data3': 6,
                        'data4': 1,
                        'data5': 23
                    },
                    {
                        'name': 'metric five',
                        'data1': 4,
                        'data2': 4,
                        'data3': 36,
                        'data4': 13,
                        'data5': 33
                    }
                ]
            }),
            axes: [
                {
                    type: 'Numeric',
                    position: 'left',
                    fields: ['data1', 'data2'],
                    label: {
                        renderer: Ext.util.Format.numberRenderer('0,0')
                    },
                    title: 'Number of communications',
                    grid: true,
                    minimum: 0
                },
                {
                    type: 'Category',
                    position: 'bottom',
                    fields: ['name']
                }
            ],
            series: [
                {
                    type: 'line',
                    highlight: {
                        size: 7,
                        radius: 7
                    },
                    axis: 'left',
                    xField: 'name',
                    yField: 'data1',
                    markerConfig: {
                        type: 'cross',
                        size: 4,
                        radius: 4,
                        'stroke-width': 0
                    }
                },
                {
                    type: 'line',
                    highlight: {
                        size: 7,
                        radius: 7
                    },
                    axis: 'left',
                    fill: true,
                    xField: 'name',
                    yField: 'data2',
                    markerConfig: {
                        type: 'circle',
                        size: 4,
                        radius: 4,
                        'stroke-width': 0
                    }
                }
            ]
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});