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
				
				if (data.numberOfVersions == 0){
					result = Uni.I18n.translate('validation.version.noVersion', 'CFG', '-')
				}
				else {
					startDate = data.startDate;
					endDate = data.endDate;
					if (startDate && endDate) {
						result = Uni.I18n.translate('validation.version.from', 'CFG', 'From') + ' '+ Uni.DateTime.formatDateTimeLong(new Date(startDate)) + ' - ' +
						Uni.I18n.translate('validation.version.until', 'CFG', 'Until') + ' '+ Uni.DateTime.formatDateTimeLong(new Date(endDate));
					} else if (data.startDate) {
						result = Uni.I18n.translate('validation.version.from', 'CFG', 'From') + ' ' + Uni.DateTime.formatDateTimeLong(new Date(startDate));
					} else if (data.endDate) {
						result = Uni.I18n.translate('validation.version.until', 'CFG', 'Until') + ' ' + Uni.DateTime.formatDateTimeLong(new Date(endDate));
					}else {
						result = Uni.I18n.translate('validation.version.notStart', 'CFG', 'Always')
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
