Ext.define('InsightApp.controller.insight.UsagePoint', {
    extend: 'Ext.app.Controller',

    requires: [
       'InsightApp.view.UsagePointEdit'
    ],
    models: [
             'InsightApp.model.UsagePoint'
         ],
    refs: [
           {
               ref: 'usagePointEditPage',
               selector: 'usagePointEdit'
           }
    ],
    init: function () {
        this.control({
            'usagePointEdit button[action=saveModel]': {
                click: this.saveUsagePoint
            }
        });
    },
    test: function() {
    	var me = this,
    	    widget = Ext.widget('usagePointEdit');
    	me.getApplication().fireEvent('changecontentevent', widget);
    },
    saveUsagePoint: function (button) {
        var me = this,
        page = me.getUsagePointEditPage(),
        form = page.down('form'),
        formErrorsPanel = form.down('uni-form-error-message'),
        model;

	    if (form.getForm().isValid()) {
	        model = me.formToModel();
	
	        button.setDisabled(true);
	        page.setLoading('Saving...');
	        formErrorsPanel.hide();
	        //TODO: Some way to use success: and failure: instead of callback: to clean this up?
	        model.save({
	            callback: function (model, operation, success) {
	                page.setLoading(false);
	                button.setDisabled(false);
	
	                if (success) {
	                    me.onSuccessSaving(operation.action, model.get('usagePointType'));
	                } else {
	                    me.onFailureSaving(operation.response);
	                }
	            }
	        });
	    } else {
	        formErrorsPanel.show();
	    }
    },
    formToModel: function () {
        var me=this,
            form = this.getUsagePointEditPage().down('form'),
            values = form.getValues(),
            model = Ext.create('InsightApp.model.UsagePoint'),
            q = Ext.create('InsightApp.model.Quantity');
        model.beginEdit();
        model.set(values);
        q.set('unit', 'V');
        q.set('value', values['nominalVoltage.value']);
        q.set('multiplier', 0);
        model.set('nominalServiceVoltage', q.getData());
        model.endEdit();

        return model;
    },
    onSuccessSaving: function (action) {
        var router = this.getController('Uni.controller.history.Router'),
            messageText;

        switch (action) {
            case 'create':
                messageText = Uni.I18n.translate('usagePoint.acknowledge.createSuccess', 'INS', 'Usage point added');
                break;
            case 'update':
                messageText = Uni.I18n.translate('usagePoint.acknowledge.updateSuccess', 'INS', 'Usage point saved');
                break;
        }
        this.getApplication().fireEvent('acknowledge', messageText);
        //router.getRoute('administration/comservers').forward();
    },
    onFailureSaving: function (response) {
        var form = this.getUsagePointEditPage().down('form'),
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