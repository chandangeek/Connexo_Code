Ext.define('Mdc.view.setup.validation.RuleSetView', {
    extend: 'Ext.panel.Panel',
    xtype: 'validation-ruleset-view',
    ui: 'medium',
    padding: 0,

    requires: [
        'Mdc.view.setup.validation.RuleActionMenu',
        'Mdc.view.setup.validation.RulesGrid',
        'Cfg.view.validation.RulePreview'
    ],

    validationRuleSetId: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
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
                                    html: '<h4>' + Uni.I18n.translate('validation.empty.title', 'MDC', 'No validation rule sets found') + '</h4><br>' +
                                        Uni.I18n.translate('validation.empty.detail', 'MDC', 'There are no validation rule sets. This could be because:') + '<ul>' +
                                        '<li>' + Uni.I18n.translate('validation.empty.list.item1', 'MDC', 'No validation rule sets have been added yet.') + '</li>' +
                                        '<li>' + Uni.I18n.translate('validation.empty.list.item2', 'MDC', 'Validation rules exists, but you do not have permission to view them.') + '</li>' +
                                        '</ul>' + Uni.I18n.translate('validation.empty.steps', 'MDC', 'Possible steps:')
                                },
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('validation.addValidationRules', 'MDC', 'Add validation rule'),
                                    ui: 'action',
                                    action: 'addValidationRule',
                                    href: '#/administration/validation/addRule/' + me.validationRuleSetId
                                }
                            ]
                        }
                    ]
                },
                grid: {
                    xtype: 'validation-rules-grid'
                },
                previewComponent: {
                    xtype: 'validation-rule-preview',
                    tools: [
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                            iconCls: 'x-uni-action-iconD',
                            menu: {
                                xtype: 'validation-rule-actionmenu'
                            }
                        }
                    ]
                }
            }
        ];

        me.callParent(arguments);
    },

    updateValidationRuleSet: function (validationRuleSet) {
        var me = this,
            grid = me.down('validation-rules-grid'),
            addButton = me.down('button[action=addValidationRule]');

        me.setTitle(validationRuleSet.get('name'));
        me.validationRuleSetId = validationRuleSet.get('id');
        addButton.setHref('#/administration/validation/addRule/' + me.validationRuleSetId);

        grid.store.load({params: {
            id: me.validationRuleSetId
        }});
    }
});