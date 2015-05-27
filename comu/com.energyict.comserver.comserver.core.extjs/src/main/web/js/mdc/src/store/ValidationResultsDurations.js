Ext.define('Mdc.store.ValidationResultsDurations', {
    extend: 'Ext.data.Store',    
	model: 'Mdc.model.LoadProfileDataDuration',	
	proxy: {
        type: 'memory'
	},
    data:  [
		{
			id: '1years',
			count: 1,
			timeUnit: 'years',
			localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 1, 'MDC', 'year')
		},
		{
			id: '3months',
			count: 3,
			timeUnit: 'months',
			localizeValue: 3 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 3, 'MDC', 'month')
		},
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
			id: '3weeks',
			count: 3,
			timeUnit: 'weeks',
			localizeValue: 3 + ' ' + Uni.I18n.translatePlural('general.timeUnit.weeks', 3, 'MDC', 'weeks')
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