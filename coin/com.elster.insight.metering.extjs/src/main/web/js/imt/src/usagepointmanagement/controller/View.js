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
        'Imt.usagepointmanagement.store.measurementunits.EstimationLoad',
        'Imt.usagepointmanagement.store.Purposes',
        'Imt.usagepointmanagement.store.DataCompletion',
        'Imt.usagepointmanagement.store.Periods'
    ],

    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.usagepointmanagement.model.UsagePointFavorite'
    ],

    views: [
        'Imt.usagepointmanagement.view.Setup'
    ],

    loadUsagePoint: function (usagePointId, callback) {
        var me = this,
            app = me.getApplication(),
            failure = callback.failure,
            purposesStore = me.getStore('Imt.usagepointmanagement.store.Purposes');

        me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load(function(usagePointTypes, op, success) {
            if (success) {
                me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(usagePointId, {
                    success: function (usagePoint) {
                        app.fireEvent('usagePointLoaded', usagePoint);
                        purposesStore.getProxy().extraParams = {usagePointId: usagePointId};
                        purposesStore.load(function(purposes, op, success) {
                            if (success) {
                                usagePoint.set('purposes', purposes);
                                app.fireEvent('purposes-loaded', purposes);
                                callback.success(usagePointTypes, usagePoint, purposes);
                            } else {
                                failure();
                            }
                        });
                    },
                    failure: failure
                });
            } else {
                failure();
            }
        });
    },

    showUsagePoint: function (usagePointId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        me.loadUsagePoint(usagePointId, {
            success: function (types, usagePoint, purposes) {
                app.fireEvent('changecontentevent', Ext.widget('usage-point-management-setup', {
                    itemId: 'usage-point-management-setup',
                    meterActivationsStore: me.getStore('Imt.usagepointmanagement.store.MeterActivations'),
                    router: router,
                    usagePoint: usagePoint,
                    purposes: purposes,
                    favoriteRecord: Ext.create('Imt.usagepointmanagement.model.UsagePointFavorite', {
                        id: usagePointId,
                        parent: {
                            id: usagePointId,
                            version: usagePoint.get('version')
                        }
                    })
                }));
                mainView.setLoading(false);
            },
            failure: function () {
                mainView.setLoading(false);
            }
        });
    }
});