Ext.define('Est.estimationtasks.store.DaysWeeksMonths', {
    extend: 'Ext.data.Store',
    requires: ['Est.estimationtasks.model.DayWeekMonth'],
    model: 'Est.estimationtasks.model.DayWeekMonth',
    data: [
        {name: 'days', displayValue: Uni.I18n.translate('period.days','EST','day(s)')},
        {name: 'weeks', displayValue: Uni.I18n.translate('period.weeks','EST','week(s)')},
        {name: 'months', displayValue: Uni.I18n.translate('period.months','EST','month(s)')},
        {name: 'years', displayValue: Uni.I18n.translate('perios.years','EST','year(s)')}
    ]
});
