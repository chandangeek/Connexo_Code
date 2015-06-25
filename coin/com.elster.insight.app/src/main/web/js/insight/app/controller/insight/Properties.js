Ext.define('InsightApp.controller.insight.Properties', {
    extend: 'Ext.app.Controller',

    requires: [
       'InsightApp.view.PropertiesView',
        'Cfg.model.ValidationRule',
        'Cfg.model.ValidationRuleSet',
        'Cfg.store.ValidationRules',
        'Uni.property.form.Property'
    ],
    refs: [
        { ref: 'propertyForm', selector: '#propertyForm' },
    ],
    test: function(){
        var me = this;
        var widget = Ext.widget('propertiesPanel');
        this.getApplication().fireEvent('changecontentevent', widget);

        var store = this.getStore('Cfg.store.ValidationRules');
        store.load(
            {
                params: {
                    id: 1
                },
               callback: function(results){
                   me.getPropertyForm().loadRecord(results[1]);
               }
        });
    }
});


