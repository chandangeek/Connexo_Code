Ext.define('Dxp.store.DaysWeeksMonths', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.DayWeekMonth',

    data: [
        {name: 'days', displayValue: 'day(s)'},
        {name: 'weeks', displayValue: 'week(s)'},
        {name: 'months', displayValue: 'month(s)'},
        {name: 'years', displayValue: 'year(s)'}
    ]
});
