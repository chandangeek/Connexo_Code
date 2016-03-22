Ext.define('Imt.usagepointmanagement.controller.Attributes', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],

    stores: [
        'Imt.usagepointmanagement.store.UsagePointTypes',
        'Imt.usagepointmanagement.store.PhaseCodes'
    ],

    models: [

    ],

    views: [
        'Imt.usagepointmanagement.view.Attributes'
    ],

    init: function () {
        var me = this;

        me.control({
            'view-edit-form': {
                save: me.saveAttributes
            }
        });
    },

    showUsagePointAttributes: function (mRID) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            dependenciesCounter = 3,
            showPage = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    app.fireEvent('usagePointLoaded', usagePoint);
                    app.fireEvent('changecontentevent', Ext.widget('usage-point-attributes', {
                        itemId: 'usage-point-attributes',
                        router: router,
                        usagePoint: usagePoint
                    }));
                    mainView.setLoading(false);
                }
            },
            usagePoint;

        mainView.setLoading();
        me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load(showPage);
        me.getStore('Imt.usagepointmanagement.store.PhaseCodes').load(showPage);
        me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
            success: function (record) {
                usagePoint = record;
                showPage();
            },
            failure: function () {
                mainView.setLoading(false);
            }
        });
    },

    saveAttributes: function (form) {
        console.info(form);
    }
});