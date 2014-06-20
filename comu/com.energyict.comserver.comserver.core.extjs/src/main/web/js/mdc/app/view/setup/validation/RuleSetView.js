Ext.define('Mdc.view.setup.validation.RuleSetView', {
    extend: 'Ext.panel.Panel',
    xtype: 'validation-ruleset-view',
    ui: 'medium',
    padding: 0,

    requires: [
        'Mdc.view.setup.validation.RulesGrid'
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
                                    html: '<h4>' + Uni.I18n.translate('validation.rule.empty.title', 'MDC', 'No validation rules found') + '</h4><br>' +
                                        Uni.I18n.translate('validation.rule.empty.detail', 'MDC', 'There are no validation rules. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                        Uni.I18n.translate('validation.rule.empty.list.item1', 'MDC', 'No validation rules have been added yet.') + '</li></lv><br>' +
                                        Uni.I18n.translate('validation.rule.empty.steps', 'MDC', 'Possible steps:')
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
                    // TODO Refactor the preview component so it can be reused here.
//                    xtype: 'validation-rule-preview',
//                    tools: [
//                        {
//                            xtype: 'button',
//                            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
//                            iconCls: 'x-uni-action-iconD',
//                            menu: {
//                                xtype: 'validation-add-ruleset-actionmenu'
//                            }
//                        }
//                    ]
                    xtype: 'component',
                    html: 'Here be dragons.'
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