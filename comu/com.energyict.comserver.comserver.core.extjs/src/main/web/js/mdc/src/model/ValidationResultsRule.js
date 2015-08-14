Ext.define('Mdc.model.ValidationResultsRule', {
    extend: 'Cfg.model.ValidationRule',
	requires: [
        'Uni.property.model.Property',
		'Cfg.model.ValidationRule'
    ],
    fields: [
    	{
            name: 'total',            
            convert: function (value, record) {
                if (value) {
                    return  Ext.String.format(Uni.I18n.translate('validationResults.suspects', 'MDC', '{0} suspects'), value);                    
                }
                return '';    
            }
        },
		{
            name: 'active',            
            convert: function (value, record) {
                if (value) {
                    return Uni.I18n.translate('general.active', 'MDC', 'Active');
                }
                return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
            }
        },
		{
			name: 'ruleSetId',			            
            convert: function (value, record) {				
                return record.get('ruleSetVersion').ruleSet.id;  
            }
		},
		{
			name: 'ruleSetVersionId',			            
            convert: function (value, record) {				
                return record.get('ruleSetVersion').id; 
            }
		},
    ]
	
     
});
