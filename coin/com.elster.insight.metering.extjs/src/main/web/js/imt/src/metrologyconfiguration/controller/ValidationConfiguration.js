/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.controller.ValidationConfiguration', {
    extend: 'Ext.app.Controller',

    views: [
        'Imt.metrologyconfiguration.view.validation.ValidationConfiguration',
        'Imt.metrologyconfiguration.view.validation.AddValidationRuleSetsToPurpose',
        'Imt.metrologyconfiguration.view.validation.ValidationSchedule'
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
            'add-validation-rule-sets-to-purpose purpose-rules-grid': {
                select: this.showRulePreview
            },
            'add-validation-rule-sets-to-purpose #add-validation-rule-sets-to-purpose-add-button': {
                click: this.addRuleSetsToPurpose
            }
        });
    },

    showValidationConfiguration: function (mcid, tab) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            metrologyConfigurationController = me.getController('Imt.metrologyconfiguration.controller.View');

        if (tab != 'rules') { //only 'rules' tab is available
            window.location.replace(router.getRoute('administration/metrologyconfiguration/view/validation').buildUrl({tab: 'rules'}));
        } else {
            pageMainContent.setLoading();
            metrologyConfigurationController.loadMetrologyConfiguration(mcid, {
                success: function (metrologyConfiguration) {
                    var widget = Ext.widget('metrology-configuration-validation-tab-view', {
                        itemId: 'metrology-configuration-validation-tab-view',
                        router: router,
                        metrologyConfig: metrologyConfiguration,
                        activeTab: tab,
                        controller: me
                    });
                    app.fireEvent('changecontentevent', widget);
                    pageMainContent.setLoading(false);
                },
                failure: function () {
                    pageMainContent.setLoading(false);
                }
            });
        }
    },

    showRules: function (rowModel, ruleSet) {
        var me = this,
            view = me.getValidationRuleSetsView(),
            rulesStore = me.getStore('Imt.store.ValidationRules');

        Ext.suspendLayouts();
        if (view.down('#purpose-rule-sets-add-rule-button')) {
            view.down('#purpose-rule-sets-add-rule-button').setHref(me.getController('Uni.controller.history.Router')
                .getRoute('administration/rulesets/overview/versions/overview/rules/add').buildUrl({ruleSetId: ruleSet.get('id'), versionId: ruleSet.get('currentVersionId')}));
        }
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
                    purpose.estimationRuleSets().removeAll();
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
            metrologyConfigurationController = me.getController('Imt.metrologyconfiguration.controller.View'),
            purposesWithLinkedRuleSetsStore = me.getStore('Imt.metrologyconfiguration.store.PurposesWithValidationRuleSets'),
            rulesStore = me.getStore('Imt.store.ValidationRules');

        pageMainContent.setLoading();
        metrologyConfigurationController.loadMetrologyConfiguration(id, {
            success: function (metrologyConfiguration) {
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
                                metrologyConfig: metrologyConfiguration,
                                purposesStore: purposesWithLinkedRuleSetsStore,
                                purposeWithLinkableRuleSets: purpose,
                                rulesStore: rulesStore
                            });
                            app.fireEvent('changecontentevent', widget);
                        },
                        callback: function () {
                            pageMainContent.setLoading(false);
                        }
                    });
                });
            },
            failure: function () {
                pageMainContent.setLoading(false);
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

        purpose.estimationRuleSets().removeAll();
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

    showRulesTab: function(panel) {
        var me = this,
            purposesWithLinkedRuleSetsStore = me.getStore('Imt.metrologyconfiguration.store.PurposesWithValidationRuleSets'),
            rulesStore = me.getStore('Imt.store.ValidationRules'),
            router = me.getController('Uni.controller.history.Router');

        Uni.util.History.suspendEventsForNextCall();
        Uni.util.History.setParsePath(false);
        router.getRoute('administration/metrologyconfiguration/view/validation').forward({tab: 'rules'});

        purposesWithLinkedRuleSetsStore.getProxy().extraParams = {
            metrologyConfigurationId: router.arguments.mcid
        };
        purposesWithLinkedRuleSetsStore.load(function (purposes) {
            Ext.suspendLayouts();
            panel.removeAll();
            panel.add({
                xtype: 'validation-mc-rule-sets',
                metrologyConfig: panel.metrologyConfig,
                rulesStore: rulesStore,
                purposes: purposes,
                router: router
            });

            var btn = panel.down('#metrology-config-add-validation-rule-set-btn');
            if (btn) {
                btn.setDisabled(panel.metrologyConfig.get('status').id == 'deprecated');
            }
            Ext.resumeLayouts(true);
        });
    }
});
