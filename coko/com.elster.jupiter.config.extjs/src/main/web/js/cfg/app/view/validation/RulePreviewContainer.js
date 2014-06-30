Ext.define('Cfg.view.validation.RulePreviewContainer', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.rulePreviewContainer',

    requires: [
        'Cfg.view.validation.RuleList',
        'Cfg.view.validation.RulePreview',
        'Cfg.view.validation.RuleActionMenu',
        'Uni.view.container.PreviewContainer',
        'Cfg.view.validation.RuleSetSubMenu'
    ],

    ruleSetId: null,

    initComponent: function () {
        var me = this;
        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('validation.validationRules', 'CFG', 'Validation rules'),
                overflowY: 'auto',
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewRuleContainer',
                        grid: {
                            xtype: 'validationruleList',
                            ruleSetId: me.ruleSetId
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
                                            html: '<h4>' + Uni.I18n.translate('validation.empty.rules.title', 'CFG', 'No validation rules found') + '</h4><br>' +
                                                Uni.I18n.translate('validation.empty.rules.detail', 'CFG', 'There are no validation rules. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('validation.empty.rules.list.item1', 'CFG', 'No validation rules have been added yet.') + '</li></lv><br>' +
                                                Uni.I18n.translate('validation.empty.steps', 'CFG', 'Possible steps:')
                                        },
                                        {
                                            xtype: 'button',
                                            text: Uni.I18n.translate('validation.addValidationRule', 'CFG', 'Add validation rule'),
                                            itemId: 'addRuleLink',
                                            ui: 'action',
                                            listeners: {
                                                click: {
                                                    fn: function () {
                                                        window.location.href = '#/administration/validation/rulesets/validationrules/' + me.ruleSetId + '/addRule/' + me.ruleSetId;
                                                    }
                                                }
                                            }
                                        }
                                    ]
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'validation-rule-preview'
                        }
                    }
                ]
            }
        ];
        this.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('validation.validationRuleSet', 'CFG', 'Validation rule set'),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                ui: 'medium',
                items: [
                    {
                        xtype: 'ruleSetSubMenu',
                        itemId: 'stepsMenu',
                        ruleSetId: this.ruleSetId,
                        toggle: 1
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});

