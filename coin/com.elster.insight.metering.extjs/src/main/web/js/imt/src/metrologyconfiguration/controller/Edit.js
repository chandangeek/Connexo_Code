Ext.define('Imt.metrologyconfiguration.controller.Edit', {
    extend: 'Ext.app.Controller',

    requires: [
       'Uni.controller.history.Router',
       'Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetSetup',
       'Imt.metrologyconfiguration.view.MetrologyConfigurationEdit',
       'Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetEdit',
       //'Imt.metrologyconfiguration.store.MetrologyConfiguration',
       'Imt.metrologyconfiguration.store.LinkedValidationRulesSet',
       'Imt.metrologyconfiguration.store.LinkableValidationRulesSet',
        'Imt.metrologyconfiguration.view.DefineMetrologyConfiguration'
    ],
    models: [
             'Imt.metrologyconfiguration.model.MetrologyConfiguration',
             'Imt.metrologyconfiguration.model.LinkedValidationRulesSet',
             'Imt.metrologyconfiguration.model.LinkableValidationRulesSet',
             'Imt.metrologyconfiguration.model.LinkableMetrologyConfiguration'
    ],
    stores: [
             'Imt.metrologyconfiguration.store.LinkedValidationRulesSet',
             'Imt.metrologyconfiguration.store.LinkableValidationRulesSet',
             'Imt.metrologyconfiguration.store.LinkableMetrologyConfigurations'
    ],
    refs: [
           {ref: 'metrologyConfigurationEditPage', selector: 'metrologyConfigurationEdit'},
           {ref: 'metrologyConfigurationNameLabel', selector: '#metrology-configuration-name-label'},
           {ref: 'metrologyConfigValRulesSetEditForm', selector: '#metrologyConfigValRulesSetEditForm'},
           {ref: 'metrologyConfigValRulesSetSetup', selector: '#metrologyConfigValRulesSetSetup'},
           {ref: 'overviewLink', selector: '#metrology-configuration-overview-link'},
           {ref: 'metrologyConfigValRulesSetEditPanel', selector: '#metrologyConfigValRulesSetEditPanel'},
           {ref: 'metrologyConfigValRulesSetEditPage', selector: 'metrologyConfigValRulesSetEdit'},
           {ref: 'wizard', selector: 'define-metrology-configuration define-metrology-configuration-wizard'},
           {ref: 'navigationMenu', selector: 'define-metrology-configuration navigation-menu'},
    ],

    returnLink: null,

    init: function () {
        this.control({
            'metrologyConfigurationEdit button[action=saveModel]': {
                click: this.saveMetrologyConfiguration
            },
            'metrologyConfigurationEdit button[action=cancelButton]': {
                click: this.saveMetrologyConfiguration
            },
            'metrologyConfigValRulesSetEdit button[action=addRulesSet]': {
                click: this.addRulesSetsToMetrologyConfiguration
            },
            'metrologyConfigValRulesSetEdit button[action=removeRulesSet]': {
                click: this.removeRulesSetsFromMetrologyConfiguration
            },
            'metrologyConfigValRulesSetEdit button[action=saveModel]': {
                click: this.saveRulesSetsToMetrologyConfiguration
            },
            'metrologyConfigValRulesSetEdit button[action=cancelButton]': {
                click: this.saveRulesSetsToMetrologyConfiguration
            },
            'define-metrology-configuration define-metrology-configuration-wizard button[navigationBtn=true]': {
                click: this.moveTo
            },
            'define-metrology-configuration define-metrology-configuration-wizard button[action=add]': {
                click: this.linkUsagePointToMetrologyConfiguration
            },
            'define-metrology-configuration navigation-menu': {
                movetostep: this.moveTo
            },
            'define-metrology-configuration-wizard #metrology-configuration-combo': {
                change: this.onMetrologyConfigurationChange
            },
        });
    },
    createMetrologyConfiguration: function() {
    	var me = this,
    	    widget = Ext.widget('metrologyConfigurationEdit');
    	me.getApplication().fireEvent('changecontentevent', widget);
    },
    editMetrologyConfiguration: function(mcid) {
    	var me = this,
    		router = me.getController('Uni.controller.history.Router'),
    	    widget,
    	    model = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration');
    	 widget = Ext.widget('metrologyConfigurationEdit', {
    		 mcid: mcid,
             edit: true,
         });
    	me.getApplication().fireEvent('changecontentevent', widget);
    	widget.setEdit(true, '#');
        widget.setLoading(true);
        model.load(mcid, {
            success: function (record) {
                var form = widget.down('form');
                if (form) {
                    form.setTitle("Edit '" + record.get('name') + "'");
                    me.modelToForm(record, form);
                }
            },
            callback: function () {
                widget.setLoading(false);
            }
        });



    },

    saveMetrologyConfiguration: function (button) {
        var me = this,
        router = me.getController('Uni.controller.history.Router'),
        route;

        switch (button.action) {
        	case 'cancelButton':
        		route = 'administration/metrologyconfiguration';
        		break;
        	case 'saveModel':
        		route = 'administration/metrologyconfiguration';
        		me.saveModel(button);
        		break;

        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments, {previousRoute: router.getRoute().buildUrl()});

    },
    saveModel: function(button) {

        var me = this,
        page = me.getMetrologyConfigurationEditPage(),
        form = page.down('form'),
        formErrorsPanel = form.down('uni-form-error-message'),
        model;

	    if (form.getForm().isValid()) {
	        model = me.formToModel();

//	        button.setDisabled(true);
	        page.setLoading('Saving...');
	        formErrorsPanel.hide();
	        model.save({
	            callback: function (model, operation, success) {
	                page.setLoading(false);
//	                button.setDisabled(false);

	                if (success) {
	                    me.onSuccessSaving(operation.action, model.get('name'));
	                } else {
	                    me.onFailureSaving(operation.response);
	                }
	            }
	        });
	    } else {
	        formErrorsPanel.show();
	    }
    },
    modelToForm: function(model, form) {
        var data = model.getData(),
            basicForm  = form.getForm(),
            values = {};
        form.loadRecord(model);

        Ext.Object.each(data, function (key, value) {
            if (Ext.isObject(value)) {
                Ext.Object.each(value, function (valKey, valValue) {
                    values[key + valKey.charAt(0).toUpperCase() + valKey.slice(1)] = valValue;
                });
            }
        });

        basicForm.setValues(values);
    },
    formToModel: function () {
        var me=this,
            form = this.getMetrologyConfigurationEditPage().down('form'),
            values = form.getValues(),
            model = form.getRecord();
        if (!model) {
            model = Ext.create('Imt.metrologyconfiguration.model.MetrologyConfiguration');
        }
        model.beginEdit();
        model.set(values);
        model.set('name', values['name']);

        model.endEdit();

        return model;
    },

    onSuccessSaving: function (action) {
        var router = this.getController('Uni.controller.history.Router'),
            messageText;

        switch (action) {
            case 'create':
                messageText = Uni.I18n.translate('metrologyconfiguration.acknowledge.add.success', 'IMT', 'Metrology Configuration added');
                break;
            case 'update':
                messageText = Uni.I18n.translate('metrologyconfiguration.acknowledge.save.success', 'IMT', 'Metrology Configuration saved');
                break;
        }
        this.getApplication().fireEvent('acknowledge', messageText);
        router.getRoute().forward();
    },
    onFailureSaving: function (response) {
        var form = this.getMetrologyConfigurationEditPage().down('form'),
            formErrorsPanel = form.down('uni-form-error-message'),
            basicForm = form.getForm(),
            responseText;

        if (response.status == 400) {
            responseText = Ext.decode(response.responseText, true);
            if (responseText && responseText.errors) {
                basicForm.markInvalid(responseText.errors);
                formErrorsPanel.show();
            } else {
            	basicForm.markInvalid(response.responseText);
            	formErrorsPanel.show();
            }
        }
    },
    manageValidationRuleSets: function(id) {
      var me = this,
      	router = me.getController('Uni.controller.history.Router'),
          pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
      	metrologyConfigurationModel = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration'),
      	linkedStore=Ext.getStore('Imt.metrologyconfiguration.store.LinkedValidationRulesSet'),
      	linkableStore=Ext.getStore('Imt.metrologyconfiguration.store.LinkableValidationRulesSet');

      linkedStore.getProxy().setUrl(id);
      linkedStore.load();
      linkableStore.getProxy().setUrl(id);
      linkableStore.load();

        pageMainContent.setLoading();
        metrologyConfigurationModel.load(id, {
            success: function (record) {
                var view = Ext.create('Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetSetup',{router: router, mcid: id, metrologyConfig: record});
                me.getApplication().fireEvent('changecontentevent', view);
                view.down('#metrologyConfigValRulesSetEditForm').getForm().findField('mcid').setValue(id);
                view.down('#metrologyConfigValRulesSetEditForm').getForm().findField('name').setValue(record.get('name'));
                var form = view.down('form');
                if (form) {
                    form.setTitle("Link validation rule sets to '" + record.get('name') + "'");
                }
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });

    },
	addRulesSetsToMetrologyConfiguration: function(mcid) {
		var me = this,
		linkedStore = me.getMetrologyConfigValRulesSetEditForm().getForm().findField('linkedValidationRulesSets').getStore();
		linkableStore = me.getMetrologyConfigValRulesSetEditForm().getForm().findField('linkableValidationRulesSets').getStore();
		addItems = me.getMetrologyConfigValRulesSetEditForm().getForm().findField('linkableValidationRulesSets').getValue();

		var arrayModels = new Array();
		addItems.forEach(function(entry) {
				var rec = linkedStore.findRecord('id', entry);
				if (rec == null) {
					model = Ext.create('Imt.metrologyconfiguration.model.LinkedValidationRulesSet');
					rec = linkableStore.findRecord('id', entry);
					model.beginEdit();
					model.set('id', rec.get('id'));
					model.set('name', rec.get('name'));
					model.endEdit();
					arrayModels.push(model);
				}
				linkableStore.remove(rec);
		});
		linkedStore.loadData(arrayModels, true);
		me.getMetrologyConfigValRulesSetEditForm().getForm().findField('linkableValidationRulesSets').clearValue();
	},
	removeRulesSetsFromMetrologyConfiguration: function(mcid) {

		var me = this,
		linkedStore = me.getMetrologyConfigValRulesSetEditForm().getForm().findField('linkedValidationRulesSets').getStore();
		linkableStore = me.getMetrologyConfigValRulesSetEditForm().getForm().findField('linkableValidationRulesSets').getStore();
		removeItems = me.getMetrologyConfigValRulesSetEditForm().getForm().findField('linkedValidationRulesSets').getValue();

		var arrayModels = new Array();
		removeItems.forEach(function(entry) {
				var rec = linkableStore.findRecord('id', entry);
				if (rec == null) {
					model = Ext.create('Imt.metrologyconfiguration.model.LinkableValidationRulesSet');
					rec = linkedStore.findRecord('id', entry);
					model.beginEdit();
					model.set('id', rec.get('id'));
					model.set('name', rec.get('name'));
					model.endEdit();
					arrayModels.push(model);
				}
				linkedStore.remove(rec);
		});
		linkableStore.loadData(arrayModels, true);
		me.getMetrologyConfigValRulesSetEditForm().getForm().findField('linkedValidationRulesSets').clearValue();
	},
    saveRulesSetsToMetrologyConfiguration: function (button) {
        var me = this,
        router = me.getController('Uni.controller.history.Router'),
        route;
        switch (button.action) {
        	case 'cancelButton':
        		route = 'administration/metrologyconfiguration';
        		break;
        	case 'saveModel':
        		me.saveMetrologyConfigValRuleSetsModel(button);

        		break;

        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments, {previousRoute: router.getRoute().buildUrl()});

    },

    saveMetrologyConfigValRuleSetsModel: function(button) {

        var me = this,
        page = me.getMetrologyConfigValRulesSetEditPage(),
        form = page.down('form'),
        values = form.getValues(),
        linkedStore = form.getForm().findField('linkedValidationRulesSets').getStore(),
        assignedValidationRuleSets = Ext.create('Imt.metrologyconfiguration.model.LinkedValidationRulesSet'),
        formErrorsPanel = form.down('uni-form-error-message'),
        model,
        assignedRuleSetModelArray = new Array();

	    if (form.getForm().isValid()) {
	    	 linkedStore.each(function(record) {
	         	model = Ext.create('Imt.metrologyconfiguration.model.ValidationRuleSet');
	         	model.set('id',record.get('id'));
	         	model.set('name',record.get('name'))
	         	assignedRuleSetModelArray.push(model.getData());
	         })

	         assignedValidationRuleSets.set('ruleSets', assignedRuleSetModelArray);
	    	 button.setDisabled(true);
	    	 page.setLoading('Saving...');
	    	 formErrorsPanel.hide();
	    	 assignedValidationRuleSets.getProxy().setUrl(values['mcid']);
	    	 assignedValidationRuleSets.save({
	            callback: function (model, operation, success) {
	                page.setLoading(false);
	                button.setDisabled(false);

	                if (success) {
	                    me.onSuccessSavingValRuleSet(operation.action, model.get('name'));
	                } else {
	                    me.onFailureSavingvalRuleSet(operation.response);
	                }
	            }
	        });
	    } else {
	        formErrorsPanel.show();
	    }
    },
    onSuccessSavingValRuleSet: function (action) {
        var router = this.getController('Uni.controller.history.Router'),
            messageText;

        switch (action) {
            case 'create':
                messageText = Uni.I18n.translate('metrologyconfiguration.acknowledge.valrulesets.add.success', 'IMT', 'Validation rule sets added to metrology configuration');
                break;
            case 'update':
                messageText = Uni.I18n.translate('metrologyconfiguration.acknowledge.valrulesets.update.success', 'IMT', 'Validation rule sets updated to metrology configuration');
                break;
        }
        this.getApplication().fireEvent('acknowledge', messageText);
        router.getRoute().forward();
    },
    onFailureSavingvalRuleSet: function (response) {
        var form = this.getMetrologyConfigurationEditPage().down('form'),
            formErrorsPanel = form.down('uni-form-error-message'),
            basicForm = form.getForm(),
            responseText;

        if (response.status == 400) {
            responseText = Ext.decode(response.responseText, true);
            if (responseText && responseText.errors) {
                basicForm.markInvalid(responseText.errors);
                formErrorsPanel.show();
            } else {
            	basicForm.markInvalid(response.responseText);
            	formErrorsPanel.show();
            }
        }
    },

    showWizard: function (mRID) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointModel = me.getModel('Imt.usagepointmanagement.model.UsagePoint'),
            configurationModel = me.getModel('Imt.metrologyconfiguration.model.LinkableMetrologyConfiguration'),
            store = me.getStore('Imt.metrologyconfiguration.store.LinkableMetrologyConfigurations');

        mainView.setLoading();

        usagePointModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('usagePointLoaded', record);
                store.getProxy().setUrl(mRID);
                me.returnLink = router.queryParams.fromLandingPage ? router.getRoute('usagepoints/view').buildUrl() : router.getRoute('usagepoints/view/metrologyconfiguration').buildUrl();
                configurationModel.getProxy().setUrl(mRID);
                me.getStore('Imt.metrologyconfiguration.store.LinkableMetrologyConfigurations').load(function (records) {
                    var isPossibleAdd = records && records.length;
                    me.getApplication().fireEvent('changecontentevent', Ext.widget('define-metrology-configuration', {
                        itemId: 'define-metrology-configuration',
                        returnLink: me.returnLink,
                        isPossibleAdd: isPossibleAdd,
                        upVersion: record.get('version')
                    }));
                    me.getWizard().loadRecord(Ext.create('Imt.metrologyconfiguration.model.LinkableMetrologyConfiguration'));
                    mainView.setLoading(false);
                });
            }
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
        if(currentStep == 1){
            wizard.getRecord().customPropertySets().removeAll();
        }

        wizard.clearInvalid();
        if (direction > 0) {
            if (stepView.xtype === 'cps-info-form') {
                validationParams.customPropertySetId = stepView.getRecord().getId();
            }
            me.doRequest({
                params: validationParams,
                success: changeStep
            });
        } else {
            changeStep();
        }
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
        record.phantom = false;       // force 'PUT' method for request otherwise 'POST' will be performed
        modelProxy.appendId = false; // remove 'id' part from request url
        record.save(Ext.merge({
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
            }, Ext.merge(options, {
            params: {
                upVersion: wizard.upVersion
            },
                dontTryAgain: true,
                backUrl: me.returnLink
            })
        ));
        modelProxy.appendId = true; // restore id in the url for normal functionality
    },

    prepareNextStep: function (stepNumber) {
        var me = this,
            wizard = me.getWizard(),
            isLastStep = wizard.query('[isWizardStep=true]').length == stepNumber,
            buttons = wizard.getDockedComponent('define-metrology-configuration-wizard-buttons'),
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

    onMetrologyConfigurationChange: function (field, newValue) {
        var me = this,
            configuration = field.findRecordByValue(newValue),
            wizard = me.getWizard(),
            buttons = wizard.getDockedComponent('define-metrology-configuration-wizard-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            addBtn = buttons.down('[action=add]'),
            navigation = me.getNavigationMenu(),
            currentSteps = wizard.query('[isWizardStep=true]'),
            currentMenuItems = navigation.query('menuitem'),
            stepNumber = 1,
            stepsToAdd = [],
            navigationItemsToAdd = [];

        Ext.suspendLayouts();
        // remove all steps except first
        for (var i = 1; i < currentSteps.length; i++) {
            wizard.remove(currentSteps[i], true);
        }
        // remove all menu items except first
        for (var j = 1; j < currentMenuItems.length; j++) {
            navigation.remove(currentMenuItems[j], true);
        }
        wizard.getRecord().customPropertySets().removeAll();
        if(configuration){
            configuration.customPropertySets().each(function (record) {
                stepNumber++;
                stepsToAdd.push({
                    xtype: 'cps-info-form',
                    title: Uni.I18n.translate('metrologyConfiguration.wizard.cpsStepTitle', 'IMT', 'Step {0}: {1}', [stepNumber, record.get('name')]),
                    navigationIndex: stepNumber,
                    itemId: 'define-metrology-configuration-step' + stepNumber,
                    ui: 'large',
                    isWizardStep: true,
                    predefinedRecord: record
                });
                navigationItemsToAdd.push({
                    text: record.get('name')
                });
            });
        }

        if(navigationItemsToAdd.length){
            addBtn.hide();
            nextBtn.show();
        } else {
            addBtn.show();
            nextBtn.hide();
        }

        navigation.add(navigationItemsToAdd);
        wizard.add(stepsToAdd);
        wizard.updateRecord(configuration);
        Ext.resumeLayouts(true);
    },

    linkUsagePointToMetrologyConfiguration: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        me.doRequest({
            success: function (record) {
                router.getRoute('usagepoints/view/metrologyconfiguration').forward({mRID: record.get('mRID')});
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('metrologyConfiguration.wizard.successMsg', 'IMT', "Metrology configuration defined"));
            }
        });
    }
});