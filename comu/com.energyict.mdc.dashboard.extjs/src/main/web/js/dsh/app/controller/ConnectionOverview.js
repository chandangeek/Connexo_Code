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
        { ref: 'breakdown', selector: '#breakdown' },
        { ref: 'breakdown', selector: '#breakdown' },
        { ref: 'overview', selector: '#overview' },
        { ref: 'summary', selector: '#summary' }
    ],

    showOverview: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('connection-overview', {router: router}));
        this.loadData();
    },

    loadData: function () {
        var me = this;
        var model = me.getModel('Dsh.model.connection.Overview');
        model.load(null, {
                success: function (record) {
                    me.getSummary().setRecord(record.getSummary());
                    me.getOverview().bindStore(record.overviews());
                    me.getBreakdown().bindStore(record.breakdowns());
                }
            }
        );
    }
});