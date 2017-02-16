/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicedatavalidation.RuleSetVersionPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceDataValidationRulesSetVersionPreview',
    itemId: 'deviceDataValidationRulesSetVersionPreview',
    title: '',
    ruleSetId: null,
    rulesSetVersionId: null,
    ui: 'medium',
    padding: 0,
    requires: [
        'Mdc.view.setup.devicedatavalidation.RulesGrid',
        'Mdc.view.setup.devicedatavalidation.RulePreview',
        'Uni.util.FormEmptyMessage'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'deviceDataValidationRulesGrid',
                    ruleSetId: me.ruleSetId,
                    versionId: me.versionId
                },
                emptyComponent: {
                    xtype: 'uni-form-empty-message',
                    text: Uni.I18n.translate('validation.rules.empty', 'MDC', 'No validation rules have been defined yet.')
                },
                previewComponent: {
                    xtype: 'deviceDataValidationRulePreview'
                }
            }
        ];
        me.callParent(arguments);
    }
});