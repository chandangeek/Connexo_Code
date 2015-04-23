Ext.define('Mdc.model.ValidationResultsRuleSet', {
    extend: 'Ext.data.Model',
	requires: [
        'Mdc.model.ValidationResultsVersion'
    ],
	
    fields: [
        'id',
        'name',				
		{
            name: 'total',            
            convert: function (value, record) {
                if (value) {
                    return  Ext.String.format(Uni.I18n.translate('validationResults.suspects', 'MDC', '{0} suspects'), value);                    
                }
                return '';    
            }
        }
    ],
	
	associations: [
        {
			name: 'detailedRuleSetVersions', 
			type: 'hasMany', 
			model: 'Mdc.model.ValidationResultsVersion', 
			associationKey: 'detailedRuleSetVersions', 
			foreignKey: 'detailedRuleSetVersions'
        }
    ],

});
