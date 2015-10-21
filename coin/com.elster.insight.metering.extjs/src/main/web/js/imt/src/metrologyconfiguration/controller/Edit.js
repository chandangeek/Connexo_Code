Ext.define('Imt.metrologyconfiguration.controller.Edit', {
    extend: 'Ext.app.Controller',

    requires: [
       'Uni.controller.history.Router',
       'Imt.metrologyconfiguration.view.MetrologyConfigurationEdit'
    ],
    models: [
             'Imt.metrologyconfiguration.model.MetrologyConfiguration'
         ],
    refs: [
           {
               ref: 'metrologyConfigurationEditPage',
               selector: 'metrologyConfigurationEdit'
           }
    ],
    init: function () {
        this.control({
            'metrologyConfigurationEdit button[action=saveModel]': {
                click: this.saveMetrologyConfiguration
            },
            'metrologyConfigurationEdit button[action=cancelButton]': {
                click: this.saveMetrologyConfiguration
            },
        });
    },
    createMetrologyConfiguration: function() {
    	var me = this,
    	    widget = Ext.widget('metrologyConfigurationEdit');
    	me.getApplication().fireEvent('changecontentevent', widget);
    },
    editMetrologyConfiguration: function(id) {
    	var me = this,
    	    widget = Ext.widget('metrologyConfigurationEdit'),
    	    model = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration');
    	me.getApplication().fireEvent('changecontentevent', widget);
    	widget.setEdit(true, '#');
        widget.setLoading(true);
        model.load(id, {
            success: function (record) {
                var form = widget.down('form');
                me.modelToForm(record, form);
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
    }
});