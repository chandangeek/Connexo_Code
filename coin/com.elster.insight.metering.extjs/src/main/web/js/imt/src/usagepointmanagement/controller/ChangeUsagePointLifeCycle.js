/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.controller.ChangeUsagePointLifeCycle', {
    extend: 'Ext.app.Controller',

    views: [
        'Imt.usagepointmanagement.view.changeusagepointlifecycle.Browse'
    ],

    stores: [
        'Imt.usagepointmanagement.store.UsagePointLifeCycles'
    ],

    refs: [
        {
            ref: 'wizard',
            selector: 'change-usage-point-life-cycle-browse change-usage-point-life-cycle-wizard'
        },
        {
            ref: 'navigation',
            selector: 'change-usage-point-life-cycle-browse usagepointChangeLifeCycleWizardNavigation'
        }
    ],

    usagePoint: null,

    init: function () {
        this.control({
            'change-usage-point-life-cycle-browse change-usage-point-life-cycle-wizard button[action=step-next]': {
                click: this.moveTo
            }
        });
    },

    showChangeUsagePointLifeCycle: function (usagePointId) {
        var me = this,
            usagePointModel = me.getModel('Imt.usagepointmanagement.model.UsagePoint'),
            router = me.getController('Uni.controller.history.Router'),
            view = Ext.widget('change-usage-point-life-cycle-browse', {
                router: router
            });

        if (router.queryParams.previousRoute) {
            setTimeout(function () { // make redirect after executing this method
                Uni.util.History.setParsePath(false);
                Uni.util.History.suspendEventsForNextCall();
                window.location.replace(router.getRoute().buildUrl(router.arguments, null));
            }, 0);
        }
        me.getApplication().fireEvent('changecontentevent', view);
        view.setLoading();
        usagePointModel.load(usagePointId, {
            success: function (usagePoint) {
                me.usagePoint = usagePoint;
                me.getApplication().fireEvent('loadUsagePointType', usagePoint);
                view.down('#change-usage-point-life-cycle-combo').getStore().load({
                    callback: function () {
                        view.down('#change-usage-point-life-cycle-combo').getStore().filterBy(function (rec) {
                            return rec.get('id') != usagePoint.get('lifeCycle').id;
                        });
                        view.setLoading(false);
                    }
                });
            }
        });
    },

    moveTo: function () {
        var me = this,
            wizard = me.getWizard(),
            router = me.getController('Uni.controller.history.Router'),
            nextBtn = wizard.down('#change-usage-point-life-cycle-next'),
            backBtn = wizard.down('#change-usage-point-life-cycle-step-back'),
            cancelBtn = wizard.down('#change-usage-point-life-cycle-cancel'),
            finishBtn = wizard.down('#change-usage-point-life-cycle-finish'),
            lifeCycleCombo = wizard.down('#change-usage-point-life-cycle-combo');

        lifeCycleCombo.clearInvalid();
        wizard.down('#form-errors').hide();
        wizard.setLoading();
        Ext.Ajax.request({
            url: '/api/udr/usagepoints/{id}/usagepointlifecycle'.replace('{id}', router.arguments.usagePointId),
            method: 'PUT',
            backUrl: cancelBtn.href,
            jsonData: lifeCycleCombo.getDisplayValue(),
            callback: function (options, success, response) {
                wizard.setLoading(false);
                if (response.status === 409) {
                    return
                }
                var result = Ext.decode(response.responseText, true);
                if (lifeCycleCombo.getValue()) {
                    nextBtn.hide();
                    backBtn.hide();
                    cancelBtn.hide();
                    finishBtn.show();
                    wizard.getLayout().setActiveItem(1);
                    me.getNavigation().moveToStep(2);
                    wizard.down('change-usage-point-life-cycle-step2').setResultMessage(result, success);
                } else {
                    lifeCycleCombo.markInvalid(result.errors[0].msg);
                    wizard.down('#form-errors').show();
                }
            }
        });
    }
});
