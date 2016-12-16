Ext.define('Imt.dashboard.controller.OperatorDashboard', {
    extend: 'Ext.app.Controller',
    models: [],
    stores: [],
    views: ['Imt.dashboard.view.OperatorDashboard'],

    refs: [
        {ref: 'dashboard', selector: '#operator-dashboard'},
        {ref: 'header', selector: 'operator-dashboard #header-section'}
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
        this.getApplication().fireEvent('changecontentevent', Ext.widget('operator-dashboard', {
            router: router,
            itemId: 'operator-dashboard'
        }));
        this.loadData();
    },

    loadData: function () {
        var me = this,
            dashboard = me.getDashboard(),
            lastUpdateField = dashboard.down('#last-updated-field'),
            myWorkList = dashboard.down('#my-work-list');

        if (myWorkList) {
            myWorkList.reload();
        }
        lastUpdateField.update(Uni.I18n.translate('general.lastUpdatedAt', 'IMT', 'Last updated at {0}', [Uni.DateTime.formatTimeShort(new Date())]));
    }
});
