Ext.define('Dsh.controller.ConnectionOverview', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.connection.Overview'
    ],
    stores: [
        'CommunicationServerInfos',
        'Dsh.store.CombineStore',
        'Dsh.store.ConnectionResultsStore'
    ],
    views: [ 'Dsh.view.ConnectionOverview' ],
    refs: [
        { ref: 'connectionOverview', selector: '#connection-overview' },
        { ref: 'header', selector: '#header-section' },
        { ref: 'summary', selector: '#summary' },
        { ref: 'communicationServers', selector: '#communication-servers' },
        { ref: 'overview', selector: '#overview' },
        { ref: 'breakdown', selector: '#breakdown' },
        { ref: 'kpi', selector: '#connection-overview read-outs-over-time' }
    ],

    init: function () {
        this.control({
            '#connection-overview #refresh-btn': {
                click: this.loadData
            }
        });
    },

    showOverview: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('connection-overview', {router: router}));
        this.loadData();
    },

    loadData: function () {
        var me = this,
            model = me.getModel('Dsh.model.connection.Overview'),
            router = this.getController('Uni.controller.history.Router');

        model.setFilter(router.filter);
        me.getConnectionOverview().setLoading();
        me.getCommunicationServers().reload();
        model.load(null, {
                success: function (record) {
                    me.getSummary().setRecord(record.getSummary());
                    me.getOverview().bindStore(record.overviews());
                    me.getBreakdown().bindStore(record.breakdowns());
                    if (record.raw.kpi) {
                        me.getKpi().setRecord(record.getKpi());
                    }
                    me.getHeader().down('#last-updated-field').setValue('Last updated at ' + Ext.util.Format.date(new Date(), 'H:i'));
                },
                callback: function () {
                    me.getConnectionOverview().setLoading(false);
                }
            }
        );
    }
});