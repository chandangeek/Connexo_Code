Ext.define('Dxp.store.DaysWeeksMonths', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.DayWeekMonth',

    data: [
        {name: 'days', displayValue: Uni.I18n.translate('period.days','DES','day(s)')},
        {name: 'weeks', displayValue: Uni.I18n.translate('period.weeks','DES','week(s)')},
        {name: 'months', displayValue: Uni.I18n.translate('period.months','DES','month(s)')},
        {name: 'years', displayValue: Uni.I18n.translate('perios.years','DES','year(s)')}
    ]
});
