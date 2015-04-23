Ext.define('Mdc.store.ValidationResultsDurations', {
    extend: 'Ext.data.Store',
    //model: 'Mdc.model.DataIntervalAndZoomLevels',
	model: 'Mdc.model.LoadProfileDataDuration',
	
 proxy: {
        type: 'memory'
		},
    data:  [
                {
                    id: '2months',
                    count: 2,
                    timeUnit: 'months',
                    localizeValue: 2 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 2, 'MDC', 'month')
                },
                {
                    id: '1months',
                    count: 1,
                    timeUnit: 'months',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', 'month')
                },
                {
                    id: '2weeks',
                    count: 2,
                    timeUnit: 'weeks',
                    localizeValue: 2 + ' ' + Uni.I18n.translatePlural('general.timeUnit.weeks', 2, 'MDC', 'weeks')
                },
                {
                    id: '1weeks',
                    count: 1,
                    timeUnit: 'weeks',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'MDC', 'week')
                }
            ]
});