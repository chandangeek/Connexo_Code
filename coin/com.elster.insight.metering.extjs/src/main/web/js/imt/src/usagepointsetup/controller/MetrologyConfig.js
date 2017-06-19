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
        'Imt.usagepointmanagement.model.UsagePoint'
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
            usagePointsController = me.getModel('Imt.usagepointmanagement.model.UsagePoint'),
            returnLink = router.queryParams.fromLandingPage ? router.getRoute('usagepoints/view').buildUrl() : router.getRoute('usagepoints/view/metrologyconfiguration').buildUrl(),
            callback = {
                success: function (usagePoint) {
                    var meterRoles = usagePoint.get('meterRoles');

                    widget = Ext.widget('usagePointActivateMeters', {
                        itemId: 'usage-point-activate-meters',
                        router: router,
                        returnLink: returnLink,
                        usagePoint: usagePoint,
                        meterRoles: meterRoles
                    });
                    app.fireEvent('changeContentEvent', widget);
                    app.fireEvent('usagePointLoaded', usagePoint);
                    mainView.setLoading(false);
                },
                failure: function () {
                    mainView.setLoading(false);
                }
            };
        mainView.setLoading(true);
        usagePointsController.load(usagePointId, callback);
    },


    saveButtonClick: function (btn) {
        var me = this,
            page = me.getPage(),
            usagePoint = btn.usagePoint,
            meterActivations = me.getMetersForm().getValue();

        meterActivations = _.filter(meterActivations, function (meterActivation) {
            return meterActivation['isAddRow'] != true;
        });

        _.each(meterActivations, function (meterActivation) {
            delete meterActivation['isAddRow'];
            delete meterActivation['meterRoleId'];

            if (Ext.isString(meterActivation.meterRole)) {
                var meterRole = meterActivation.meterRole;
                meterActivation.meterRole = {};
                meterActivation.meterRole.id = meterRole;
            }
            meterActivation.meterRole.meter = meterActivation.meter;
            if (meterActivation.meter) {
                meterActivation.meter = {
                    name: meterActivation.meterRole.meter
                };
            } else {
                meterActivation.meter = null;
            }
            meterActivation.meterRole.activationTime = meterActivation.activationTime;
            meterActivation.activationTime = undefined;
        });

        var callback = function () {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('metrologyconfiguration.setMeters.acknowledge', 'IMT', 'The list of meters saved'));
            if (page) {
                window.location.href = page.returnLink;
            }
        };
        var failure = function (response) {
            var errors = Ext.decode(response.responseText, true),
                form = me.getMetersForm();
            form.clearInvalid();
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
                var errMsgs = [],
                    err = _.map(errorsMap, function (errorObject, id) {
                            errMsgs.push(' '+errorObject.msg);
                    return {id: id, msg: errorObject.msg}
                });
                form.markInvalid(errMsgs);
                stageError = _.find(err, function (obj) {
                    return obj.id == 'stage'
                });
                if (!Ext.isEmpty(stageError)) {
                    page.down('#usage-point-edit-meters #stageErrorLabel').show();
                    page.down('#usage-point-edit-meters #stageErrorLabel').setText(stageError.msg);
                } else {
                    page.down('#usage-point-edit-meters #stageErrorLabel').hide();
                }
            }
        };
        usagePoint.set('meterActivations', meterActivations);
        usagePoint.activateMeters(callback, failure);
    }

});