/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router',
        'Imt.metrologyconfiguration.model.MetrologyConfiguration',
        'Imt.metrologyconfiguration.model.ValidationRuleSet',
        'Ext.container.Container'
    ],
    models: [
        'Imt.metrologyconfiguration.model.MetrologyConfiguration',
        'Imt.metrologyconfiguration.model.ValidationRuleSet',
        'Imt.metrologyconfiguration.model.MetrologyConfiguration'
    ],
    views: [
        'Imt.metrologyconfiguration.view.Setup',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationDetailsForm',
        'Imt.metrologyconfiguration.view.CustomAttributeSets',
        'Imt.metrologyconfiguration.view.CustomAttributeSetsAdd'
    ],
    refs: [
        {ref: 'attributesPanel', selector: '#metrology-configuration-attributes-panel'},
        {ref: 'purposePreview', selector: '#metrology-configuration-setup #purpose-preview'},
        {ref: 'customAttributeSetsPanel', selector: 'custom-attribute-sets'},
        {ref: 'customAttributeSetsAddPanel', selector: 'custom-attribute-sets-add'}
    ],

    init: function () {
        this.control({
            '#custom-attribute-sets cas-grid actioncolumn': {
                deleteCAS: this.removeCustomAttributeSet
            },
            '#custom-attribute-sets cas-detail-form menuitem[action=removeCustomAttributeSet]': {
                click: function (elm) {
                    this.removeCustomAttributeSet(elm.up('cas-detail-form').getRecord());
                }
            },
            '#metrology-configuration-setup #purposes-grid': {
                select: this.showPurposePreview
            }
        });
    },

    loadMetrologyConfiguration: function (id, callback) {
        var me = this,
            app = me.getApplication(),
            failure = callback.failure;            

        me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration').load(id, {
            success: function (metrologyConfiguration) {
                app.fireEvent('metrologyConfigurationLoaded', metrologyConfiguration);
                callback.success(metrologyConfiguration);
            },
            failure: failure            
        });        
    },

    showMetrologyConfiguration: function (id) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        pageMainContent.setLoading();
        me.loadMetrologyConfiguration(id, {
            success: function (metrologyConfiguration) {
                var widget = Ext.widget('metrology-configuration-setup', {
                        itemId: 'metrology-configuration-setup',
                        router: router,
                        metrologyConfig: metrologyConfiguration
                    }),
                    menu = widget.down('#metrology-configuration-action-menu');
                
                widget.down('#metrology-config-setup-general-info').loadRecord(metrologyConfiguration);
                app.fireEvent('changecontentevent', widget);
                pageMainContent.setLoading(false);
            },
            failure: function () {
                pageMainContent.setLoading(false);
            }
        });        
    },

    showPurposePreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPurposePreview();

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        preview.loadRecord(record);
        Ext.resumeLayouts(true);
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
                var widget = Ext.widget('custom-attribute-sets', {router: router, metrologyConfiguration: record});
                me.getApplication().fireEvent('changecontentevent', widget);
                store.getProxy().extraParams.id = id;
                store.getProxy().extraParams.linked = true;
                store.on('load', (function () {
                    store.each(function (r) {
                        r.set('parent', record.getRecordData());
                        return record
                    });
                }));
                store.load({
                    callback: function () {
                        if(record.get('status').id == 'active' && !store.getCount()){
                            widget.down('#cas-preview-container').hide();
                        }
                    }
                });
                pageMainContent.setLoading(false);

                widget.down('cas-grid').on('select', me.showCasPreview.bind(me, widget.down('cas-detail-form')));
                widget.down('pagingtoolbartop').resetPaging();
                widget.down('pagingtoolbarbottom').resetPaging();
            }
        });
    },

    showCasPreview: function (preview, selModel, record) {
        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        preview.loadRecord(record);
        Ext.resumeLayouts(true);
    },

    showAddCustomAttributeSets: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            metrologyConfigurationModel = me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration'),
            store = me.getStore('Imt.metrologyconfiguration.store.CustomAttributeSets'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        pageMainContent.setLoading(true);
        metrologyConfigurationModel.load(id, {
            success: function (record) {
                me.getApplication().fireEvent('metrologyConfigurationLoaded', record);
                var widget = Ext.widget('custom-attribute-sets-add', {router: router, metrologyConfig: record});
                me.getApplication().fireEvent('changecontentevent', widget);
                store.getProxy().extraParams.id = id;
                store.getProxy().extraParams.linked = false;
                store.load();
                pageMainContent.setLoading(false);

                widget.getAddButton().on('click', function () {
                    var records = widget.down('cas-selection-grid').getSelectionModel().getSelection();
                    me.addCAStoMetrologyConfiguration(record, records);
                });
                widget.getCancelButton().on('click', function () {
                    router.getRoute('administration/metrologyconfiguration/view/customAttributeSets').forward();
                });
            }
        });
    },

    addCAStoMetrologyConfiguration: function (mc, records) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            panel = me.getCustomAttributeSetsAddPanel(),
            sets = _.map(records, function (r) {
                return _.pick(r.getData(), 'customPropertySetId')
            });

        panel.setLoading();
        mc.set('customPropertySets', mc.get('customPropertySets').concat(sets));
        mc.save({
            success: function (record, operation) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('metrologyconfiguration.label.CAS.added', 'IMT', 'Custom attribute sets added'));
                router.getRoute('administration/metrologyconfiguration/view/customAttributeSets').forward();
            },
            backUrl: router.getRoute('administration/metrologyconfiguration/view/customAttributeSets').buildUrl(),
            callback: function () {
                panel.setLoading(false);
            }
        });
    },

    removeCustomAttributeSet: function (record) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('metrologyconfiguration.label.CAS.removeCustomAttributeSet', 'IMT', 'This custom attribute set will no longer be available.'),
            title: Uni.I18n.translate('metrologyconfiguration.label.CAS.removeCustomAttributeSet.title', 'IMT', "Remove '{0}'?", [record.get('name')]),
            fn: function (btn) {
                if (btn === 'confirm') {
                    me.destroyCustomAttributeSet(record);
                }
            }
        });
    },

    destroyCustomAttributeSet: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            panel = me.getCustomAttributeSetsPanel();

        panel.setLoading();
        record.getProxy().extraParams.mcid = router.arguments.mcid;
        record.destroy({
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute().forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('metrologyconfiguration.label.CAS.removed', 'IMT', 'Custom attribute set removed'));
            },
            callback: function () {
                panel.setLoading(false);
            }
        });
    }
});

