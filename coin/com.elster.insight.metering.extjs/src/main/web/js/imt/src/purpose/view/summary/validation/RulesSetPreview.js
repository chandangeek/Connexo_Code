/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.validation.RulesSetPreview', {
    extend: 'Ext.container.Container',
    alias: 'widget.validationConfigurationRulesSetPreview',
    itemId: 'validationConfigurationRulesSetPreview',
    title: '',
    ruleSetId: null,
    ui: 'medium',
    padding: 0,
    requires: [
        'Imt.purpose.view.summary.validation.RuleSetVersionsGrid',
        'Imt.purpose.view.summary.validation.RuleSetVersionPreview'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'validationConfigurationRuleSetVersionsGrid',
                    ruleSetId: me.ruleSetId
                },
                emptyComponent: {


                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-validation-rule',
                    title: Uni.I18n.translate('validation.empty.versions.title', 'IMT', 'No validation rule set versions found'),
                    reasons: [
                        Uni.I18n.translate('validation.empty.versions.list.item1', 'IMT', 'No validation rule set versions have been added yet.')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('validation.addValidationRulesetVersion', 'IMT', 'Add validation rule set version'),
                            privileges: Cfg.privileges.Validation.admin,
                            ui: 'action',
                            href: '#/administration/validation/rulesets/' + me.ruleSetId + '/versions/add'
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'container',
                    itemId: 'validationConfigurationRuleSetVersionsPreviewCt'
                }
            }
        ];
        me.callParent(arguments);
    }
});