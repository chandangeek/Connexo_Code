/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointsetup.controller.MetrologyConfig', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router',
        'Imt.usagepointmanagement.controller.View'
    ],

    stores: [
        'Imt.usagepointsetup.store.Devices',
        'Imt.usagepointmanagement.store.MeterActivations'
    ],
    models: [
        'Imt.usagepointsetup.model.EffectiveMetrologyConfig'
    ],
    views: [
        'Imt.usagepointsetup.view.ActivateMeters',
        'Imt.usagepointmanagement.view.forms.fields.meteractivations.MeterActivationsGrid'
    ],
    refs: [
        {
            ref: 'metersForm',
            selector: '#usage-point-edit-meters #meter-activations-field'
        },
        {
            ref: 'page',
            selector: '#usage-point-activate-meters'
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

    showActivateMeters: function (usagePointId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View'),
            metrologyConfig = me.getModel('Imt.usagepointsetup.model.EffectiveMetrologyConfig'),
            returnLink = router.queryParams.fromLandingPage ? router.getRoute('usagepoints/view').buildUrl() : router.getRoute('usagepoints/view/metrologyconfiguration').buildUrl(),
            meterActivationsStore = me.getStore('Imt.usagepointmanagement.store.MeterActivations'),
            callback = {
                success: function (usagePointTypes, usagePoint, purposes) {
                    metrologyConfig.getProxy().setExtraParam('usagePointId', usagePointId);
                    metrologyConfig.load(undefined, {
                        success: function (mconfig) {
                            if (Ext.isEmpty(mconfig.get('meterRoles'))) {
                                window.location.replace(router.getRoute('error/notfound').buildUrl());
                            } else {
                                meterActivationsStore.getProxy().setExtraParam('usagePointId', usagePointId);
                                meterActivationsStore.load({
                                    callback: function (records, operation, success) {
                                        if (success) {
                                            var meterRoles = usagePoint.get('metrologyConfiguration_meterRoles');
                                            widget = Ext.widget('usagePointActivateMeters', {
                                                itemId: 'usage-point-activate-meters',
                                                router: router,
                                                returnLink: returnLink,
                                                usagePoint: usagePoint,
                                                meterRoles: meterRoles
                                            });
                                            app.fireEvent('changeContentEvent', widget);
                                        }
                                        mainView.setLoading(false);
                                    }
                                })
                            }
                        },
                        callback: function () {
                            mainView.setLoading(false);
                        }
                    });
                },
                failure: function () {
                    mainView.setLoading(false);
                }
            };
        mainView.setLoading(true);
        usagePointsController.loadUsagePoint(usagePointId, callback);
    },


    saveButtonClick: function (btn) {
        var me = this,
            usagePoint = btn.usagePoint,
            meterActivations = me.getMetersForm().getValue();

        _.each(meterActivations, function (meterActivation) {
            meterActivation.activationTime = undefined;
            meterActivation.meterRole.meter = meterActivation.meter;
            meterActivation.meter = {
                name: meterActivation.meterRole.meter
            }
        });

        var callback = function () {
            var page = me.getPage();

            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('metrologyconfiguration.setMeters.acknowledge', 'IMT', 'The list of meters saved'));
            if (page) {
                window.location.href = page.returnLink;
            }
        };
        var failure = function (response) {
            var errors = Ext.decode(response.responseText, true);
            if (errors && Ext.isArray(errors.errors)) {
                var errorsMap = {},
                    stageError;
                Ext.each(errors.errors, function (err) {
                    if (!Ext.isEmpty(errorsMap[err.id])) {
                        errorsMap[err.id].msg += '<br>' + err.msg
                    } else {
                        errorsMap[err.id] = {msg: err.msg}
                    }
                });
                var errMsgs = _.map(errorsMap, function (errorObject, id) {
                    return {id: id, msg: errorObject.msg}
                });
                form.getForm().markInvalid(errMsgs);
                stageError =_.find(errMsgs, function(obj) { return obj.id == 'stage' });
                if(!Ext.isEmpty(stageError)) {
                    form.down('#stageErrorLabel').show();
                    form.down('#stageErrorLabel').setText(stageError.msg);
                } else {
                    form.down('#stageErrorLabel').hide();
                }
            }
        };
        usagePoint.set('meterActivations', meterActivations);
        usagePoint.activateMeters(callback, failure);

    }

});