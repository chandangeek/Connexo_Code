Ext.define('Imt.usagepointmanagement.controller.Edit', {
    extend: 'Ext.app.Controller',

    requires: [
       'Uni.controller.history.Router',
       'Imt.usagepointmanagement.view.UsagePointEdit'
    ],
    models: [
             'Imt.usagepointmanagement.model.UsagePoint',
             'Imt.metrologyconfiguration.model.MetrologyConfiguration'
    ],
    stores: [
             'Imt.metrologyconfiguration.store.MetrologyConfigurationSelect'
    ],
    refs: [
           {
               ref: 'usagePointEditPage',
               selector: 'usagePointEdit'
           }
    ],
    selectedMetrologyConfig: null,
    init: function () {
        this.control({
            'usagePointEdit button[action=saveModel]': {
                click: this.saveUsagePoint
            },
            'usagePointEdit combobox[name=metrologyConfiguration]': {
                select: this.selectMetrologyConfiguration
            }
        });
    },
    selectMetrologyConfiguration: function(combo, record, index) { 
        this.selectedMetrologyConfig = record[0].data; 
    },
    createUsagePoint: function() {
    	var me = this,
    	    widget = Ext.widget('usagePointEdit');
    	me.getApplication().fireEvent('changecontentevent', widget);
    },
    editUsagePoint: function(id) {
    	var me = this,
    	    widget = Ext.widget('usagePointEdit'),
    	    model = me.getModel('Imt.usagepointmanagement.model.UsagePoint');
    	me.getApplication().fireEvent('changecontentevent', widget);
    	widget.setEdit(true, '#');
        widget.setLoading(true);
        model.load(id, {
            success: function (record) {
                var form = widget.down('form');
                if (form) {
                    me.modelToForm(record, form);
                }
            },
            callback: function () {
                widget.setLoading(false);
            }
        });
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
    modelToForm: function(model, form) {
        var data = model.getData(),
            basicForm = form.getForm(),
            values = {};
        this.selectedMetrologyConfig=data.metrologyConfiguration;
        form.loadRecord(model);
        if (data.metrologyConfiguration) {
            values['metrologyConfiguration']=data.metrologyConfiguration.name;           
        } else {
            values['metrologyConfiguration']='NONE';                       
        }
        Ext.Object.each(data, function (key, value) {
            if (Ext.isObject(value)) {
                if (value.unit) {
                    Ext.Object.each(value, function (valKey, valValue) {
                        values[key + valKey.charAt(0).toUpperCase() + valKey.slice(1)] = valValue;
                    });
                } 
            }
        });

        basicForm.setValues(values);
    },
    formToModel: function () {
        var me=this,
            form = this.getUsagePointEditPage().down('form'),
            values = form.getValues(),
            model = form.getRecord();
        if (!model) { 
            model = Ext.create('Imt.usagepointmanagement.model.UsagePoint');
        }
        model.beginEdit();
        model.set(values);
        model.set('nominalServiceVoltage', me.buildQuantity(values['nominalServiceVoltageValue'], 'V', 0));
        model.set('ratedCurrent', me.buildQuantity(values['ratedCurrentValue'], 'A', 0));
        model.set('ratedPower', me.buildQuantity(values['ratedPowerValue'], 'W', 3));
        model.set('estimatedLoad', me.buildQuantity(values['estimatedLoadValue'], values['estimatedLoadUnit'], 3));
        model.set('metrologyConfiguration', me.buildMC(this.selectedMetrologyConfig));
        model.endEdit();

        return model;
    },
    buildMC: function(mc) {
       if(mc == null || mc.id == 0) {
           return undefined;
       }
       var o = Object();
       o.id=mc.id;
       o.name=mc.name;
       return o;
    },
    buildQuantity: function(value, unit, mult) {
    	var o = Object();
    	o.value = value;
    	o.unit = unit;
    	o.multiplier = mult;
    	return o;
    },
    onSuccessSaving: function (action) {
        var router = this.getController('Uni.controller.history.Router'),
            messageText;

        switch (action) {
            case 'create':
                messageText = Uni.I18n.translate('usagePoint.acknowledge.createSuccess', 'IMT', 'Usage point added');
                break;
            case 'update':
                messageText = Uni.I18n.translate('usagePoint.acknowledge.updateSuccess', 'IMT', 'Usage point saved');
                break;
        }
        this.getApplication().fireEvent('acknowledge', messageText);
        router.getRoute().forward();
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