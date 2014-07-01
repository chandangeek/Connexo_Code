Ext.define('Cfg.view.validation.RuleSetBrowse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.validationrulesetBrowse',

    requires: [
        'Cfg.view.validation.RuleSetList',
        'Cfg.view.validation.RuleSetBrowseFilter',
        'Cfg.view.validation.RuleSetPreview',
        'Cfg.view.validation.RuleSetActionMenu',
        'Uni.view.container.PreviewContainer'
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
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'left'
                        },
                        minHeight: 20,
                        items: [
                            {
                                xtype: 'image',
                                margin: '0 10 0 0',
                                src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                height: 20,
                                width: 20
                            },
                            {
                                xtype: 'container',
                                items: [
                                    {
                                        xtype: 'component',
                                        html: '<h4>' + Uni.I18n.translate('validation.empty.title', 'CFG', 'No validation rule sets found') + '</h4><br>' +
                                            Uni.I18n.translate('validation.empty.detail', 'CFG', 'There are no validation rule sets. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('validation.empty.list.item1', 'CFG', 'No validation rule sets have been added yet.') + '</li></lv><br>' +
                                            Uni.I18n.translate('validation.empty.steps', 'CFG', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('validation.addRuleSet', 'CFG', 'Add validation rule set'),
                                        ui: 'action',
                                        href: '#/administration/validation/rulesets/add'
                                    }
                                ]
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
