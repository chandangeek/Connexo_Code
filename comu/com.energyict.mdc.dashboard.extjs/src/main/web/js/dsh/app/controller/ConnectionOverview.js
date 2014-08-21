Ext.define('Dsh.controller.ConnectionOverview', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.BreakdownCounter',
        'Dsh.model.ConnectionCounter',
        'Dsh.model.ConnectionBreakdown',
        'Dsh.model.ConnectionOverview',
        'Dsh.model.ConnectionSummary',
        'Dsh.model.ConnectionSummaryData',
        'Dsh.model.CommunicationOverview',
        'Dsh.model.CommunicationServerInfo',
        'Dsh.model.OverviewPerCurrentStateInfo',
        'Dsh.model.OverviewPerLastResultInfo',
        'Dsh.model.TimeInfo',
        'Dsh.model.CounterInfo',
        'Dsh.model.Combine'
    ],
    stores: [
        'CommunicationServerInfos',
        'OverviewPerCurrentStateInfos',
        'OverviewPerLastResultInfos',
        'Dsh.store.CombineStore'
    ],
    views: [ 'Dsh.view.ConnectionOverview' ],
    refs: [
        { ref: 'breakdown', selector: '#breakdown' },
        { ref: 'summary', selector: '#summary' }
    ],

    init: function () {
        this.callParent(arguments);
    },

    showOverview: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('connection-overview', {router: router}));
        this.loadData();
    },

    loadData: function () {
        var me = this;
        var model = me.getModel('Dsh.model.ConnectionSummary');
        model.load(null, {
                success: function (record) {
                    me.getSummary().setRecord(record.getSummary());
                    var breakdowns = record.breakdowns();
                    me.getBreakdown().bindStore(breakdowns);
                }
            }
        );
    }
});