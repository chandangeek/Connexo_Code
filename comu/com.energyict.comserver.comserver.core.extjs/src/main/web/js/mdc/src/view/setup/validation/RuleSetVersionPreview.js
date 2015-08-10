Ext.define('Mdc.view.setup.validation.RuleSetVersionPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.validation-versions-view',
    title: '',
    ruleSetId: null,
    versionId: null,
    ui: 'medium',
    padding: 0,
    requires: [
        'Mdc.view.setup.validation.RuleSetVersionsGrid',        
		'Cfg.view.validation.RulePreview'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'validation-rules-grid',
                    ruleSetId: me.ruleSetId,
                    versionId: me.versionId
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    title: Uni.I18n.translate('validation.empty.rules.title', 'MDC', 'No validation rules found'),
                    reasons: [
                        Uni.I18n.translate('validation.empty.list.rules.item1', 'MDC', 'No validation rules have been defined yet.')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('validation.addValidationRule', 'MDC', 'Add validation rule'),
                            privileges : Cfg.privileges.Validation.device,                            
							action: 'addValidationRule',
                            href: '#/administration/validation/rulesets/' + me.ruleSetId + '/versions/' + me.versionId + '/rules/add'
                        }
                    ]
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
	
    updateValidationVersionSet: function (validationVersion) {
        var me = this,
            grid = me.down('validation-rules-grid'),
            addButton = me.down('button[action=addValidationRule]');
			
		
        me.setTitle(validationVersion.get('name'));
        me.validationRuleSetId = validationVersion.get('ruleSetId');		
		me.versionId = validationVersion.get('id');						
        addButton.setHref('#/administration/validation/rulesets/' + me.validationRuleSetId + '/versions/' + me.versionId + '/rules/add');
    

        grid.store.load({params: {
            ruleSetId: me.validationRuleSetId,
			versionId: me.versionId
        }});
    }
});