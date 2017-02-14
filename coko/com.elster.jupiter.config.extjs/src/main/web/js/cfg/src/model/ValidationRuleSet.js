/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.model.ValidationRuleSet', {
    extend: 'Uni.model.Version',
    fields: [
        'id',
        'name',
        'description',
		'numberOfVersions',
		'hasCurrent',
		{			
			name: 'activeVersion',
            persist: false,
            mapping: function (data) {
                var result, startDate, endDate;

				if ((data.numberOfVersions === 0) || (data.hasCurrent === false)){
					result = '-';
				}
				else {
					startDate = data.startDate;
					endDate = data.endDate;
					if (startDate && endDate) {
						result = Uni.I18n.translate('validation.version.display.fromUntil', 'CFG', "From {0} - Until {1}",
							[Uni.DateTime.formatDateTimeShort(new Date(startDate)), Uni.DateTime.formatDateTimeShort(new Date(endDate))],
                            false
						);
					} else if (data.startDate) {
						result = Uni.I18n.translate('validation.version.display.from', 'CFG', "From {0}",
							Uni.DateTime.formatDateTimeShort(new Date(startDate)),
                            false
						);
					} else if (data.endDate) {
						result = Uni.I18n.translate('validation.version.display.until', 'CFG', "Until {0}",
							Uni.DateTime.formatDateTimeShort(new Date(endDate)),
                            false
						);
					}else {
						result = Uni.I18n.translate('general.always', 'CFG', 'Always')
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
