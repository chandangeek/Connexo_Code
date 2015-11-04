Ext.define('Imt.metrologyconfiguration.controller.Edit', {
    extend: 'Ext.app.Controller',

    requires: [
       'Uni.controller.history.Router',
       'Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetSetup',
       'Imt.metrologyconfiguration.view.MetrologyConfigurationEdit',
       'Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetEdit',
       'Imt.metrologyconfiguration.store.MetrologyConfiguration',
       'Imt.metrologyconfiguration.store.LinkedValidationRulesSet',
       'Imt.metrologyconfiguration.store.LinkableValidationRulesSet',
    ],
    models: [
             'Imt.metrologyconfiguration.model.MetrologyConfiguration',
             'Imt.metrologyconfiguration.model.LinkedValidationRulesSet',
             'Imt.metrologyconfiguration.model.LinkableValidationRulesSet',
    ],
    stores: [
             'Imt.metrologyconfiguration.store.MetrologyConfiguration',
             'Imt.metrologyconfiguration.store.LinkedValidationRulesSet',
             'Imt.metrologyconfiguration.store.LinkableValidationRulesSet',
    ],
    refs: [
           {ref: 'metrologyConfigurationEditPage', selector: 'metrologyConfigurationEdit'},
           {ref: 'metrologyConfigurationNameLabel', selector: '#metrology-configuration-name-label'},  
           {ref: 'metrologyConfigValRulesSetEditForm', selector: '#metrologyConfigValRulesSetEditForm'},
           {ref: 'metrologyConfigValRulesSetSetup', selector: '#metrologyConfigValRulesSetSetup'},
           {ref: 'overviewLink', selector: '#metrology-configuration-overview-link'},
           {ref: 'metrologyConfigValRulesSetEditPanel', selector: '#metrologyConfigValRulesSetEditPanel'},
           {ref: 'metrologyConfigValRulesSetEditPage', selector: 'metrologyConfigValRulesSetEdit'},
    ],
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
        });
    },
    createMetrologyConfiguration: function() {
    	var me = this,
    	    widget = Ext.widget('metrologyConfigurationEdit');
    	me.getApplication().fireEvent('changecontentevent', widget);
    },
    editMetrologyConfiguration: function(mcid) {
    	var me = this,
    	    widget = Ext.widget('metrologyConfigurationEdit'),
    	    model = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration');
    	me.getApplication().fireEvent('changecontentevent', widget);
    	widget.setEdit(true, '#');
        widget.setLoading(true);
        model.load(mcid, {
            success: function (record) {
                var form = widget.down('form');
                me.modelToForm(record, form);
            },
            callback: function () {
                widget.setLoading(false);
            }
        });
    },    
    deleteMetrologyConfiguration: function(mcid) {
        var me = this,
        router = me.getController('Uni.controller.history.Router'),
        model = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration'),
        store = me.getStore('Imt.metrologyconfiguration.store.MetrologyConfiguration'),
        mcName,
        mcRecord;
        model.load(mcid, {
            success: function (record) {
            	mcName = record.get('name');
            	mcRecord = record;
            }
	    });
        var msg = 'Are you sure you woule like to delete - ' + mcName;

        Ext.MessageBox.confirm('Delete', msg , function(btn){
        	   if(btn === 'yes') {

//        		   store.remove(mcRecord);
//        		   store.sync();
        		   
        	   }
 
        });
    },    
    saveMetrologyConfiguration: function (button) {
        var me = this,
        router = me.getController('Uni.controller.history.Router'),
        route;

        switch (button.action) {
        	case 'cancelButton':
        		route = 'metrologyconfiguration/overview';
        		break;
        	case 'saveModel':
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
	
	        button.setDisabled(true);
	        page.setLoading('Saving...');
	        formErrorsPanel.hide();
	        model.save({
	            callback: function (model, operation, success) {
	                page.setLoading(false);
	                button.setDisabled(false);
	
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
                messageText = Uni.I18n.translate('metrologyConfiguration.acknowledge.createSuccess', 'IMT', 'Metrology Configuration added');
                break;
            case 'update':
                messageText = Uni.I18n.translate('metrologyConfiguration.acknowledge.updateSuccess', 'IMT', 'Metrology Configuration saved');
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
    	this.manageValidationRuleSets1(id);
    },
    manageValidationRuleSets1: function(id) {
      var me = this,
      	view = Ext.create('Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetEdit',{mcid: id}),
      	metrologyConfigurationModel = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration'),
      	linkedValRulesSetCombo = view.down('#metrology-config-linked-val-rules-set'),
      	linkableValRulesSetCombo = view.down('#metrology-config-linkable-val-rules-set');
	    me.getApplication().fireEvent('changecontentevent', view);

	    metrologyConfigurationModel.load(id, {
            success: function (record) {
            	me.getMetrologyConfigValRulesSetEditForm().getForm().findField('mcid').setValue(id);
            	me.getMetrologyConfigValRulesSetEditForm().getForm().findField('name').setValue(record.get('name'));
            }
	    });

	    linkedValRulesSetCombo.store.getProxy().setUrl(id);
	    linkedValRulesSetCombo.store.load(function () {
	        if (this.getCount() === 0) {
	        	linkedValRulesSetCombo.allowBlank = true;
	        }
	    });
	    linkableValRulesSetCombo.store.getProxy().setUrl(id);
	    linkableValRulesSetCombo.store.load(function () {
	         if (this.getCount() === 0) {
	        	 linkableValRulesSetCombo.allowBlank = true;
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
        		route = 'metrologyconfiguration/overview';
        		break;
        	case 'saveModel':
        		me.saveMetrologyConfigValRuleSetsModel(button);
        		alert('save model');
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
        assignedValidationRuleSets = Ext.create('Imt.metrologyconfiguration.model.LinkedValidationRulesSet');
        formErrorsPanel = form.down('uni-form-error-message'),
        model,
        assignedRuleSetModelArray = new Array();
        
        linkedStore.each(function(record) {
        	model = Ext.create('Imt.metrologyconfiguration.model.ValidationRuleSet');
        	model.set('id',record.get('id'));
        	model.set('name',record.get('name'))
        	assignedRuleSetModelArray.push(model.getData());
        })
     
        
        assignedValidationRuleSets.set('ruleSets', assignedRuleSetModelArray);

	    if (form.getForm().isValid()) {
//        model = me.formToValRuleSetModel();
	
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

    formToValRuleSetModel: function () {
        var me=this,
            form = this.getMetrologyConfigValRulesSetEditPage().down('form'),
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

    onSuccessSavingValRuleSet: function (action) {
        var router = this.getController('Uni.controller.history.Router'),
            messageText;

        switch (action) {
            case 'create':
                messageText = Uni.I18n.translate('metrologyConfiguration.acknowledge.createSuccess', 'IMT', 'Metrology Configuration added');
                break;
            case 'update':
                messageText = Uni.I18n.translate('metrologyConfiguration.acknowledge.updateSuccess', 'IMT', 'Metrology Configuration saved');
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

});