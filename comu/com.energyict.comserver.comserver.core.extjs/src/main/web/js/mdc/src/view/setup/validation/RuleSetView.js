Ext.define('Mdc.view.setup.validation.RuleSetView', {
    extend: 'Ext.panel.Panel',
    xtype: 'validation-ruleset-view',
    ui: 'medium',
    padding: 0,

    requires: [
        'Mdc.view.setup.validation.RuleActionMenu',
        'Mdc.view.setup.validation.RulesGrid',
        'Cfg.view.validation.RulePreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    validationRuleSetId: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-validation-rule',
                    title: Uni.I18n.translate('validation.empty.title', 'MDC', 'No validation rules found'),
                    reasons: [
                        Uni.I18n.translate('validation.empty.list.item1', 'MDC', 'No validation rules have been defined yet.'),
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('validation.addValidationRules', 'MDC', 'Add validation rule'),
                            privileges : Cfg.privileges.Validation.deviceConfiguration,
                            ui: 'action',
                            action: 'addValidationRule',
                            href: '#/administration/validation/rulesets/' + me.validationRuleSetId + '/rules/add'
                        }
                    ]
                },
                grid: {
                    xtype: 'validation-rules-grid',
                    itemId: 'grd-validation-rules',
                    validationRuleSetId: me.validationRuleSetId
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
        addButton.setHref('#/administration/validation/rulesets/' + me.validationRuleSetId + '/rules/add');
        grid.updateValidationRuleSetId(me.validationRuleSetId);

        grid.store.load({params: {
            id: me.validationRuleSetId
        }});
    }
});