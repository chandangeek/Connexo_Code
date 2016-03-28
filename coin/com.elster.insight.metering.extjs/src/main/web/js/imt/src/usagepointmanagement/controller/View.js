Ext.define('Imt.usagepointmanagement.controller.View', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    stores: [
        'Imt.usagepointmanagement.store.MeterActivations',
        'Imt.customattributesonvaluesobjects.store.ServiceCategoryCustomAttributeSets',
        'Imt.customattributesonvaluesobjects.store.MetrologyConfigurationCustomAttributeSets',
        'Imt.metrologyconfiguration.store.MetrologyConfiguration',
        'Imt.usagepointmanagement.store.ServiceCategories',
        'Imt.usagepointmanagement.store.UsagePointTypes',
        'Imt.usagepointmanagement.store.PhaseCodes',
        'Imt.usagepointmanagement.store.BypassStatuses',
        'Imt.usagepointmanagement.store.measurementunits.Voltage',
        'Imt.usagepointmanagement.store.measurementunits.Amperage',
        'Imt.usagepointmanagement.store.measurementunits.Power',
        'Imt.usagepointmanagement.store.measurementunits.Volume',
        'Imt.usagepointmanagement.store.measurementunits.Pressure',
        'Imt.usagepointmanagement.store.measurementunits.Capacity',
        'Imt.usagepointmanagement.store.measurementunits.EstimationLoad'
    ],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint'
    ],

    views: [
        'Imt.usagepointmanagement.view.Setup'
    ],

    showUsagePoint: function (mRID) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            dependenciesCounter = 2,
            showPage = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    app.fireEvent('usagePointLoaded', usagePoint);
                    app.fireEvent('changecontentevent', Ext.widget('usage-point-management-setup', {
                        itemId: 'usage-point-management-setup',
                        router: router,
                        usagePoint: usagePoint
                    }));
                    mainView.setLoading(false);
                }
            },
            usagePoint;

        mainView.setLoading();
        me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load(showPage);
        me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
            success: function (record) {
                usagePoint = record;
                showPage();
            },
            failure: function () {
                mainView.setLoading(false);
            }
        });
    }
});

