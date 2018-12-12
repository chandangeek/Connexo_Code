/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.validation.RuleSetVersionPreview', {
    extend: 'Ext.container.Container',
    alias: 'widget.validationConfigurationRulesSetVersionPreview',
    itemId: 'validationConfigurationRulesSetVersionPreview',
    title: '',
    ruleSetId: null,
    rulesSetVersionId: null,
    ui: 'medium',
    padding: 0,
    requires: [
        'Imt.purpose.view.summary.validation.RulesGrid',
        'Imt.purpose.view.summary.validation.RulePreview',
        'Uni.util.FormEmptyMessage'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'validationConfigurationRulesGrid',
                    ruleSetId: me.ruleSetId,
                    versionId: me.versionId
                },
                emptyComponent: {
                    xtype: 'uni-form-empty-message',
                    text: Uni.I18n.translate('validation.rules.empty', 'IMT', 'No validation rules have been defined yet.')
                },
                previewComponent: {
                    xtype: 'validationConfigurationRulePreview',
                    itemId: 'ruleItemPreviewContainer'
                }
            }
        ];
        me.callParent(arguments);
    }
});