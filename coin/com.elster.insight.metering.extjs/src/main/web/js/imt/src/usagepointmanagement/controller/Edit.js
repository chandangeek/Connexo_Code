/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        'Imt.usagepointmanagement.view.forms.CustomPropertySetInfo',
        'Imt.usagepointmanagement.view.forms.MetrologyConfigurationWithMeters',
        'Imt.usagepointmanagement.view.forms.LifeCycleTransition'
    ],

    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.usagepointmanagement.model.technicalinfo.Electricity',
        'Imt.usagepointmanagement.model.technicalinfo.Gas',
        'Imt.usagepointmanagement.model.technicalinfo.Water',
        'Imt.usagepointmanagement.model.technicalinfo.Thermal',
        'Imt.metrologyconfiguration.model.MetrologyConfigurationWithCAS'
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
        'Imt.usagepointmanagement.store.measurementunits.Capacity',
        'Imt.usagepointmanagement.store.measurementunits.EstimationLoad',
        'Imt.metrologyconfiguration.store.LinkableMetrologyConfigurations',
        'Imt.usagepointmanagement.store.MeterActivations',
        'Imt.usagepointmanagement.store.UsagePointTransitions'
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

    serviceCategoryMap: {
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
            '#add-usage-point general-info-form techinfo-installationtimefield #installation-time-date': {
                change: me.onCreateTimeChange
            },
            '#add-usage-point metrology-configuration-with-meters-info-form #metrology-configuration-combo': {
                change: me.onMetrologyConfigurationChange
            },
            '#add-usage-point add-usage-point-wizard button[action=add]': {
                click: me.saveUsagePoint
            },
            '#add-usage-point metrology-configuration-with-meters-info-form': {
                beforeshow: me.loadAvailableMetrologyConfigurations
            },
            '#add-usage-point life-cycle-transition-info-form': {
                beforeshow: me.loadAvailableTransitions
            },
            '#add-usage-point calendar-info-form': {
                beforeshow: me.loadCalendars
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
            validationParams = {validate: true},
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
            validationParams.step = stepView.stepName;
            if (stepView.xtype === 'cps-info-form') {
                validationParams.customPropertySetId = stepView.getRecord().getId();
            }
            if(stepView.stepName === 'calendarTransitionInfo' && stepView.down('combobox[required=true]') && !stepView.down('combobox[required=true]').getValue()){
                stepView.down('combobox[required=true]').markInvalid(Uni.I18n.translate('usagepoint.add.required', 'IMT', 'This field is required'));
            } else {
                me.doRequest({
                    params: validationParams,
                    success: changeStep
                });
            }
        } else {
            changeStep();
        }
    },

    saveUsagePoint: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        me.doRequest({
            success: function (record) {
                router.getRoute('usagepoints/view').forward({usagePointId: encodeURIComponent(record.get('name'))});
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagepoint.add.successMsg', 'IMT', "Usage point added"));
            }
        });
    },

    doRequest: function (options) {
        var me = this,
            wizard = me.getWizard(),
            record,
            modelProxy;
        wizard.clearInvalid();
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

                if (errors && Ext.isArray(errors.errors) && !Ext.isEmpty(errors.errors)) {
                    wizard.markInvalid(errors.errors);
                }
            }
        }, options));
        record.phantom = false;     // restore id in the record data for normal functionality
        modelProxy.appendId = true; // restore id in the url for normal functionality
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

    onCreateTimeChange: function (field, newValue) {
        var me = this,
            wizard = me.getWizard(),
            activationTime = wizard.down('metrology-configuration-with-meters-info-form #metrology-configuration-start-date-field');
        activationTime && newValue && activationTime.setValue(newValue);
        activationTime && !newValue && activationTime.setValue(new Date());
    },

    onServiceCategoryChange: function (field, newValue) {
        var me = this,
            category = me.serviceCategoryMap[newValue],
            step2;

        if (category) {
            step2 = {
                xtype: category.form,
                title: Uni.I18n.translate('usagepoint.wizard.step2title', 'IMT', 'Step 2: Technical information'),
                itemId: 'add-usage-point-step-tech-info',
                navigationIndex: 2,
                stepName: 'techInfo',
                ui: 'large',
                isWizardStep: true,
                predefinedRecord: Ext.create(category.model),
                defaults: {
                    labelWidth: 260,
                    width: 595
                }
            };
            me.updateSteps(step2, field.findRecordByValue(newValue));
        }
    },

    onMetrologyConfigurationChange: function (combo, newValue) {
        var me = this,
            wizard = me.getWizard(),
            step = wizard.down('metrology-configuration-with-meters-info-form'),
            metrologyConfigurationInfo = step.down('#metrology-configuration-with-meters-info'),
            metrologyConfigurationStartDate = step.down('#metrology-configuration-start-date'),
            meterActivationsField = step.down('#meter-activations-field'),
            notAllMetersSpecifiedMessage = step.down('#not-all-meters-specified-message'),
            purposesField = step.down('#purposes-field');

        Ext.suspendLayouts();
        notAllMetersSpecifiedMessage.hide();
        step.down('#reset-metrology-configuration').setDisabled(!newValue);
        if (!Ext.isEmpty(newValue)) {
            me.metrologyConfigurationRequiresCalendar = newValue.requiresCalendar;
            metrologyConfigurationStartDate.show();
            metrologyConfigurationInfo.show();
            metrologyConfigurationInfo.setLoading();
            me.getModel('Imt.metrologyconfiguration.model.MetrologyConfigurationWithCAS').load(newValue.id, {
                success: function (record) {
                    var meterRoles = record.get('meterRoles');
                    Ext.suspendLayouts();
                    me.updateMetrologyConfigurationCustomAttributeSetsSteps(record);
                    notAllMetersSpecifiedMessage.setVisible(!Ext.isEmpty(meterRoles));
                    step.clearInvalid();
                    meterActivationsField.clearInvalid();
                    meterActivationsField.setMeterRoles(meterRoles, wizard.getRecord().get('installationTime'));
                    purposesField.setStore(record.metrologyContracts());
                    Ext.resumeLayouts(true);
                },
                callback: function () {
                    metrologyConfigurationInfo.setLoading(false);
                }
            });
        } else {
            me.metrologyConfigurationRequiresCalendar = false;
            metrologyConfigurationStartDate.hide();
            metrologyConfigurationInfo.hide();
            me.updateMetrologyConfigurationCustomAttributeSetsSteps();
        }
        Ext.resumeLayouts(true);
    },

    updateMetrologyConfigurationCustomAttributeSetsSteps: function (metrologyConfiguration) {
        var me = this,
            wizard = me.getWizard(),
            navigation = me.getNavigationMenu(),
            currentSteps = wizard.query('[isWizardStep=true]'),
            currentMenuItems = navigation.query('menuitem'),
            currentStepNumber = wizard.getLayout().getActiveItem().navigationIndex,
            stepsToAdd = [],
            navigationItemsToAdd = [],
            stepNumber = currentStepNumber;

        Ext.suspendLayouts();
        // remove steps depending on metrology configuration
        for (var i = currentStepNumber; i < currentSteps.length - 2; i++) {
            wizard.remove(currentSteps[i], true);
        }
        // remove menu items after current step
        for (var j = currentStepNumber; j < currentMenuItems.length; j++) {
            navigation.remove(currentMenuItems[j], true);
        }

        if (metrologyConfiguration) {
            me.addCustomPropertySetsSteps(metrologyConfiguration, stepsToAdd, navigationItemsToAdd, currentStepNumber);
        }
        me.modifyCalendarStep(wizard, navigationItemsToAdd, currentStepNumber + stepsToAdd.length + 1);
        me.modifyLifeCycleTransitionStep(wizard, navigationItemsToAdd, currentStepNumber + stepsToAdd.length + 2);
        navigation.insert(currentStepNumber, navigationItemsToAdd);
        wizard.insert(currentStepNumber, stepsToAdd);
        Ext.resumeLayouts(true);
    },

    updateSteps: function (step2, serviceCategory) {
        var me = this,
            wizard = me.getWizard(),
            navigation = me.getNavigationMenu(),
            currentSteps = wizard.query('[isWizardStep=true]'),
            currentMenuItems = navigation.query('menuitem'),
            stepsToAdd = [step2],
            navigationItemsToAdd = [];

        Ext.suspendLayouts();
        // remove steps depending on service category
        for (var i = 1; i < currentSteps.length - 2; i++) {
            wizard.remove(currentSteps[i], true);
        }
        // remove all menu items except first two
        for (var j = 2; j < currentMenuItems.length; j++) {
            navigation.remove(currentMenuItems[j], true);
        }
        me.addCustomPropertySetsSteps(serviceCategory, stepsToAdd, navigationItemsToAdd, stepsToAdd.length + 1);
        me.addLinkMetrologyConfigurationStep(stepsToAdd, navigationItemsToAdd);
        me.modifyCalendarStep(wizard, navigationItemsToAdd, stepsToAdd.length + 2);
        me.modifyLifeCycleTransitionStep(wizard, navigationItemsToAdd, stepsToAdd.length + 3);
        navigation.add(navigationItemsToAdd);
        wizard.insert(1, stepsToAdd);
        me.onCreateTimeChange();
        Ext.resumeLayouts(true);
    },

    addCustomPropertySetsSteps: function (parent, stepsToAdd, navigationItemsToAdd, stepNumber) {
        parent.customPropertySets().each(function (record) {
            stepNumber++;
            stepsToAdd.push({
                xtype: 'cps-info-form',
                itemId: 'step-cas' + record.getId(),
                title: Uni.I18n.translate('usagepoint.wizard.cpsStepTitle', 'IMT', 'Step {0}: {1}', [stepNumber, record.get('name')]),
                navigationIndex: stepNumber,
                stepName: 'casInfo',
                ui: 'large',
                isWizardStep: true,
                predefinedRecord: record
            });
            navigationItemsToAdd.push({
                text: record.get('name')
            });
        });
    },

    addLinkMetrologyConfigurationStep: function (stepsToAdd, navigationItemsToAdd) {
        var title = Uni.I18n.translate('general.linkMetrologyConfiguration', 'IMT', 'Link metrology configuration'),
            wizard = this.getWizard(),
            usagePoint = wizard.getRecord(),
            stepNumber = stepsToAdd.length + 1;

        stepNumber++;
        stepsToAdd.push({
            xtype: 'metrology-configuration-with-meters-info-form',
            itemId: 'step-metrology-configuration-with-meters',
            title: Uni.I18n.translate('usagepoint.wizard.cpsStepTitle', 'IMT', 'Step {0}: {1}', [stepNumber, title]),
            navigationIndex: stepNumber,
            stepName: 'metrologyConfigurationWithMetersInfo',
            ui: 'large',
            isWizardStep: true,
            usagePoint: usagePoint
        });
        navigationItemsToAdd.push({
            itemId: 'navigation-metrology-configuration-with-meters-info',
            text: title
        });
    },


    loadAvailableMetrologyConfigurations: function (step) {
        var me = this,
            wizard = me.getWizard(),
            usagePoint = wizard.getRecord(),
            metrologyConfigurationCombo = step.down('#metrology-configuration-combo');

        wizard.setLoading();
        Ext.Ajax.request({
            url: '/api/udr/field/metrologyconfigurations',
            method: 'POST',
            jsonData: usagePoint.getProxy().getWriter().getRecordData(usagePoint),
            success: function (response) {
                var linkableMetrologyConfigurationsStore = me.getStore('Imt.metrologyconfiguration.store.LinkableMetrologyConfigurations'),
                    availableMetrologyConfigurations = Ext.decode(response.responseText),
                    currentMetrologyConfiguration = metrologyConfigurationCombo ? metrologyConfigurationCombo.getValue() : null;

                linkableMetrologyConfigurationsStore.loadData(availableMetrologyConfigurations);
                if (!currentMetrologyConfiguration || !linkableMetrologyConfigurationsStore.getById(currentMetrologyConfiguration.id)) {
                    step.prepareStep(!Ext.isEmpty(availableMetrologyConfigurations));
                }
            },
            callback: function () {
                wizard.setLoading(false)
            }
        });
    },


    modifyCalendarStep: function(wizard, navigationItemsToAdd, stepNumber){
        var calendarTransitionStep = wizard.down('calendar-info-form'),
            title = Uni.I18n.translate('general.calendars', 'IMT', 'Calendars');

        calendarTransitionStep.setTitle(Uni.I18n.translate('usagepoint.wizard.cpsStepTitle', 'IMT', 'Step {0}: {1}', [stepNumber, title]));
        calendarTransitionStep.navigationIndex = stepNumber;

        navigationItemsToAdd.push({
            itemId: 'navigation-calendar-transition-info',
            text: title
        });
    },

    modifyLifeCycleTransitionStep: function (wizard, navigationItemsToAdd, stepNumber) {

        var lifeCycleTransitionStep = wizard.down('life-cycle-transition-info-form'),
            title = Uni.I18n.translate('general.lifeCycleTransition', 'IMT', 'Life cycle transition');

        lifeCycleTransitionStep.setTitle(Uni.I18n.translate('usagepoint.wizard.cpsStepTitle', 'IMT', 'Step {0}: {1}', [stepNumber, title]));
        lifeCycleTransitionStep.navigationIndex = stepNumber;

        navigationItemsToAdd.push({
            itemId: 'navigation-life-cycle-transition-info',
            text: title
        });
    },

    loadAvailableTransitions: function (step) {
        var me = this,
            transitionStore = me.getStore('Imt.usagepointmanagement.store.UsagePointTransitions'),
            wizard = me.getWizard(),
            usagePoint = wizard.getRecord();

        step.usagePoint = usagePoint;
        wizard.setLoading();
        Ext.Ajax.request({
            url: '/api/udr/field/transitions',
            method: 'POST',
            jsonData: usagePoint.getProxy().getWriter().getRecordData(usagePoint),
            success: function (response) {
                var availableTransitions = Ext.decode(response.responseText);

                transitionStore.loadRawData(availableTransitions);
            },
            callback: function () {
                wizard.setLoading(false)
            }
        });
    },

    loadCalendars: function(step){
        var me = this,
            wizard = me.getWizard(),
            usagePoint = wizard.getRecord(),
            calendarCategoriesStore = me.getStore('Imt.usagepointmanagement.store.CalendarCategories');
        if(me.getWizard().down('calendar-info-form').query('combobox').length === 0){
            calendarCategoriesStore.load(function(response){
                calendarCategoriesStore.each(function(category){
                    var calendarStore = Ext.create('Imt.usagepointmanagement.store.CalendarsForCategory');
                    calendarStore.filter([
                        {
                            property: 'status',
                            value: 'ACTIVE'
                        },
                        {
                            property: 'category',
                            value: category.get('name')
                        }
                    ]);
                    me.getWizard().down('calendar-info-form').add(
                        {
                            xtype: 'combobox',
                            itemId: 'cbo-calendar-time-of-use',
                            calendarType: category.get('name'),
                            fieldLabel: category.get('displayName'),
                            store: calendarStore,
                            displayField: 'name',
                            valueField: 'id',
                            queryMode: 'local',
                            required: category.get('name') === 'TOU' && me.metrologyConfigurationRequiresCalendar,
                            forceSelection: true,
                            emptyText: Uni.I18n.translate('usagepoint.add.emptyText.calendar', 'IMT', 'Select a calendar...'),
                            listeners: {
                                change: {
                                    fn: function (field, newValue) {
                                        var wizard = me.getWizard(),
                                            dateTime = wizard.down('#activation-on'),
                                            installationTime = wizard.getRecord().get('installationTime');

                                        dateTime.setValue(installationTime);
                                        dateTime.down('#date-time-field-date').setMinValue(moment(installationTime).startOf('day').toDate());
                                        me.getWizard().down('#calendar-date-field-errors').hide();
                                        field.clearInvalid();
                                        if (Ext.isEmpty(newValue)) {
                                            me.getWizard().down('#activate-calendar-'+ category.get('name')).hide();
                                            field.reset();
                                        } else {
                                            me.getWizard().down('#activate-calendar-'+ category.get('name')).show();
                                        }
                                    }
                                }
                            }
                        }
                    );
                    me.getWizard().down('calendar-info-form').add(
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('general.activateCalendar', 'IMT', 'Activate calendar'),
                            itemId: 'activate-calendar-'+ category.get('name'),
                            required: true,
                            layout: 'hbox',
                            hidden: true,
                            items: [
                                {
                                    // itemId: 'activate-calendar',
                                    xtype: 'radiogroup',
                                    name: 'activateCalendar'+ category.get('name'),
                                    columns: 1,
                                    vertical: true,
                                    width: 100,
                                    defaults: {
                                        name: 'activateCalendar'+ category.get('name'),
                                        style: {
                                            overflowX: 'visible',
                                            whiteSpace: 'nowrap'
                                        }
                                    },
                                    listeners: {
                                        change: function (field, newValue, oldValue) {
                                            this.up().down('fieldcontainer[name=activationDateValues]').setDisabled(newValue['activateCalendar' + category.get('name')] !== 'on-date-activation');
                                        }
                                    },
                                    items: [
                                        {
                                            //    itemId: 'immediate-activation-date',
                                            boxLabel: Uni.I18n.translate('general.immediately', 'IMT', 'Immediately'),
                                            inputValue: 'immediate-activation',
                                            checked: true
                                        },
                                        {
                                            //    itemId: 'on-activation-date',
                                            boxLabel: Uni.I18n.translate('general.on', 'IMT', 'On'),
                                            margin: '7 0 0 0',
                                            inputValue: 'on-date-activation'
                                        }
                                    ]
                                },
                                {
                                    // itemId: 'activation-date-values',
                                    xtype: 'fieldcontainer',
                                    name: 'activationDateValues',
                                    itemId: 'activationDateValues-'+ category.get('name'),
                                    required: true,
                                    margin: '30 0 10 -40',
                                    disabled: true,
                                    layout: 'hbox',
                                    items: [
                                        {
                                            xtype: 'date-time',
                                            itemId: 'activation-on',
                                            layout: 'hbox',
                                            name: 'activationOn',
                                            //    disabled: true,
                                            dateConfig: {
                                                allowBlank: true,
                                                editable: true,
                                                format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                            },
                                            hoursConfig: {
                                                width: 55,
                                                value: new Date().getHours()
                                            },
                                            minutesConfig: {
                                                width: 55,
                                                value: new Date().getMinutes()
                                            }
                                        }
                                    ]
                                }
                            ]
                        }
                    );

                    me.getWizard().down('calendar-info-form').add({
                        xtype: 'component',
                        itemId: 'calendar-date-field-errors',
                        cls: 'x-form-invalid-under',
                        style: {
                            'white-space': 'normal',
                            'padding': '0px 0px 10px 335px',
                            'margin': '-35px 0px 0px 0px'
                        },
                        hidden: true
                    });

                })
            });
        }
    }
});