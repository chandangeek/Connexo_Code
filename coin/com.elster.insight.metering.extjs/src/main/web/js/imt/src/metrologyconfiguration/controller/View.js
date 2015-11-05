Ext.define('Imt.metrologyconfiguration.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router',
        'Imt.metrologyconfiguration.model.MetrologyConfiguration',
        'Ext.container.Container'
    ],
    stores: [
        'Imt.metrologyconfiguration.store.MetrologyConfiguration'
    ],
    views: [
        'Imt.metrologyconfiguration.view.Setup'
    ],
    refs: [
        {ref: 'attributesPanel', selector: '#metrology-configuration-attributes-panel'},
    ],

    init: function () {
    },

    showMetrologyConfiguration: function (id) {

        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            metrologyConfigurationModel = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            actualModel,
            actualForm;
     
        pageMainContent.setLoading(true);

        metrologyConfigurationModel.load(id, {

            success: function (record) {
                me.getApplication().fireEvent('metrologyConfigurationLoaded', record);
                var widget = Ext.widget('metrology-configuration-setup', {router: router});
                actualModel = Ext.create('Imt.metrologyconfiguration.model.MetrologyConfiguration', record.data);
                actualForm = Ext.create('Imt.metrologyconfiguration.view.MetrologyConfigurationAttributesForm', {router: router});
                
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getAttributesPanel().add(actualForm);
                actualForm.getForm().loadRecord(actualModel);
                pageMainContent.setLoading(false);
            }
        });
    },
});

