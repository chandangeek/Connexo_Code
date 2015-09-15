Ext.define('Mdc.model.ValidationRuleSetVersion', {
    extend: 'Ext.data.Model',	
    fields: [      
		{
			name: 'name',            
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
			}                
		},
		'startDate',
		'endDate',

    ],	
	proxy: {
        type: 'rest',
        urlTpl: '/api/val/validation/{ruleSetId}/versions/{versionId}',
        reader: {
            type: 'json'
        },

        setUrl: function (ruleSetId, versionId) {		
            this.url = this.urlTpl.replace('{ruleSetId}', ruleSetId).replace('{versionId}', versionId);		
        }
    }
}); 
