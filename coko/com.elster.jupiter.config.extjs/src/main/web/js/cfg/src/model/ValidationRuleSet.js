Ext.define('Cfg.model.ValidationRuleSet', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        'description',
		'numberOfVersions',
		{			
			name: 'activeVersion',
            persist: false,
            mapping: function (data) {
                var result, startDate, endDate;
				
				if (data.numberOfVersions === 0){
					result = Uni.I18n.translate('validation.version.noVersion', 'CFG', '-')
				}
				else {
					startDate = data.startDate;
					endDate = data.endDate;
					if (startDate && endDate) {
						result = Ext.String.format(Uni.I18n.translate('validation.version.display.fromUntil', 'CFG', "From {0} - Until {1}"), Uni.DateTime.formatDateTimeLong(new Date(startDate)), Uni.DateTime.formatDateTimeLong(new Date(endDate)));
					} else if (data.startDate) {
						result = Ext.String.format(Uni.I18n.translate('validation.version.display.from', 'CFG', "From {0}"), Uni.DateTime.formatDateTimeLong(new Date(startDate)));						
					} else if (data.endDate) {
						result = Ext.String.format(Uni.I18n.translate('validation.version.display.until', 'CFG', "Until {0}"), Uni.DateTime.formatDateTimeLong(new Date(endDate)));												
					}else {
						result = Uni.I18n.translate('validation.version.display.always', 'CFG', 'Always')
					}
				}

                return result;
            }

		}       
    ],

    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        headers: {'Accept': 'application/json'},
        reader: {
            type: 'json'
        }
    }
});
