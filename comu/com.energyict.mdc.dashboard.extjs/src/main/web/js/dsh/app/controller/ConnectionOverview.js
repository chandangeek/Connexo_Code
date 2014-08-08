Ext.define('Dsh.controller.ConnectionOverview', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.BreakdownCounter',
        'Dsh.model.ConnectionCounter',
        'Dsh.model.ConnectionBreakdown',
        'Dsh.model.ConnectionOverview',
        'Dsh.model.ConnectionSummary'
    ],
    stores: [
        'CommunicationServerInfos',
        'OverviewPerCurrentStateInfos',
        'OverviewPerLastResultInfos'
    ],
    views: [
        'Dsh.view.widget.HSeparator',
        'Dsh.view.widget.OverviewHeader',
        'Dsh.view.widget.Summary',
        'Dsh.view.widget.CommunicationServers',
        'Dsh.view.widget.QuickLinks',
        'Dsh.view.widget.ReadOutsOverTime',
        'Dsh.view.widget.Overview',
        'Dsh.view.widget.Breakdown',
        'Dsh.view.ConnectionOverview'
    ],
    refs: [
        {ref: 'heatmapchart', selector: '#breakdown'}
    ],
    init: function () {
        this.control({
            '#brakdownchartcombinecombobox' : {
                change: this.combineComboChanged
            }
        });
        this.callParent(arguments);
    },
    showOverview: function () {
        var widget = Ext.widget('connection-overview');
        widget.add(Ext.widget('overview-header', { headerTitle: 'Connection overview' })); //TODO: localize
        widget.add(Ext.widget('summary', { title: 'Connection summary' }));
        widget.add(Ext.widget('communication-servers'));
        widget.add(Ext.widget('quicklinks', {
            data: [ //TODO: check & change
                { link: 'View all connections', href: '#/workspace/datacommunication/connections' },
                { link: 'Communication overview', href: '#/workspace/datacommunication/communication' },
                { link: 'Some link 1', href: '#' },
                { link: 'Some link 2', href: '#' },
                { link: 'Some link 3', href: '#' }
            ]
        }));
        widget.add(Ext.widget('h-sep'));
        widget.add(Ext.widget('read-outs-over-time'));
        widget.add(Ext.widget('h-sep'));
        widget.add(Ext.widget('overview'));
        widget.add(Ext.widget('h-sep'));
        widget.add(Ext.widget('breakdown'));
        this.getApplication().fireEvent('changecontentevent', widget);
        this.loadBreakdownData('comPortPool');
    },

    combineComboChanged: function (combo, newValue) {
        var me = this;
        me.setNewChartData(combo.record, newValue);
    },

    setNewChartData: function (record, alias) {
        var me = this,
            breakDowns = record.breakdowns(),
            perValue = breakDowns.findRecord('alias', alias),
            ycat = ['Success count', 'Failed count', 'Pending count'],
            chart = me.getHeatmapchart(),
            xcat = perValue.counters().collect('displayName'),
            yaxisTitles = {
                comPortPool: 'Com port pool',
                connectionType: 'Connection type'
            };
        chart.setXAxis(xcat, 'Latest result');
        chart.setYAxis(ycat, yaxisTitles[alias]);
        chart.setChartData(chart.storeToHighchartData(perValue.counters(), [
            "successCount",
            "failedCount",
            "pendingCount"
        ]))
    },

    loadBreakdownData: function (alias) {
        var me = this;
        model = me.getModel('Dsh.model.ConnectionSummary');
        model.load(0, {
                success: function (record) {
                    var breakDowns = record.breakdowns(),
                        chart = me.getHeatmapchart(),
                        combineCategories = [],
                        combineStore;
                    breakDowns.each(function (item) {
                        combineCategories.push({
                            displayValue: item.get('displayName'),
                            value: item.get('alias')
                        })
                    });
                    combineStore = Ext.create('Ext.data.Store', {
                        fields: [
                            'displayValue', 'value'
                        ],
                        data: combineCategories
                    });
                    chart.combineCombo.bindStore(combineStore);
                    chart.combineCombo.record = record;
                    me.setNewChartData(record, alias)
                }
            }
        );
    }
});