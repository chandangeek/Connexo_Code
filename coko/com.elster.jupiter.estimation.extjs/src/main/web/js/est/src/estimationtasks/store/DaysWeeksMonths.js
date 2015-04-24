Ext.define('Est.estimationtasks.store.DaysWeeksMonths', {
    extend: 'Ext.data.Store',
    requires: ['Est.estimationtasks.model.DayWeekMonth'],
    model: 'Est.estimationtasks.model.DayWeekMonth',
    data: [
        {name: 'days', displayValue: 'day(s)'},
        {name: 'weeks', displayValue: 'week(s)'},
        {name: 'months', displayValue: 'month(s)'},
        {name: 'years', displayValue: 'year(s)'}
    ]
});
