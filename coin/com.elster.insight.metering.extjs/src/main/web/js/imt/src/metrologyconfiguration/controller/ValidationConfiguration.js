Ext.define('Imt.metrologyconfiguration.controller.ValidationConfiguration', {
    extend: 'Ext.app.Controller',

    views: [
        'Imt.metrologyconfiguration.view.validation.ValidationConfiguration',
        'Imt.metrologyconfiguration.view.validation.AddValidationRuleSetsToPurpose'
    ],

    models: [
        'Imt.metrologyconfiguration.model.MetrologyConfiguration',
        'Imt.metrologyconfiguration.model.PurposeWithValidationRuleSets'
    ],

    stores: [
        'Imt.metrologyconfiguration.store.PurposesWithValidationRuleSets',
        'Imt.store.ValidationRules'
    ],

    refs: [
        {
            ref: 'validationTabView',
            selector: 'metrology-configuration-validation-tab-view'
        },
        {
            ref: 'validationRuleSetsView',
            selector: 'metrology-configuration-validation-tab-view validation-mc-rule-sets'
        },
        {
            ref: 'addValidationRuleSetsView',
            selector: 'add-validation-rule-sets-to-purpose'
        }
    ],

    init: function () {        
        this.control({
            'validation-mc-rule-sets purpose-with-rule-sets-grid': {
                select: this.showRules
            },
            'validation-mc-rule-sets purpose-rules-grid': {
                select: this.showRulePreview
            },
            'validation-mc-rule-sets purpose-with-rule-sets-grid actioncolumn': {
                removeRuleSetFromPurpose: this.removeRuleSet
            },
            'add-validation-rule-sets-to-purpose #purpose-combo': {
                change: this.showRuleSetsOfPurpose
            },
            'add-validation-rule-sets-to-purpose add-validation-rule-sets-to-purpose-grid': {
                selectionchange: this.showAddValidationRuleSetsRules
            },
            'add-validation-rule-sets-to-purpose purpose-rules-grid': {
                select: this.showRulePreview
            },
            'add-validation-rule-sets-to-purpose #add-validation-rule-sets-to-purpose-add-button': {
                click: this.addRuleSetsToPurpose
            }
        });
    },

    showValidationConfiguration: function (id) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            purposesWithLinkedRuleSetsStore = me.getStore('Imt.metrologyconfiguration.store.PurposesWithValidationRuleSets'),
            rulesStore = me.getStore('Imt.store.ValidationRules');

        pageMainContent.setLoading();
        me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration').load(id, {
            success: function (metrologyConfig) {
                purposesWithLinkedRuleSetsStore.getProxy().extraParams = {
                    metrologyConfigurationId: router.arguments.mcid
                };
                purposesWithLinkedRuleSetsStore.load(function (purposes) {
                    var widget = Ext.widget('metrology-configuration-validation-tab-view', {
                        itemId: 'metrology-configuration-validation-tab-view',
                        router: router,
                        metrologyConfig: metrologyConfig,
                        purposes: purposes,
                        rulesStore: rulesStore
                    });
                    app.fireEvent('metrologyConfigurationLoaded', metrologyConfig);
                    app.fireEvent('changecontentevent', widget);
                });
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
    },

    showRules: function (rowModel, ruleSet) {
        var me = this,
            view = me.getValidationRuleSetsView(),
            rulesStore = me.getStore('Imt.store.ValidationRules');

        Ext.suspendLayouts();
        view.down('#purpose-rule-sets-add-rule-button').setHref(me.getController('Uni.controller.history.Router')
            .getRoute('administration/rulesets/overview/versions/overview/rules/add').buildUrl({ruleSetId: ruleSet.get('id'), versionId: ruleSet.get('currentVersionId')}));
        rulesStore.getProxy().extraParams = {
            ruleSetId: ruleSet.get('id'),
            versionId: ruleSet.get('currentVersionId')
        };
        rulesStore.load(function () {
            view.down('#purpose-rules-grid-paging-top').child('#displayItem')
                .setText(Uni.I18n.translate('metrologyConfiguration.validation.rulesCount', 'IMT', '{0} validation rule(s)', rulesStore.getCount()));
        });
        Ext.resumeLayouts(true);
    },

    showRulePreview: function (rowModel, rule) {
        var me = this,
            view = me.getValidationRuleSetsView() || me.getAddValidationRuleSetsView();

        view.down('validation-rule-preview').updateValidationRule(rule);
    },

    removeRuleSet: function (record) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('general.removex', 'IMT', "Remove '{0}'?", record.get('name')),
            msg: Uni.I18n.translate('validationruleset.deleteConfirmation.msg', 'IMT', 'This validation rule set will no longer be available.'),
            config: {
                record: record
            },
            fn: function (action) {
                if (action == 'confirm') {
                    var purpose = _.find(me.getValidationRuleSetsView().purposes, function (p) {
                        return p.getId() == record.get('metrologyContractId');
                    });
                    purpose.getProxy().extraParams = {
                        metrologyConfigurationId: me.getController('Uni.controller.history.Router').arguments.mcid,
                        action: 'remove'
                    };
                    purpose.validationRuleSets().removeAll();
                    purpose.validationRuleSets().add(record);
                    purpose.save({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationruleset.remove.successMsg', 'IMT', 'Validation rule set removed'));
                            me.getController('Uni.controller.history.Router').getRoute().forward();
                        }
                    });
                }
            }
        });
    },

    showAddValidationRuleSets: function (id) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            purposesWithLinkedRuleSetsStore = me.getStore('Imt.metrologyconfiguration.store.PurposesWithValidationRuleSets'),
            rulesStore = me.getStore('Imt.store.ValidationRules');

        pageMainContent.setLoading();
        me.getModel('Imt.metrologyconfiguration.model.MetrologyConfiguration').load(id, {
            success: function (metrologyConfig) {
                purposesWithLinkedRuleSetsStore.getProxy().extraParams = {
                    metrologyConfigurationId: router.arguments.mcid
                };
                purposesWithLinkedRuleSetsStore.load(function () {
                    me.getModel('Imt.metrologyconfiguration.model.PurposeWithValidationRuleSets').getProxy().extraParams = {
                        metrologyConfigurationId: router.arguments.mcid
                    };
                    me.getModel('Imt.metrologyconfiguration.model.PurposeWithValidationRuleSets').load(purposesWithLinkedRuleSetsStore.first().getId(), {
                        success: function (purpose) {
                            var widget = Ext.widget('add-validation-rule-sets-to-purpose', {
                                itemId: 'add-validation-rule-sets-to-purpose',
                                router: router,
                                metrologyConfig: metrologyConfig,
                                purposesStore: purposesWithLinkedRuleSetsStore,
                                purposeWithLinkableRuleSets: purpose,
                                rulesStore: rulesStore
                            });
                            app.fireEvent('metrologyConfigurationLoaded', metrologyConfig);
                            app.fireEvent('changecontentevent', widget);
                        },
                        callback: function () {
                            pageMainContent.setLoading(false);
                        }
                    });
                });
            }
        });
    },

    showRuleSetsOfPurpose: function (combo, newValue, oldValue) {
        var me = this,
            store = me.getAddValidationRuleSetsView().down('add-validation-rule-sets-to-purpose-grid').getStore();

        me.getModel('Imt.metrologyconfiguration.model.PurposeWithValidationRuleSets').load(newValue, {
            success: function (purpose) {
                store.removeAll();
                store.add(purpose.validationRuleSets().getRange());
                store.fireEvent('load');
            }
        });
    },

    addRuleSetsToPurpose: function () {
        var me = this,
            purposesCombo = me.getAddValidationRuleSetsView().down('#purpose-combo'),
            purpose = purposesCombo.findRecordByValue(purposesCombo.getValue()),
            records = me.getAddValidationRuleSetsView().down('add-validation-rule-sets-to-purpose-grid').getSelectionModel().getSelection();

        purpose.validationRuleSets().removeAll();
        purpose.validationRuleSets().add(records);
        purpose.save({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translatePlural('validationRuleSets.count.added', records.length, 'IMT', 'No validation rule sets added',
                    '{0} validation rule set added',
                    '{0} validation rule sets added'));
                me.getController('Uni.controller.history.Router').getRoute('administration/metrologyconfiguration/view/validation').forward();
            }
        });
    },

    showAddValidationRuleSetsRules: function (selModel, records) {
        var me = this,
            view = me.getAddValidationRuleSetsView(),
            ruleSet,
            rulesStore = me.getStore('Imt.store.ValidationRules');

        Ext.suspendLayouts();
        if (records.length === 1) {
            ruleSet = records[0];
            view.down('#purpose-rule-sets-add-rule-button').setHref(me.getController('Uni.controller.history.Router')
                .getRoute('administration/rulesets/overview/versions/overview/rules/add').buildUrl({ruleSetId: ruleSet.get('id'), versionId: ruleSet.get('currentVersionId')}));
            rulesStore.getProxy().extraParams = {
                ruleSetId: ruleSet.get('id'),
                versionId: ruleSet.get('currentVersionId')
            };
            rulesStore.load(function () {
                view.down('#purpose-rules-grid-paging-top').child('#displayItem')
                    .setText(Uni.I18n.translate('metrologyConfiguration.validation.rulesCount', 'IMT', '{0} validation rule(s)', rulesStore.getCount()));
            });
        }
        view.down('#add-validation-rule-sets-to-purpose-add-button').setDisabled(records.length === 0);
        Ext.resumeLayouts(true);
    }
});
