Ext.define('Imt.metrologyconfiguration.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router',
        'Imt.metrologyconfiguration.model.MetrologyConfiguration',
        'Imt.metrologyconfiguration.model.ValidationRuleSet',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationAttributesForm',
        'Ext.container.Container'
    ],
    models: [
             'Imt.metrologyconfiguration.model.MetrologyConfiguration',
             'Imt.metrologyconfiguration.model.ValidationRuleSet'
    ],
    stores: [
        'Imt.metrologyconfiguration.store.MetrologyConfiguration',
        'Imt.metrologyconfiguration.store.LinkedValidationRulesSet'
    ],
    views: [
        'Imt.metrologyconfiguration.view.Setup',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationAttributesForm'
    ],
    refs: [
        {ref: 'attributesPanel', selector: '#metrology-configuration-attributes-panel'},
//        {ref: 'validationRuleSetLink', selector: '#validationRuleSetLink'},
    ],

    init: function () {
    },

    showMetrologyConfiguration: function (id) {

        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = Ext.create('Imt.metrologyconfiguration.view.MetrologyConfigurationAttributesForm'),
            metrologyConfigurationModel = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration'),
            linkedStore = Ext.getStore('Imt.metrologyconfiguration.store.LinkedValidationRulesSet'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
           // validationRuleSetLink,
            count,
            actualModel,
            actualForm;  
     
    	linkedStore.getProxy().setUrl(id);
    	linkedStore.load(function () {
    		count = this.getCount();
            if (count === 0) {
               this.add({id:'0', name:'-'});
            }
        });

        pageMainContent.setLoading(true);

        metrologyConfigurationModel.load(id, {

            success: function (record) {
                me.getApplication().fireEvent('metrologyConfigurationLoaded', record);
                var widget = Ext.widget('metrology-configuration-setup', {router: router});
                actualModel = Ext.create('Imt.metrologyconfiguration.model.MetrologyConfiguration', record.data);
                actualForm = Ext.create('Imt.metrologyconfiguration.view.MetrologyConfigurationAttributesForm', {router: router, mcid: id, count: count});
                
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getAttributesPanel().add(actualForm);
                actualForm.getForm().loadRecord(actualModel);
                widget.down('metrologyConfigurationActionMenu').record=record;

//                validationRuleSetLink = me.getValidationRuleSetLink();
//                validationRuleSetLink.href = '#/administration/metrologyconfiguration/' + encodeURIComponent(id) + '/associatedvalidationrulesets';
//                validationRuleSetLink.setText( 
//                    Uni.I18n.translatePlural('general.validationRuleSets', linkedStore.getCount(), 'IMT',
//                        'No validation rule sets', '1 validation rule set', '{0} validation rule sets'));
//                validationRuleSetLink.enable();
                pageMainContent.setLoading(false);

            }
        });
        
        
        
    },
});

