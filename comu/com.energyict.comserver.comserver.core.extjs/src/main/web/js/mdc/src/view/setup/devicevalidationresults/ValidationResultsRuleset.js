Ext.define('Mdc.view.setup.devicevalidationresults.ValidationResultsRuleset', {
    //extend: 'Uni.view.container.ContentContainer',	   
	  extend: 'Ext.container.Container',
    alias: 'widget.deviceValidationResultsRuleset',    
    //ui: 'medium',  
	layout: {
        type: 'vbox',
        align: 'stretch'
    },
	requires :[		
		'Mdc.view.setup.devicevalidationresults.RuleSetList',
		'Mdc.view.setup.devicevalidationresults.RuleSetVersionList',
		'Mdc.view.setup.devicevalidationresults.RuleSetVersionRuleList',		
		'Mdc.view.setup.devicedatavalidation.RulePreview'
	],
   
    initComponent: function () {
        this.items = [            
			{
				xtype: 'container',	
				 layout: {
					type: 'hbox',
					align: 'stretch'
				},				
				items: [
					{
						xtype: 'form',	
						itemId: 'deviceValidationResultsRulesetForm',	
						flex: 1,						
						layout: {
							type: 'vbox',
							align: 'stretch'
						},			
						defaults: {
							labelWidth: 150,
							labelAlign: 'left'
						},						
						items: [
							{
								xtype: 'displayfield',						
								itemId: 'configuration-view-data-validated',
								fieldLabel: Uni.I18n.translate('validationResults.dataValidated', 'MDC', 'Data validated'),
								name: 'dataValidatedDisplay',
								value: Uni.I18n.translate('validationResults.updatingStatus', 'MDC', 'Updating status...')					
								
							},
							{
								xtype: 'displayfield',						
								itemId: 'configuration-view-validation-results',
								fieldLabel: Uni.I18n.translate('validationResults.validationResults', 'MDC', 'Validation results'),
								name: 'total',
								value: Uni.I18n.translate('validationResults.updatingStatus', 'MDC', 'Updating status...')					
							}	
						]
					},
					{
						xtype: 'container',		
						layout: {
							type: 'hbox',
							align: 'bottom',
							pack: 'end'
						},							
						items: [
							{
								xtype: 'button',
								itemId: 'configurationViewValidateNow',
								text: Uni.I18n.translate('validationResults.validateNow', 'MDC', 'Validate now'),
								disabled: true,
								action: 'validateNow'								
							}
						]
					}						

				]
			},
			{
				 xtype: 'container',
				 layout: {
                        type: 'vbox',
                        align: 'stretch'
                 },
				 ui: 'medium', 
				// margin: '0 -16 0 -16',
				 hidden: true,				 
				 itemId: 'configurationViewValidationResultsBrowse',
				 items: [
					{
						ui: 'medium',
						margin: '0 -16 0 -16',
						itemId: 'ruleSetList',
						title: Uni.I18n.translate('validationResults.validationRuleSets', 'MDC', 'Validation rule sets'),
						xtype: 'mdc-rule-set-list'
					},
					{
						ui: 'medium',
						itemId: 'ruleSetVersionList',
						margin: '0 -16 0 -16',
						title: Uni.I18n.translate('validationResults.validationRuleSetVersions', 'MDC', 'Validation rule set versions'),
						xtype: 'mdc-rule-set-version-list'
					},
					{
						ui: 'medium',
						margin: '0 -16 0 -16',
						itemId: 'ruleSetVersionRuleList',
						title: Uni.I18n.translate('validationResults.validationRuleSetVersionRules', 'MDC', 'Validation rules'),
						xtype: 'rule-set-version-rule-list'
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

