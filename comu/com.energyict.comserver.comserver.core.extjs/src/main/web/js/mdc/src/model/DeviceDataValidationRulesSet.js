Ext.define('Mdc.model.DeviceDataValidationRulesSet', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'isActive', type: 'boolean'},
        {name: 'numberOfInactiveRules', type: 'int'},
        {name: 'numberOfRules', type: 'int'},
        {
            name: 'numberOfActiveRules',
            persist: false,
            mapping: function (data) {
                return data.numberOfRules - data.numberOfInactiveRules;
            }
        },
		{
            name: 'activeVersion',            
            convert: function (value, record) {
				
                var result, startDate, endDate;

                startDate = record.get('startDate');
                endDate = record.get('endDate');
                if (startDate && endDate) {
                    result = Uni.I18n.translate('validationResults.version.from', 'MDC', 'From') + ' '+ Uni.DateTime.formatDateTimeLong(new Date(startDate)) + ' - ' +
                    Uni.I18n.translate('validationResults.version.until', 'MDC', 'Until') + ' '+ Uni.DateTime.formatDateTimeLong(new Date(endDate));
                } else if (startDate) {
                    result = Uni.I18n.translate('validationResults.version.from', 'MDC', 'From') + ' ' + Uni.DateTime.formatDateTimeLong(new Date(startDate));
                } else if (endDate) {
                    result = Uni.I18n.translate('validationResults.version.until', 'MDC', 'Until') + ' ' + Uni.DateTime.formatDateTimeLong(new Date(endDate));
                }else {
                    result = Uni.I18n.translate('validationResults.version.notStart', 'MDC', 'Always')
                }

				return result;                
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mRID}/validationrulesets',
        timeout: 60000,
        reader: {
            type: 'json',
            root: 'rulesets',
            totalProperty: 'total'
        }
    }
});