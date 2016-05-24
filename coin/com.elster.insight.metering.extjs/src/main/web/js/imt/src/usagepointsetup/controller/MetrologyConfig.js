Ext.define('Imt.usagepointsetup.controller.MetrologyConfig', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],

    stores: [
        'Imt.usagepointsetup.store.Devices'
    ],
    models: [
        'Imt.usagepointsetup.model.EffectiveMetrologyConfig',
        'Imt.usagepointsetup.model.UsagePoint'
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
                click: this.saveButtonClick
            }
        });
    },

    showActivateMeters: function (mrid) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            metrologyConfig = me.getModel('Imt.usagepointsetup.model.EffectiveMetrologyConfig'),
            usagePointModel = Ext.create('Imt.usagepointsetup.model.UsagePoint');
        mainView.setLoading(true);

        usagePointModel.getProxy().url = usagePointModel.getProxy().url = usagePointModel.getProxy().urlTpl.replace('{0}', mrid);
        me.getModel('Imt.usagepointsetup.model.UsagePoint').load(undefined, {
            success: function (usagePoint) {
                metrologyConfig.getProxy().setUrl(mrid);
                metrologyConfig.load(undefined, {
                    success: function (record) {
                        var meterRoles = record.get('meterRoles');
                        Ext.Array.each(meterRoles, function (meterRole) {
                            Ext.Array.each(usagePoint.get('meterActivations'), function (mact) {
                                if (mact.meterRole && mact.meterRole.id == meterRole.id) {
                                    meterRole.value = mact.meter ? mact.meter.mRID : '';
                                    meterRole.meterData = mact.meter;
                                }
                            });
                            meterRole.fieldLabel = meterRole.name;
                            meterRole.name = meterRole.id
                        });

                        widget = Ext.widget('usagePointActivateMeters', {
                            router: router,
                            usagePoint: usagePoint,
                            meterRoles: meterRoles
                        });

                        app.fireEvent('changeContentEvent', widget);
                        mainView.setLoading(false);
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
            form = this.getMetersForm(),
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
        var activate = function () {
            usagePoint.set('meterActivations', meterActivations);
            usagePoint.activateMeters(callback, failure);
        };
        var uniqMeterActivations = _.uniq(meterActivations, function (ma) {
                return ma.meter ? ma.meter.mRID : true;
            }),
            confirm = false,
            confirmationMsg = me.makeConfirmationMessage(uniqMeterActivations, usagePoint);

        if (confirm) {
            Ext.create('Uni.view.window.Confirmation', {
                htmlEncode: false,
                confirmText: Uni.I18n.translate('general.save', 'IMT', 'Save')
            }).show({
                msg: confirmationMsg,
                title: Uni.I18n.translate('metrologyconfiguration.setMeters.confirmation.title', 'IMT', 'Save meters?'),
                fn: activate
            })
        } else {
            activate();
        }

    },

    makeConfirmationMessage: function (meterActivations, usagePoint) {

        var msg = '',
            msgTpl = Uni.I18n.translate('metrologyconfiguration.setMeters.confirmation.msg', 'IMT', "Meter '{X}' is already linked to usage point '{Y}' as '{Z}' meter.");
        Ext.each(meterActivations, function (meterAct) {
            meterAct.meter && meterAct.meter.meterActivations && Ext.each(meterAct.meter.meterActivations, function (mact) {
                if (mact.usagePoint && !Ext.isEmpty(mact.usagePoint.mRID) && (mact.usagePoint.mRID != usagePoint.get('mRID'))) {
                    confirm = true;
                    msg += msgTpl
                            .replace('{X}', meterAct.meter.mRID)
                            .replace('{Y}', mact.usagePoint.mRID)
                            .replace('{Z}', mact.meterRole.name) + '<br>';
                }
            });
        });
        return msg;
    }

});