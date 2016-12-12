Ext.define('Imt.metrologyconfiguration.view.estimation.AddEstimationRuleSetsToPurposeGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.add-estimation-rule-sets-to-purpose-grid',
    router: null,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('estimationRuleSets.count.selected', count, 'IMT', 'No estimation rule sets selected',
            '{0} estimation rule set selected',
            '{0} estimation rule sets selected');
    },

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.label.activeRules', 'IMT', 'Active rules'),
                dataIndex: 'activeRules',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.label.inactiveRules', 'IMT', 'Inactive rules'),
                dataIndex: 'inactiveRules',
                flex: 1
            }
        ];

        me.listeners = {
            selectionchange: {
                fn: function (selModel, records) {
                    var view = Ext.ComponentQuery.query('add-estimation-rule-sets-to-purpose')[0],
                        ruleSet,
                        rulesStore = Ext.data.StoreManager.lookup('Imt.store.EstimationRules');

                    Ext.suspendLayouts();
                    if (records.length === 1) {
                        ruleSet = records[0];
                        view.down('#purpose-estimation-rule-sets-add-rule-button').setHref(me.router
                            .getRoute('administration/estimationrulesets/estimationruleset/rules/add').buildUrl({ruleSetId: ruleSet.get('id')}));
                        rulesStore.getProxy().extraParams = {
                            ruleSetId: ruleSet.get('id')
                        };
                        rulesStore.load(function () {
                            view.down('#est-purpose-rules-grid-paging-top').child('#displayItem')
                                .setText(Uni.I18n.translate('metrologyConfiguration.estimation.rulesCount', 'IMT', '{0} estimation rule(s)', rulesStore.getCount()));
                        });
                    }
                    view.down('#add-estimation-rule-sets-to-purpose-add-button').setDisabled(records.length === 0);
                    Ext.resumeLayouts(true);
                }
            }
        };

        me.callParent(arguments);
    }
});
