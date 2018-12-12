/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                    result = Uni.I18n.translate('validationResults.version.fromxUntily', 'MDC', 'From {0} - Until {1}',
                        [Uni.DateTime.formatDateTime(new Date(startDate), Uni.DateTime.LONG, Uni.DateTime.SHORT),
                         Uni.DateTime.formatDateTime(new Date(endDate), Uni.DateTime.LONG, Uni.DateTime.SHORT)],
                        false
                    );
                } else if (startDate) {
                    result = Uni.I18n.translate('validationResults.version.fromx', 'MDC', 'From {0}',
                        Uni.DateTime.formatDateTime(new Date(startDate), Uni.DateTime.LONG, Uni.DateTime.SHORT),
                        false
                    );
                } else if (endDate) {
                    result = Uni.I18n.translate('validationResults.version.untilx', 'MDC', 'Until {0}',
                        Uni.DateTime.formatDateTime(new Date(endDate), Uni.DateTime.LONG, Uni.DateTime.SHORT),
                        false
                    );
                }else {
                    result = Uni.I18n.translate('validationResults.version.notStart', 'MDC', 'Always')
                }
				return result;
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
