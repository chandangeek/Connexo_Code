Ext.define('Imt.usagepointmanagement.controller.Edit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Imt.usagepointmanagement.view.Add'
    ],

    models: [
        'Imt.usagepointmanagement.model.UsagePoint'
    ],

    stores: [
        'Imt.servicecategories.store.ServiceCategories',
        'Imt.servicecategories.store.CAS',
        'Imt.usagepointmanagement.store.UsagePointTypes',
        'Imt.usagepointmanagement.store.PhaseCodes',
        'Imt.usagepointmanagement.store.BypassStatuses'
    ],

    refs: [
        {
            ref: 'wizard',
            selector: '#add-usage-point add-usage-point-wizard'
        },
        {
            ref: 'navigationMenu',
            selector: '#add-usage-point add-usage-point-navigation'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#add-usage-point add-usage-point-wizard button[navigationBtn=true]': {
                click: me.moveTo
            },
            '#add-usage-point add-usage-point-navigation': {
                movetostep: me.moveTo
            }
        });
    },

    showWizard: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('add-usage-point', {
                itemId: 'add-usage-point',
                returnLink : router.getRoute('usagepoints').buildUrl()
            });

        me.getStore('Imt.servicecategories.store.ServiceCategories').load();
        me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load();
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getWizard().loadRecord(Ext.create('Imt.usagepointmanagement.model.UsagePoint'));
    },

    moveTo: function (button) {
        var me = this,
            wizard = me.getWizard(),
            wizardLayout = me.getWizard().getLayout(),
            currentStep = wizardLayout.getActiveItem().navigationIndex,
            direction,
            nextStep,
            changeStep = function () {
                Ext.suspendLayouts();
                me.prepareNextStep(nextStep);
                wizardLayout.setActiveItem(nextStep - 1);
                me.getNavigationMenu().moveToStep(nextStep);
                Ext.resumeLayouts(true);
            };

        if (button.action === 'step-next') {
            direction = 1;
            nextStep = currentStep + direction;
        } else {
            direction = -1;
            if (button.action === 'step-back') {
                nextStep = currentStep + direction;
            } else {
                nextStep = button;
            }
        }

        wizard.clearInvalid();
        if (direction > 0) {
            me.doRequest({
                params: {
                    step: currentStep
                },
                success: changeStep,
                failure: function (record, options) {
                    var response = options.response,
                        errors = Ext.decode(response.responseText, true);

                    if (errors && Ext.isArray(errors.errors)) {
                        wizard.markInvalid(errors.errors);
                    }
                }
            });
        } else {
            changeStep();
        }
    },

    doRequest: function (options) {
        var me = this,
            wizard = me.getWizard();

        wizard.updateRecord();
        wizard.getRecord().save(options);
    }
});