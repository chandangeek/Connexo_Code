Ext.define('Dsh.controller.CommunicationOverview', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.communication.Overview'
    ],
    stores: [
        'CommunicationServerInfos'
    ],
    views: [
        'Dsh.view.CommunicationOverview'
    ],
    refs: [
        { ref: 'communicationOverview', selector: '#communication-overview' },
        { ref: 'header', selector: '#header-section' },
        { ref: 'summary', selector: '#summary' },
        { ref: 'communicationServers', selector: '#communication-servers' },
        { ref: 'overview', selector: '#overview' },
        { ref: 'breakdown', selector: '#breakdown' },
        { ref: 'kpi', selector: '#communication-overview read-outs-over-time' }
    ],

    init: function () {
        this.control({
            '#communication-overview #refresh-btn': {
                click: this.loadData
            }
        });
    },

    showOverview: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('communication-overview', {router: router}));
        this.loadData();
    },

    loadData: function () {
        var me = this,
            model = me.getModel('Dsh.model.communication.Overview'),
            router = this.getController('Uni.controller.history.Router');

        model.setFilter(router.filter);
        me.getCommunicationOverview().setLoading();
        me.getCommunicationServers().reload();
        model.load(null, {
                success: function (record) {
                    me.getSummary().setRecord(record.getSummary());
                    me.getOverview().bindStore(record.overviews());
                    me.getBreakdown().bindStore(record.breakdowns());
                    me.getKpi().bindStore(record.kpi());
                    me.getHeader().down('#last-updated-field').setValue('Last updated at ' + Ext.util.Format.date(new Date(), 'H:i'));
                },
                callback: function () {
                    me.getCommunicationOverview().setLoading(false);
                }
            }
        );
    }
});