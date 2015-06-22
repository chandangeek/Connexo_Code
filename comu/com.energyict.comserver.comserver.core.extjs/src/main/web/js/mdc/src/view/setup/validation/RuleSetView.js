Ext.define('Mdc.view.setup.validation.RuleSetView', {
    extend: 'Ext.panel.Panel',
    xtype: 'validation-ruleset-view',
    ui: 'medium',
    padding: 0,

    requires: [
        'Mdc.view.setup.validation.RuleActionMenu',
        'Mdc.view.setup.validation.RuleSetVersionsGrid',
        'Cfg.view.validation.RulePreview',
        'Uni.view.container.PreviewContainer',
		'Mdc.view.setup.validation.RuleSetVersionPreview',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    validationRuleSetId: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {

                xtype: 'preview-container',
				selectByDefault: false,
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-validation-rule',
                    title: Uni.I18n.translate('validation.empty.versions.title', 'MDC', 'No validation rule set versions found'),
                    reasons: [
                        Uni.I18n.translate('validation.empty.versions.list.item1', 'MDC', 'No validation rule set versions have been added yet.'),
                    ],
                    stepItems: [
                        {
							text: Uni.I18n.translate('validation.addValidationRulesetVersion', 'MDC', 'Add validation rule set version'),
                            privileges : Cfg.privileges.Validation.deviceConfiguration,
                            ui: 'action',
                            action: 'addValidationVersion',
                            href: '#/administration/validation/rulesets/' + me.ruleSetId  + '/versions/add'
                        }
                    ]
                },
                grid: {
                    xtype: 'validation-versions-grid',
                    itemId: 'grd-validation-versions',
                    validationRuleSetId: me.validationRuleSetId
                },
                previewComponent: {
                    xtype: 'validation-versions-view'
                }
            }
        ];

        me.callParent(arguments);
    },

    updateValidationRuleSet: function (validationRuleSet) {
        var me = this,
            grid = me.down('validation-versions-grid'),
            addButton = me.down('button[action=addValidationVersion]');
			
        me.setTitle(validationRuleSet.get('name'));
        me.validationRuleSetId = validationRuleSet.get('id');		
        addButton.setHref('#/administration/validation/rulesets/' + me.validationRuleSetId + '/versions/add');
    //    grid.updateValidationRuleSetId(me.validationRuleSetId);

        grid.store.load({params: {
            ruleSetId: me.validationRuleSetId
        }});
    }
});