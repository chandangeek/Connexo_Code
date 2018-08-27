/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.validation.RulesSetMainView', {
    //extend: 'Uni.view.container.ContentContainer',
    extend: 'Ext.container.Container',
    alias: 'widget.validationConfigurationRulesSetMainView',
    itemId: 'validationConfigurationRulesSetMainView',

    device: null,

    requires: [
        'Imt.purpose.view.summary.validation.RulesSetGrid',
        'Imt.purpose.view.summary.validation.RulesSetPreview',
        'Imt.purpose.view.summary.validation.RuleSetVersionPreview',
        'Uni.view.container.PreviewContainer'
    ],
    initComponent: function () {
        var me = this;
        me.items = [
            {
                ui: 'medium',
                title: Uni.I18n.translate('device.dataValidation.rulesSetGrid.title', 'IMT', 'Validation rule sets'),
                margin: '0 0 0 -10',
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'validationConfigurationRulesSetGrid',
                            purposeId: me.purpose.get('name'),
                            store: 'Imt.purpose.store.PurposeValidationConfiguration'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'noItemsFoundPanel',
                            title: Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.title', 'IMT', 'No validation rule sets found'),
                            reasons: [
                                Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.item1', 'IMT', 'No validation rule sets have been defined yet.'),
                                Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.item2', 'IMT', 'Validation rule sets exist, but you do not have permission to view them.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('validation.addRuleSet', 'IMT', 'Add validation rule set'),
                                    privileges: Cfg.privileges.Validation.admin,
                                    href: '#/administration/validation/rulesets/add'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'container',
                            itemId: 'validationConfigurationRulesSetPreviewCt'
                        }
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});