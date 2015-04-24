Ext.define('Mdc.model.ValidationResultsRegister', {
    extend: 'Ext.data.Model',
	requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'id',
        'displayName',		
		'result',
		'implementation',        
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
			name: 'properties', 
			type: 'hasMany', 
			model: 'Uni.property.model.Property', 
			associationKey: 'properties', 
			foreignKey: 'properties',
            
			getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],
});
