Ext.define('Imt.usagepointmanagement.controller.Edit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Imt.usagepointmanagement.view.Add',
        'Imt.usagepointmanagement.view.forms.ElectricityInfo',
        'Imt.usagepointmanagement.view.forms.GasInfo',
        'Imt.usagepointmanagement.view.forms.WaterInfo',
        'Imt.usagepointmanagement.view.forms.ThermalInfo',
        'Imt.usagepointmanagement.view.forms.CustomPropertySetInfo'
    ],

    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.usagepointmanagement.model.technicalinfo.Electricity',
        'Imt.usagepointmanagement.model.technicalinfo.Gas',
        'Imt.usagepointmanagement.model.technicalinfo.Water',
        'Imt.usagepointmanagement.model.technicalinfo.Thermal'
    ],

    stores: [
        'Imt.usagepointmanagement.store.ServiceCategories',
        'Imt.usagepointmanagement.store.UsagePointTypes',
        'Imt.usagepointmanagement.store.PhaseCodes',
        'Imt.usagepointmanagement.store.BypassStatuses',
        'Imt.usagepointmanagement.store.measurementunits.Voltage',
        'Imt.usagepointmanagement.store.measurementunits.Amperage',
        'Imt.usagepointmanagement.store.measurementunits.Power',
        'Imt.usagepointmanagement.store.measurementunits.Volume',
        'Imt.usagepointmanagement.store.measurementunits.Pressure',
        'Imt.usagepointmanagement.store.measurementunits.PressureExtended',
        'Imt.usagepointmanagement.store.measurementunits.Capacity'
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

    techInfoMap: {
        'ELECTRICITY': {
            model: 'Imt.usagepointmanagement.model.technicalinfo.Electricity',
            form: 'electricity-info-form'
        },
        'GAS': {
            model: 'Imt.usagepointmanagement.model.technicalinfo.Gas',
            form: 'gas-info-form'
        },
        'WATER': {
            model: 'Imt.usagepointmanagement.model.technicalinfo.Water',
            form: 'water-info-form'
        },
        'HEAT': {
            model: 'Imt.usagepointmanagement.model.technicalinfo.Thermal',
            form: 'thermal-info-form'
        }
    },

    init: function () {
        var me = this;

        me.control({
            '#add-usage-point add-usage-point-wizard button[navigationBtn=true]': {
                click: me.moveTo
            },
            '#add-usage-point add-usage-point-navigation': {
                movetostep: me.moveTo
            },
            '#add-usage-point general-info-form #up-service-category-combo': {
                change: me.onServiceCategoryChange
            },
            '#add-usage-point add-usage-point-wizard button[action=add]': {
                click: me.saveUsagePoint
            }
        });
    },

    showWizard: function () {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            router = me.getController('Uni.controller.history.Router');

        mainView.setLoading();
        me.getStore('Imt.usagepointmanagement.store.ServiceCategories').load(function (records) {
            var isPossibleAdd = records && records.length;

            me.getApplication().fireEvent('changecontentevent', Ext.widget('add-usage-point', {
                itemId: 'add-usage-point',
                returnLink: router.getRoute('usagepoints').buildUrl(),
                isPossibleAdd: isPossibleAdd
            }));
            if (isPossibleAdd) {
                me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load();
                me.getStore('Imt.usagepointmanagement.store.PhaseCodes').load();
                me.getStore('Imt.usagepointmanagement.store.BypassStatuses').load();
                me.getWizard().loadRecord(Ext.create('Imt.usagepointmanagement.model.UsagePoint'));
            }
            mainView.setLoading(false);
        });
    },

    moveTo: function (button) {
        var me = this,
            wizard = me.getWizard(),
            wizardLayout = wizard.getLayout(),
            stepView = wizardLayout.getActiveItem(),
            currentStep = stepView.navigationIndex,
            validationParams = {},
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
            if (stepView.xtype === 'cps-info-form') {
                validationParams.customPropertySetId = stepView.getRecord().getId();
            } else {
                validationParams.step = currentStep;
            }
            me.doRequest({
                params: validationParams,
                success: changeStep
            });
        } else {
            changeStep();
        }
    },

    saveUsagePoint: function () {
        var me = this,
            wizard = me.getWizard();

        me.doRequest({
            success: function (record) {
                window.location.href = wizard.returnLink;
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagepoint.add.successMsg', 'MDC', "Usage point '{0}' added.", record.get('mRID'), false));
            }
        });
    },

    doRequest: function (options) {
        var me = this,
            wizard = me.getWizard(),
            record,
            modelProxy;

        wizard.updateRecord();
        wizard.setLoading();
        record = wizard.getRecord();
        modelProxy = record.getProxy();
        record.phantom = true;       // force 'POST' method for request otherwise 'PUT' will be performed
        modelProxy.appendId = false; // remove 'id' part from request url
        record.save(Ext.apply({
            callback: function () {
                wizard.setLoading(false);
            },
            failure: function (record, options) {
                var response = options.response,
                    errors = Ext.decode(response.responseText, true);

                if (errors && Ext.isArray(errors.errors)) {
                    wizard.markInvalid(errors.errors);
                }
            }
        }, options));
        modelProxy.appendId = true; // restore id for normal functionality
    },

    prepareNextStep: function (stepNumber) {
        var me = this,
            wizard = me.getWizard(),
            isLastStep = wizard.query('[isWizardStep=true]').length == stepNumber,
            buttons = wizard.getDockedComponent('usage-point-wizard-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            addBtn = buttons.down('[action=add]'),
            backBtn = buttons.down('[action=step-back]');

        if (stepNumber === 1) {
            nextBtn.show();
            backBtn.disable();
            addBtn.hide();
        } else {
            nextBtn.setVisible(!isLastStep);
            addBtn.setVisible(isLastStep);
            backBtn.enable();
        }
    },

    onServiceCategoryChange: function (field, newValue) {
        var me = this,
            category = me.techInfoMap[newValue],
            form;

        if (category) {
            form = Ext.widget(category.form, {
                title: Uni.I18n.translate('usagepoint.wizard.step2title', 'IMT', 'Step 2: Technical information'),
                itemId: 'add-usage-point-step2',
                navigationIndex: 2,
                ui: 'large',
                isWizardStep: true
            });

            form.loadRecord(Ext.create(category.model));
            me.updateSteps(form, field.findRecordByValue(newValue));
        }
    },

    updateSteps: function (step2, serviceCategory) {
        var me = this,
            wizard = me.getWizard(),
            navigation = me.getNavigationMenu(),
            currentSteps = wizard.query('[isWizardStep=true]'),
            currentMenuItems = navigation.query('menuitem'),
            stepNumber = 2;

        Ext.suspendLayouts();
        // remove all steps except first
        for (var i = 1; i < currentSteps.length; i++) {
            wizard.remove(currentSteps[i], true);
        }
        // remove all menu items except first two
        for (var j = 2; j < currentMenuItems.length; j++) {
            navigation.remove(currentMenuItems[j], true);
        }
        wizard.add(step2);
        serviceCategory.customPropertySets().each(function (record) {
            stepNumber++;
            var cpsForm = Ext.widget('cps-info-form', {
                title: record.get('name'),
                navigationIndex: stepNumber,
                itemId: 'add-usage-point-step' + stepNumber,
                ui: 'large',
                isWizardStep: true
            });

            cpsForm.loadRecord(record);
            wizard.add(cpsForm);
            navigation.add({
                text: record.get('name')
            });
        });
        Ext.resumeLayouts(true);
    }
});