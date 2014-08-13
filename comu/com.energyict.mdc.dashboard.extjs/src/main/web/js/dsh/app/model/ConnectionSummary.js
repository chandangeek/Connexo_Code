Ext.define('Dsh.model.ConnectionSummary', {
    extend: 'Ext.data.Model',
    hasOne: {
        model: 'Dsh.model.ConnectionSummaryData',
        associationKey: 'summary',
        name: 'summary',
        getterName: 'getSummary',
        setterName: 'setSummary'
    },
    hasMany: [
        {
            model: 'Dsh.model.ConnectionOverview',
            name: 'overviews'
        },
        {
            model: 'Dsh.model.ConnectionBreakdown',
            name: 'breakdowns'
        }
    ],
    proxy: {
        type: 'ajax',
        url: '../../apps/dashboard/app/fakeData/ConnectionsOverviewFake.json',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});