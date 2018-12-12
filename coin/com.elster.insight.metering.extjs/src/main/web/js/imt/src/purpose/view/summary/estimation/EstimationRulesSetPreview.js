/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.estimation.EstimationRulesSetPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.estimationCfgRulesSetPreview',
    itemId: 'estimationCfgRulesSetPreview',
    title: '',
    rulesSetId: null,
    ui: 'medium',
    padding: 0,
    requires: [
        'Imt.purpose.view.summary.estimation.EstimationRulesGrid',
        'Imt.purpose.view.summary.estimation.EstimationRulePreview'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'estimationCfgRulesGrid',
                    itemId: 'purpose-estimation-rules-grid',
                    rulesSetId: me.rulesSetId
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'no-estimation-rules-panel',
                    title: Uni.I18n.translate('estimationPurpose.rules.empty.title', 'IMT', 'No estimation rules found'),
                    reasons: [
                        Uni.I18n.translate('estimationPurpose.rules.empty.list.item1', 'IMT', 'No estimation rules have been defined yet')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('estimationPurpose.addEstimationRule', 'IMT', 'Add estimation rule'),
                            itemId: 'add-estimation-rule-button',
                            href: '#/administration/estimationrulesets/' + me.rulesSetId + '/rules/add'
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'estimationCfgRulePreview',
                    itemId: 'purpose-estimation-rule-preview'
                }
            }
        ];
        me.callParent(arguments);
    }
});