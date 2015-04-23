Ext.define('Mdc.view.setup.devicevalidationresults.ValidationResultsRuleset', {
    extend: 'Uni.view.container.ContentContainer',	
   
    alias: 'widget.deviceValidationResultsRuleset',
    itemId: 'deviceValidationResultsRuleset',
    ui: 'medium',  
	layout: {
        type: 'vbox',
        align: 'stretch'
    },
	requires :[		
		'Mdc.view.setup.devicevalidationresults.RuleSetList',
		'Mdc.view.setup.devicevalidationresults.RuleSetVersionList',
		'Mdc.view.setup.devicevalidationresults.RuleSetVersionRuleList',
		'Mdc.view.setup.devicevalidationresults.RuleSetVersionRulesSummary'
	],
    margin: '0 0 0 -16',
    initComponent: function () {
        this.content = [            
			{
				xtype: 'form',		
				layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },			
				defaults: {
					labelWidth: 250,
					labelAlign: 'left'
                },
					
				items: [
					{
						xtype: 'displayfield',						
						itemId: 'configuration-view-data-validated',
						fieldLabel: Uni.I18n.translate('validationResults.dataValidated', 'MDC', 'Data validated'),
						name: 'dataValidated'					
						
					},
					{
						xtype: 'displayfield',						
						itemId: 'configuration-view-validation-results',
						fieldLabel: Uni.I18n.translate('validationResults.validationResults', 'MDC', 'Validation results'),
						name: 'validationResultsCount'					
					}
				]	

			},
			{
				 xtype: 'container',
				 layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },		
				 itemId: 'configuration-view-validation-results-browse',
				 items: [
					{
						ui: 'large',
						itemId: 'rule-set-list',
						title: Uni.I18n.translate('validationResults.validationRuleSets', 'MDC', 'Validation rule sets'),
						xtype: 'rulesetList'
					},
					{
						ui: 'large',
						itemId: 'rule-set-version-list',
						title: Uni.I18n.translate('validationResults.validationRuleSetVersions', 'MDC', 'Validation rule set versions'),
						xtype: 'ruleSetVersionList'
					},
					{
						ui: 'large',
						itemId: 'rule-set-version-rule-list',
						title: Uni.I18n.translate('validationResults.validationRuleSetVersionRules', 'MDC', 'Validation rules'),
						xtype: 'ruleSetVersionRuleList'
					}/*,
					{
						xtype: 'validationrulesetversionsBrowse'
					},
					{
						xtype: 'validationrulesBrowse'
					}*/
				 ]
			}
			
			
			
        ];
		 this.callParent(arguments);

	}

});

