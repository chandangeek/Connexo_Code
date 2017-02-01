/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.validation.AddValidationRuleSetsToPurposeGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.add-validation-rule-sets-to-purpose-grid',
    router: null,
   
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('validationRuleSets.count.selected', count, 'IMT', 'No validation rule sets selected',
            '{0} validation rule set selected',
            '{0} validation rule sets selected');
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
                header: Uni.I18n.translate('validation.activeVersion', 'IMT', 'Active version'),
                dataIndex: 'currentVersion',
                flex: 1
            }
        ];

        me.listeners = {
            selectionchange: {
                fn: function (selModel, records) {
                    var view = Ext.ComponentQuery.query('add-validation-rule-sets-to-purpose')[0],
                        ruleSet,
                        rulesStore = Ext.data.StoreManager.lookup('Imt.store.ValidationRules');

                    Ext.suspendLayouts();
                    if (records.length === 1) {
                        ruleSet = records[0];
                        view.down('#purpose-rule-sets-add-rule-button').setHref(me.router
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
            }
        };

        me.callParent(arguments);
    }
});
