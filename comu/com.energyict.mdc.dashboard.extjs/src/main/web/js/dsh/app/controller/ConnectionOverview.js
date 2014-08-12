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
        { ref: 'breakdown', selector: '#breakdown' }
    ],

    init: function () {
        this.callParent(arguments);
    },

    showOverview: function () {
        this.getApplication().fireEvent('changecontentevent', Ext.widget('connection-overview'));
        this.loadData();
    },

    loadData: function () {
        var me = this;
        var model = me.getModel('Dsh.model.ConnectionSummary');
        model.load(null, {
                success: function (record) {
                    var breakdowns = record.breakdowns();
                    me.getBreakdown().bindStore(breakdowns);
                }
            }
        );
    }
});