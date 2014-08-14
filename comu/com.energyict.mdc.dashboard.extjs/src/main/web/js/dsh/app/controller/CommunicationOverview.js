Ext.define('Dsh.controller.CommunicationOverview', {
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
        'Dsh.model.CounterInfo'
    ],
    stores: [
        'CommunicationServerInfos',
        'OverviewPerCurrentStateInfos',
        'OverviewPerLastResultInfos'
    ],
    views: [ 'Dsh.view.CommunicationOverview' ],
    refs: [
        { ref: 'breakdown', selector: '#breakdown' },
        { ref: 'summary', selector: '#summary' }
    ],

    init: function () {
        this.callParent(arguments);
    },

    showOverview: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('communication-overview', {router: router}));
        this.loadData();
    },

    loadData: function () {
        var me = this;
        var model = me.getModel('Dsh.model.CommunicationOverview');
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