Ext.define('CSMonitor.view.performance.Pools', {
    extend: 'Ext.panel.Panel',
    xtype: 'poolsPerformance',
    border: false,
    layout: {
        type : 'vbox',
        align : 'stretch'
    },

    config: {
        priorityLow: 'Low',
        priorityAvg: 'Average',
        priorityHigh: 'High'
    },

    items: [
        {
            xtype: 'component',
            margins: '0 0 0 10',
            html: '<h2>Communication port pools</h2>'
        },
        {
            xtype: 'container',
            layout: {
                type : 'vbox',
                align : 'stretch'
            },
            defaults : { margins: '2 0 2 20'},
            items: [
                {
                    xtype: 'container',
                    autoScroll: true,
                    layout: {
                        type: 'hbox',
                        align: 'middle'
                    },
                    defaults : { margins: '0 30 0 0'},
                    items: [
                        {
                            xtype: 'poolThreadChart'
                        },
                        {
                            xtype: 'poolPortChart'
                        }
                    ]
                }
            ]
        }
    ]
});