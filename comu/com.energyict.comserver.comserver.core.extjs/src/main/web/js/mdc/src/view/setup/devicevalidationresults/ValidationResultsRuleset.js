Ext.define('Mdc.view.setup.devicevalidationresults.ValidationResultsRuleset', {
    extend: 'Uni.view.container.ContentContainer',	   
    alias: 'widget.deviceValidationResultsRuleset',    
    ui: 'medium',  
	layout: {
        type: 'vbox',
        align: 'stretch'
    },
	requires :[		
		'Mdc.view.setup.devicevalidationresults.RuleSetList',
		'Mdc.view.setup.devicevalidationresults.RuleSetVersionList',
		'Mdc.view.setup.devicevalidationresults.RuleSetVersionRuleList',
		'Mdc.view.setup.devicevalidationresults.RuleSetVersionRulesSummary',
		'Mdc.view.setup.devicedatavalidation.RulePreview'
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
					labelWidth: 150,
					labelAlign: 'left'
                },
				itemId: 'deviceValidationResultsRulesetForm',
				items: [
					{
						xtype: 'displayfield',						
						itemId: 'configuration-view-data-validated',
						fieldLabel: Uni.I18n.translate('validationResults.dataValidated', 'MDC', 'Data validated'),
						name: 'dataValidatedDisplay'					
						
					},
					{
						xtype: 'displayfield',						
						itemId: 'configuration-view-validation-results',
						fieldLabel: Uni.I18n.translate('validationResults.validationResults', 'MDC', 'Validation results'),
						name: 'total'					
					}
				]	

			},
			{
				 xtype: 'container',
				 layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },		
				 itemId: 'configurationViewValidationResultsBrowse',
				 items: [
					{
						ui: 'large',
						itemId: 'ruleSetList',
						title: Uni.I18n.translate('validationResults.validationRuleSets', 'MDC', 'Validation rule sets'),
						xtype: 'ruleSetList'
					},
					{
						ui: 'large',
						itemId: 'ruleSetVersionList',
						title: Uni.I18n.translate('validationResults.validationRuleSetVersions', 'MDC', 'Validation rule set versions'),
						xtype: 'ruleSetVersionList'
					},
					{
						ui: 'large',
						itemId: 'ruleSetVersionRuleList',
						title: Uni.I18n.translate('validationResults.validationRuleSetVersionRules', 'MDC', 'Validation rules'),
						xtype: 'ruleSetVersionRuleList'
					},
					{
						itemId: 'ruleSetVersionRulePreview',
						xtype: 'deviceDataValidationRulePreview'
					}
				 ]
			}
			
			
			
        ];
		 this.callParent(arguments);

	}

});

