Ext.define('Dsh.model.OverviewPerCurrentStateInfo', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'displayName' },
        { name: 'alias' },
        { name: 'total', type: 'int' },
        { name: 'counters' }
    ],
    associations: [
        { name: 'counter', type: 'hasMany', model: 'Dsh.model.CounterInfo', associationKey: 'counters' }
    ],
    proxy: {
        type: 'rest',
        url: '../../apps/dashboard/app/fakeData/OverviewPerCurrentStateInfosFake.json',
        reader: {
            type: 'json'
        }
    }
});