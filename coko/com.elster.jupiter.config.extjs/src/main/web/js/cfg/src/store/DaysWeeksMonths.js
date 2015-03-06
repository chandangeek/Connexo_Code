Ext.define('Cfg.store.DaysWeeksMonths', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.DayWeekMonth',

    data: [
        {name: 'days', displayValue: 'day(s)'},
        {name: 'weeks', displayValue: 'week(s)'},
        {name: 'months', displayValue: 'month(s)'},
        {name: 'years', displayValue: 'year(s)'}
    ]
});
