Ext.define('Dsh.controller.OperatorDashboard', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.connection.Overview'
    ],
    stores: [
        'CommunicationServerInfos',
        'Dsh.store.CombineStore',
        'Dsh.store.ConnectionResultsStore'
    ],
    views: [ 'Dsh.view.OperatorDashboard' ],

    refs: [
        { ref: 'dashboard', selector: '#operator-dashboard' },
        { ref: 'header', selector: 'operator-dashboard #header-section' },
        { ref: 'connectionSummary', selector: ' operator-dashboard #connection-summary' },
        { ref: 'communicationSummary', selector: ' operator-dashboard #communication-summary' },
        { ref: 'summary', selector: ' operator-dashboard #summary' },
        { ref: 'communicationServers', selector: 'operator-dashboard #communication-servers' }
    ],

    init: function () {
        this.control({
            '#operator-dashboard #refresh-btn': {
                click: this.loadData
            }
        });
    },

    showOverview: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('operator-dashboard', {router: router}));
        this.loadData();
    },

    loadData: function () {
        var me = this,
            connectionModel = me.getModel('Dsh.model.connection.Overview'),
            communicationModel = me.getModel('Dsh.model.communication.Overview'),
            router = this.getController('Uni.controller.history.Router');

        connectionModel.setFilter(router.filter);
        me.getDashboard().setLoading();
        me.getCommunicationServers().reload();

        connectionModel.load(null, {
                success: function (connections) {
                    me.getConnectionSummary().setRecord(connections.getSummary());
                    me.getHeader().down('#last-updated-field').setValue('Last updated at ' + Ext.util.Format.date(new Date(), 'H:i'));

                    communicationModel.load(null, {
                        success: function (communications) {
                            me.getCommunicationSummary().setRecord(communications.getSummary());
                            me.getHeader().down('#last-updated-field').setValue('Last updated at ' + Ext.util.Format.date(new Date(), 'H:i'));
                        },
                        callback: function () {
                            me.getDashboard().setLoading(false);
                        }
                    });
                }
            }
        );
    }
});
