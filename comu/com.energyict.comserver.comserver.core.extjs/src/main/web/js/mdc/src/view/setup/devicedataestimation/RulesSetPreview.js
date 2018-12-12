/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicedataestimation.RulesSetPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceDataEstimationRulesSetPreview',
    title: '',
    rulesSetId: null,
    ui: 'medium',
    padding: 0,
    requires: [
        'Mdc.view.setup.devicedataestimation.RulesGrid',
        'Mdc.view.setup.devicedataestimation.RulePreview'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'deviceDataEstimationRulesGrid',
                    itemId: 'device-data-estimation-rules-grid',
                    rulesSetId: me.rulesSetId
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'no-estimation-rules-panel',
                    title: Uni.I18n.translate('estimationDevice.rules.empty.title', 'MDC', 'No estimation rules found'),
                    reasons: [
                        Uni.I18n.translate('estimationDevice.rules.empty.list.item1', 'MDC', 'No estimation rules have been defined yet')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('estimationDevice.addEstimationRule', 'MDC', 'Add estimation rule'),
                            itemId: 'add-estimation-rule-button',
                            href: '#/administration/estimationrulesets/' + me.rulesSetId + '/rules/add'
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'deviceDataEstimationRulePreview',
                    itemId: 'device-data-estimation-rule-preview'
                }
            }
        ];
        me.callParent(arguments);
    }
});