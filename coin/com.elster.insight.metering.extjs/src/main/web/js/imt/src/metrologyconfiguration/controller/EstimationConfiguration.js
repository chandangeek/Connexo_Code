Ext.define('Imt.metrologyconfiguration.controller.EstimationConfiguration', {
    extend: 'Ext.app.Controller',

    views: [
        'Imt.metrologyconfiguration.view.estimation.EstimationConfiguration',
        'Imt.metrologyconfiguration.view.estimation.AddEstimationRuleSetsToPurpose'
    ],

    models: [
        'Imt.metrologyconfiguration.model.MetrologyConfiguration',
        'Imt.metrologyconfiguration.model.PurposeWithValidationRuleSets'
    ],
    stores: [
        'Imt.metrologyconfiguration.store.PurposesWithValidationRuleSets',
        'Imt.store.EstimationRules'
    ],

    refs: [
        {
            ref: 'estimationRuleSetsView',
            selector: 'metrology-configuration-estimation-tab-view estimation-mc-rule-sets'
        },
        {
            ref: 'addEstimationRuleSetsView',
            selector: 'add-estimation-rule-sets-to-purpose'
        }
    ],

    init: function () {
        this.control({
            'estimation-mc-rule-sets est-purpose-with-rule-sets-grid': {
                select: this.showRules
            },
            'estimation-mc-rule-sets est-purpose-rules-grid': {
                select: this.showRulePreview
            },
            'estimation-mc-rule-sets est-purpose-with-rule-sets-grid actioncolumn': {
                removeRuleSetFromPurpose: this.removeRuleSet
            },
            'add-estimation-rule-sets-to-purpose #purpose-combo': {
                change: this.showRuleSetsOfPurpose
            },
            'add-estimation-rule-sets-to-purpose est-purpose-rules-grid': {
                select: this.showRulePreview
            },
            'add-estimation-rule-sets-to-purpose #add-estimation-rule-sets-to-purpose-add-button': {
                click: this.addRuleSetsToPurpose
            },
            'est-purpose-with-rule-sets-grid [action=saveEstimationRuleSetsOrder]': {
                click: this.saveEstimationRuleSetsOrder
            }
        });
    },

    showEstimationConfiguration: function (mcid, tab) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            metrologyConfigurationController = me.getController('Imt.metrologyconfiguration.controller.View');

        if (tab != 'rules') { //only 'rules' tab is available
            window.location.replace(router.getRoute('administration/metrologyconfiguration/view/estimation').buildUrl({tab: 'rules'}, router.queryParams));
        } else {
            pageMainContent.setLoading();
            metrologyConfigurationController.loadMetrologyConfiguration(mcid, {
                success: function (metrologyConfiguration) {
                    var widget = Ext.widget('metrology-configuration-estimation-tab-view', {
                        itemId: 'metrology-configuration-estimation-tab-view',
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
            view = me.getEstimationRuleSetsView(),
            rulesStore = me.getStore('Imt.store.EstimationRules');

        Ext.suspendLayouts();
        if (view.down('#est-purpose-rule-sets-add-rule-button')) {
            view.down('#est-purpose-rule-sets-add-rule-button').setHref(me.getController('Uni.controller.history.Router')
                .getRoute('administration/estimationrulesets/estimationruleset/rules/add').buildUrl({ruleSetId: ruleSet.get('id')}));
        }
        rulesStore.getProxy().extraParams = {
            ruleSetId: ruleSet.get('id')
        };
        rulesStore.load(function () {
            view.down('#est-purpose-rules-grid-paging-top #displayItem')
                .setText(Uni.I18n.translate('metrologyConfiguration.estimation.rulesCount', 'IMT', '{0} estimation rule(s)', rulesStore.getCount()));
        });
        Ext.resumeLayouts(true);
    },

    showRulePreview: function (rowModel, rule) {
        var me = this,
            view = me.getEstimationRuleSetsView() || me.getAddEstimationRuleSetsView();

        view.down('estimation-rules-detail-form').updateForm(rule);
    },

    removeRuleSet: function (record) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('general.removex', 'IMT', "Remove '{0}'?", record.get('name')),
            msg: Uni.I18n.translate('estimationruleset.deleteConfirmation.msg1', 'IMT', 'This estimation rule set will be removed from the metrology configuration.'),
            config: {
                record: record
            },
            fn: function (action) {
                if (action == 'confirm') {
                    var purpose = _.find(me.getEstimationRuleSetsView().purposes, function (p) {
                        return p.getId() == record.get('metrologyContractId');
                    });
                    purpose.getProxy().extraParams = {
                        metrologyConfigurationId: me.getController('Uni.controller.history.Router').arguments.mcid,
                        action: 'remove'
                    };
                    purpose.validationRuleSets().removeAll();
                    purpose.estimationRuleSets().removeAll();
                    purpose.estimationRuleSets().add(record);
                    purpose.save({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationruleset.remove.successMsg', 'IMT', 'Estimation rule set removed'));
                            me.getController('Uni.controller.history.Router').getRoute().forward();
                        }
                    });
                }
            }
        });
    },

    showAddEstimationRuleSets: function (id) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            metrologyConfigurationController = me.getController('Imt.metrologyconfiguration.controller.View'),
            purposesWithLinkedRuleSetsStore = me.getStore('Imt.metrologyconfiguration.store.PurposesWithValidationRuleSets'),
            rulesStore = me.getStore('Imt.store.EstimationRules');

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
                            var widget = Ext.widget('add-estimation-rule-sets-to-purpose', {
                                itemId: 'add-estimation-rule-sets-to-purpose',
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
            store = me.getAddEstimationRuleSetsView().down('add-estimation-rule-sets-to-purpose-grid').getStore();

        me.getAddEstimationRuleSetsView().setLoading(true);
        me.getModel('Imt.metrologyconfiguration.model.PurposeWithValidationRuleSets').load(newValue, {
            success: function (purpose) {
                store.removeAll();
                store.add(purpose.estimationRuleSets().getRange());
                store.fireEvent('load');
            },
            callback: function () {
                me.getAddEstimationRuleSetsView().setLoading(false);
            }
        });
    },

    addRuleSetsToPurpose: function () {
        var me = this,
            purposesCombo = me.getAddEstimationRuleSetsView().down('#purpose-combo'),
            purpose = purposesCombo.findRecordByValue(purposesCombo.getValue()),
            records = me.getAddEstimationRuleSetsView().down('add-estimation-rule-sets-to-purpose-grid').getSelectionModel().getSelection();

        purpose.estimationRuleSets().removeAll();
        purpose.validationRuleSets().removeAll();
        purpose.estimationRuleSets().add(records);
        purpose.save({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translatePlural('estimationRuleSets.count.added', records.length, 'IMT', 'No estimation rule sets added',
                    '{0} estimation rule set added',
                    '{0} estimation rule sets added'));
                me.getController('Uni.controller.history.Router').getRoute('administration/metrologyconfiguration/view/estimation').forward();
            }
        });
    },

    showEstRulesTab: function (panel) {
        var me = this,
            purposesWithLinkedRuleSetsStore = me.getStore('Imt.metrologyconfiguration.store.PurposesWithValidationRuleSets'),
            rulesStore = me.getStore('Imt.store.EstimationRules'),
            router = me.getController('Uni.controller.history.Router');

        Uni.util.History.suspendEventsForNextCall();
        Uni.util.History.setParsePath(false);
        router.getRoute('administration/metrologyconfiguration/view/estimation').forward({tab: 'rules'}, router.queryParams);

        purposesWithLinkedRuleSetsStore.getProxy().extraParams = {
            metrologyConfigurationId: router.arguments.mcid
        };
        purposesWithLinkedRuleSetsStore.load(function (purposes) {
            Ext.suspendLayouts();
            panel.removeAll();
            panel.add({
                xtype: 'estimation-mc-rule-sets',
                itemId: 'estimation-mc-rule-sets-id',
                metrologyConfig: panel.metrologyConfig,
                editOrder: router.queryParams.editOrder,
                rulesStore: rulesStore,
                purposes: purposes,
                router: router
            });

            var btn = panel.down('#metrology-config-add-estimation-rule-set-btn');
            if (btn) {
                btn.setDisabled(panel.metrologyConfig.get('status').id == 'deprecated');
            }
            Ext.resumeLayouts(true);
        });
    },

    saveEstimationRuleSetsOrder: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            ruleSetsStore = me.getEstimationRuleSetsView().down('#est-purpose-with-rule-sets-grid').getStore(),
            purposesStore = me.getEstimationRuleSetsView().down('#est-purpose-with-rule-sets-grid').purposes,
            jsonData = {
                'total': purposesStore.length,
                'contracts': []
            };



        purposesStore.forEach(function (purpose) {
            var contract = purpose.getData(),
                estimationRuleSets = [];
            ruleSetsStore.each(function (ruleSet) {
                if(ruleSet.get('metrologyContractId') === contract.id && !ruleSet.get('noRuleSets'))
                    estimationRuleSets.push(ruleSet.getData());
                });
            contract.estimationRuleSets = estimationRuleSets;
            jsonData['contracts'].push(contract);
        });

        Ext.Ajax.request({
            url: '/api/ucr/metrologyconfigurations/' + router.arguments.mcid + '/contracts',
            method: 'PUT',
            jsonData: Ext.JSON.encode(jsonData),
            isNotEdit: true,
            success: function () {
                router.getRoute().forward();
            }
        });
    }
});
