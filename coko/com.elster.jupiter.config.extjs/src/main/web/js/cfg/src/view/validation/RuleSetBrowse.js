Ext.define('Cfg.view.validation.RuleSetBrowse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.validationrulesetBrowse',

    requires: [
        'Cfg.view.validation.RuleSetList',
        'Cfg.view.validation.RuleSetPreview',
        'Cfg.view.validation.RuleSetActionMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            ui: 'large',
            title: Uni.I18n.translate('general.validationRuleSets', 'CFG', 'Validation rule sets'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'validationrulesetList'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-validation-rule-set',
                        title: Uni.I18n.translate('validation.empty.title', 'CFG', 'No validation rule sets found'),
                        reasons: [
                            Uni.I18n.translate('validation.empty.list.item1', 'CFG', 'No validation rule sets have been added yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('validation.addRuleSet', 'CFG', 'Add validation rule set'),
                                privileges : Cfg.privileges.Validation.admin,
                                href: '#/administration/validation/rulesets/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'container',
                        itemId: 'ruleSetBrowsePreviewCt'
                    }

                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
