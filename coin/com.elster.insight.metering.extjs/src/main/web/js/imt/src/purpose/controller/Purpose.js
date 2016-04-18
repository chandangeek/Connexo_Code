Ext.define('Imt.purpose.controller.Purpose', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router',
        'Imt.purpose.view.Outputs',
        'Imt.purpose.store.Outputs'
    ],

    stores: [
        'Imt.purpose.store.Outputs'
    ],

    models: [
        'Imt.purpose.model.Output'
    ],

    views: [
        'Imt.purpose.view.Outputs'
    ],

    loadOutputs: function (mRID, purposeId, callback) {
        var me = this,
            outputsStore = me.getStore('Imt.purpose.store.Outputs');

        outputsStore.getProxy().extraParams = {mRID: mRID, purposeId: purposeId};
        outputsStore.load(callback);
    },

    showOutputs: function (mRID, purposeId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];


        mainView.setLoading();
        usagePointsController.loadUsagePoint(mRID, {
            success: function (types, usagePoint, purposes) {
                me.loadOutputs(mRID, purposeId, function() {
                    var purpose = _.find(purposes, function(p){return p.getId() == purposeId});
                    app.fireEvent('changecontentevent', Ext.widget('purpose-outputs', {
                        itemId: 'purpose-outputs',
                        router: router,
                        usagePoint: usagePoint,
                        purposes: purposes,
                        purpose: purpose
                    }));
                    mainView.setLoading(false);
                });
            },
            failure: function () {
                mainView.setLoading(false);
            }
        });
    }
});