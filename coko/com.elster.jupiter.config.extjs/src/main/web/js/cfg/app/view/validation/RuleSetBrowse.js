Ext.define('Cfg.view.validation.RuleSetBrowse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.validationrulesetBrowse',

    requires: [
        'Cfg.view.validation.RuleSetList',
        'Cfg.view.validation.RuleSetBrowseFilter',
        'Cfg.view.validation.RuleSetPreview',
        'Cfg.view.validation.RuleSetActionMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            ui: 'large',
            xtype: 'panel',
            title: Uni.I18n.translate('validation.validationRuleSets', 'CFG', 'Validation rule sets'),
            overflowY: 'auto',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'validationrulesetList'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('validation.empty.title', 'CFG', 'No validation rule sets found'),
                        reasons: [
                            Uni.I18n.translate('validation.empty.list.item1', 'CFG', 'No validation rule sets have been added yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('validation.addRuleSet', 'CFG', 'Add validation rule set'),
                                ui: 'action',
                                href: '#/administration/validation/rulesets/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'validation-ruleset-preview',
                        tools: [
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.actions', 'CFG', 'Actions'),
                                iconCls: 'x-uni-action-iconD',
                                menu: {
                                    xtype: 'ruleset-action-menu'
                                }
                            }
                        ],
                        hidden: true
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
