Ext.define('Dsh.controller.ConnectionOverview', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.BreakdownCounter',
        'Dsh.model.ConnectionCounter',
        'Dsh.model.ConnectionBreakdown',
        'Dsh.model.ConnectionOverview',
        'Dsh.model.ConnectionSummary',
        'Dsh.model.CommunicationServerInfo',
        'Dsh.model.OverviewPerCurrentStateInfo',
        'Dsh.model.OverviewPerLastResultInfo',
        'Dsh.model.TimeInfo',
        'Dsh.model.CounterInfo'
    ],
    stores: [
        'CommunicationServerInfos',
        'OverviewPerCurrentStateInfos',
        'OverviewPerLastResultInfos'
    ],
    views: [ 'Dsh.view.ConnectionOverview' ],
    refs: [
        { ref: 'heatmapchart', selector: '#breakdown' }
    ],
    init: function () {
        this.control({
            '#brakdownchartcombinecombobox': {
                change: this.combineComboChanged
            }
        });
        this.callParent(arguments);
    },
    showOverview: function () {
        this.getApplication().fireEvent('changecontentevent', Ext.widget('connection-overview'));
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