Ext.define('Isu.store.DueinTypes', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.DueinType',

    data: [
        {name: 'days', displayValue: 'day(s)'},
        {name: 'weeks', displayValue: 'week(s)'},
        {name: 'months', displayValue: 'month(s)'}
    ]
});
