Ext.define('Isu.store.TimeTypes', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.TimeTypes',

    data: [
        {name: 'seconds', displayValue: 'seconds'},
        {name: 'minutes', displayValue: 'minutes'},
        {name: 'hours', displayValue: 'hours'},
        {name: 'days', displayValue: 'days'},
        {name: 'weeks', displayValue: 'weeks'},
        {name: 'months', displayValue: 'months'}
    ]
});
