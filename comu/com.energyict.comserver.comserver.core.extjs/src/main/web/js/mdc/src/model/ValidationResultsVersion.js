Ext.define('Mdc.model.ValidationResultsVersion', {
    extend: 'Ext.data.Model',
	
	requires: [
        'Mdc.model.ValidationResultsRule'
    ],	
	
    fields: [
        'id',
		'startDate',
		'endDate',
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
			name: 'ruleSet'
		},
		{
			name: 'ruleSetId',			            
            convert: function (value, record) {				
                return record.get('ruleSet').id;  
            }
		},
        {
            name: 'versionName',            
            convert: function (value, record) {
				
                var result, startDate, endDate;

                startDate = record.get('startDate');
                endDate = record.get('endDate');
                if (startDate && endDate) {
                    result = Uni.I18n.translate('validationResults.version.fromx', 'MDC', 'From {0}',[Uni.DateTime.formatDateTimeLong(new Date(startDate))])+ ' - ' +
                        Uni.I18n.translate('validationResults.version.untilx', 'MDC', 'Until {0}',[Uni.DateTime.formatDateTimeLong(new Date(endDate))]);
                } else if (startDate) {
                    result = Uni.I18n.translate('validationResults.version.fromx', 'MDC', 'From {0}'[Uni.DateTime.formatDateTimeLong(new Date(startDate))]);
                } else if (endDate) {
                    result = Uni.I18n.translate('validationResults.version.untilx', 'MDC', 'Until {0}',[Uni.DateTime.formatDateTimeLong(new Date(endDate))]);
                }else {
                    result = Uni.I18n.translate('validationResults.version.notStart', 'MDC', 'Always')
                }

				return result;
                //return '<a href="#/administration/validation/rulesets/' + data.ruleSet.id + '/versions/' + data.id + '">' + result + '</a>';
            }
        },
		'detailedRules'
    ],
	
	associations: [
        {
			name: 'detailedRules', 
			type: 'hasMany', 
			model: 'Mdc.model.ValidationResultsRule', 
			associationKey: 'detailedRules', 
			foreignKey: 'detailedRules'
        }
    ],
});
