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
        'Imt.metrologyconfiguration.view.MetrologyConfigurationAttributesForm',
        'Imt.metrologyconfiguration.view.CustomAttributeSets',
        'Imt.metrologyconfiguration.view.CustomAttributeSetsAdd'
    ],
    refs: [
        {ref: 'attributesPanel', selector: '#metrology-configuration-attributes-panel'}
    ],

    init: function () {
        this.control({
            '#custom-attribute-sets cas-grid actioncolumn': {
                removeCustomAttributeSet: this.removeCustomAttributeSet
            },
            '#custom-attribute-sets cas-detail-form menuitem[action=removeCustomAttributeSet]': {
                click: function(elm) {
                    this.removeCustomAttributeSet(elm.up('cas-detail-form').getRecord());
                }
            }
        });
    },

    showMetrologyConfiguration: function (id) {

        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = Ext.create('Imt.metrologyconfiguration.view.MetrologyConfigurationAttributesForm'),
            metrologyConfigurationModel = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration'),
            linkedStore = Ext.getStore('Imt.metrologyconfiguration.store.LinkedValidationRulesSet'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
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
                pageMainContent.setLoading(false);

            }
        });
    },

    showCustomAttributeSets: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            metrologyConfigurationModel = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration'),
            store = me.getStore('Imt.metrologyconfiguration.store.CustomAttributeSets'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        pageMainContent.setLoading(true);
        metrologyConfigurationModel.load(id, {
            success: function (record) {
                me.getApplication().fireEvent('metrologyConfigurationLoaded', record);
                var widget = Ext.widget('custom-attribute-sets', {router: router});
                me.getApplication().fireEvent('changecontentevent', widget);
                store.getProxy().extraParams.id = id;
                store.getProxy().extraParams.linked = true;
                store.load();
                pageMainContent.setLoading(false);
                widget.down('cas-grid').on('select', me.showCasPreview.bind(me, widget.down('cas-detail-form')));
            }
        });
    },

    showCasPreview: function (preview, selModel, record) {
        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        preview.loadRecord(record);
        Ext.resumeLayouts(true);
    },

    showAddCustomAttributeSets: function(id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            metrologyConfigurationModel = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration'),
            store = me.getStore('Imt.metrologyconfiguration.store.CustomAttributeSets'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        pageMainContent.setLoading(true);
        metrologyConfigurationModel.load(id, {
            success: function (record) {
                me.getApplication().fireEvent('metrologyConfigurationLoaded', record);
                var widget = Ext.widget('custom-attribute-sets-add', {router: router});
                me.getApplication().fireEvent('changecontentevent', widget);
                store.getProxy().extraParams.id = id;
                store.getProxy().extraParams.linked = false;
                store.load();
                pageMainContent.setLoading(false);

                widget.getAddButton().on('click', function() {
                    var records = widget.down('cas-selection-grid').getSelectionModel().getSelection();
                    me.addCAStoMetrologyConfiguration(record, records);
                });
                widget.getCancelButton().on('click', function() {
                    router.getRoute('administration/metrologyconfiguration/view/customAttributeSets').forward();
                });
            }
        });
    },

    addCAStoMetrologyConfiguration: function (mc, records) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        mc.set('customPropertySets', _.map(records, function(r) {return _.pick(r.getData(), 'customPropertySetId')}));
        mc.save({
            //failure: function(record, operation) {
            //    debugger;
            //    // do something if the load failed
            //},
            success: function(record, operation) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('metrologyconfiguration.label.CAS.added', 'IMT', 'Custom attribute sets added'));
            },
            callback: function(record, operation, success) {
                router.getRoute('administration/metrologyconfiguration/view/customAttributeSets').forward();
            }
        });
    },

    removeCustomAttributeSet: function (record) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('metrologyconfiguration.label.CAS.removeCustomAttributeSet', 'IMT', 'This custom attribute set will no longer be available.'),
            title: Uni.I18n.translate('metrologyconfiguration.label.CAS.removeCustomAttributeSet.title', 'IMT', "Remove '{0}'?",[record.get('name')]),
            fn: function(btn){
                if (btn === 'confirm') {
                    me.destroyCustomAttributeSet(record);
                }
            }
        });
    },

    destroyCustomAttributeSet: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        record.getProxy().extraParams.mcid =  router.arguments.mcid;
        record.destroy({
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute().forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('metrologyconfiguration.label.CAS.removed', 'IMT', 'Custom attribute set removed'));
            }
        });
    }
});

