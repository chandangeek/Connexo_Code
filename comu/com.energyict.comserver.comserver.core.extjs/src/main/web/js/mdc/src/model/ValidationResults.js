/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ValidationResults', {
    extend: 'Ext.data.Model',
	requires: [
        'Mdc.model.ValidationResultsRuleSet'
    ],
    fields: [      
		{
			name: 'validationStatus'
		},
		{
            name: 'allDataValidated',            
            convert: function (value, record) {			
				return record.get('validationStatus').allDataValidated;               
            }
        },
		{
            name: 'isActive',            
            convert: function (value, record) {			
				return record.get('validationStatus').isActive;               
            }
        },
		{
            name: 'lastChecked',            
            convert: function (value, record) {			
				return record.get('validationStatus').lastChecked;               
            }
        },
		{
            name: 'dataValidatedDisplay',            
            convert: function (value, record) {			
                if (record.get('validationStatus').allDataValidated) {
                    return  Uni.I18n.translate('validationResults.dataValidatedYes', 'MDC', 'Yes');
                }
                return Uni.I18n.translate('validationResults.dataValidatedNo', 'MDC', 'No');  
            }
        },
        {
            name: 'total',            
            convert: function (value, record) {			
                return Uni.I18n.translate('validationResults.suspects', 'MDC', '{0} suspects', [value]);
            }
        },
		{
			name: 'detailedRuleSets'
		}
		
    ],
	
    associations: [
        {
			name: 'detailedRuleSets', 
			type: 'hasMany', 
			model: 'Mdc.model.ValidationResultsRuleSet', 
			associationKey: 'detailedRuleSets', 
			foreignKey: 'detailedRuleSets'
        }
    ]
});
