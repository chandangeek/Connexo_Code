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
			localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 1, 'MDC', '{0} years', '{0} year', '{0} years')
		},
		{
			id: '3months',
			count: 3,
			timeUnit: 'months',
			localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 3, 'MDC', '{0} months', '{0} month', '{0} months')
		},
		{
			id: '2months',
			count: 2,
			timeUnit: 'months',
			localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 2, 'MDC', '{0} months', '{0} month', '{0} months')
		},
		{
			id: '1months',
			count: 1,
			timeUnit: 'months',
			localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', '{0} months', '{0} month', '{0} months')
		},
		{
			id: '3weeks',
			count: 3,
			timeUnit: 'weeks',
			localizeValue: Uni.I18n.translatePlural('general.timeUnit.weeks', 3, 'MDC', '{0} weeks', '{0} week', '{0} weeks')
		},
		{
			id: '2weeks',
			count: 2,
			timeUnit: 'weeks',
			localizeValue: Uni.I18n.translatePlural('general.timeUnit.weeks', 2, 'MDC', '{0} weeks', '{0} week', '{0} weeks')
		},
		{
			id: '1weeks',
			count: 1,
			timeUnit: 'weeks',
			localizeValue: Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'MDC', '{0} weeks', '{0} week', '{0} weeks')
		}
	]
});