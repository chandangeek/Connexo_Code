Ext.define('Imt.usagepointsetup.controller.MetrologyConfig', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],

    stores: [
        'Imt.usagepointsetup.store.Devices',
        'Imt.usagepointmanagement.store.MeterActivations'
    ],
    models: [
        'Imt.usagepointsetup.model.EffectiveMetrologyConfig'
    ],
    views: [
        'Imt.usagepointsetup.view.ActivateMeters'
    ],
    refs: [
        {
            ref: 'metersForm',
            selector: '#usage-point-edit-meters #edit-form'
        }
    ],

    init: function () {
        var me = this;
        me.control({
            '#usage-point-edit-meters #save-btn': {
                click: me.saveButtonClick
            }
        });
    },

    showActivateMeters: function (mrid) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            metrologyConfig = me.getModel('Imt.usagepointsetup.model.EffectiveMetrologyConfig'),
            meterActivationsStore = me.getStore('Imt.usagepointmanagement.store.MeterActivations');
        mainView.setLoading(true);
        me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mrid, {
            success: function (usagePoint) {
                metrologyConfig.getProxy().setUrl(mrid);
                metrologyConfig.load(undefined, {
                    success: function (mconfig) {
                        meterActivationsStore.setMrid(mrid);
                        meterActivationsStore.load({
                            callback: function (records, operation, success) {
                                if (success) {
                                    var meterRoles = mconfig.get('meterRoles');
                                    Ext.Array.each(meterRoles, function (meterRole) {
                                        meterActivationsStore.each(function (mact) {
                                            if ((mact.get('meterRole') && mact.get('meterRole').id) == meterRole.id) {
                                                meterRole.value = mact.get('meter').mRID;
                                            }
                                        });
                                        meterRole.fieldLabel = meterRole.name;
                                        meterRole.name = meterRole.id;
                                    });

                                    widget = Ext.widget('usagePointActivateMeters', {
                                        router: router,
                                        usagePoint: usagePoint,
                                        meterRoles: meterRoles
                                    });
                                    app.fireEvent('changeContentEvent', widget);
                                }
                                mainView.setLoading(false);
                            }
                        })

                    },
                    callback: function () {
                        mainView.setLoading(false);
                    }
                });
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });

    },


    saveButtonClick: function (btn) {
        var me = this,
            usagePoint = btn.usagePoint,
            mRID = usagePoint.get('mRID'),
            form = me.getMetersForm(),
            meterActivations = form.getMeterActivations();
        form.getForm().clearInvalid();
        var callback = function () {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('metrologyconfiguration.setMeters.acknowledge', 'IMT', 'The list of meters saved'));
            me.getController('Uni.controller.history.Router').getRoute('usagepoints/view').forward();
        };
        var failure = function (response) {
            var errors = Ext.decode(response.responseText, true);
            if (errors && Ext.isArray(errors.errors)) {
                form.getForm().markInvalid(errors.errors);
            }
        };
        usagePoint.set('meterActivations', meterActivations);
        usagePoint.activateMeters(callback, failure);

    }

});