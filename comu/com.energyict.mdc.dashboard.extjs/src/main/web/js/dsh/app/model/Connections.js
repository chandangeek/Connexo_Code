Ext.define('Dsh.model.Connections', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'connectionSummary', usenull: true }
    ],
    hasMany: [
        {
            model: 'Dsh.model.ConnectionOverview',
            name: 'overviews'
        },
        {
            model: 'Dsh.model.ConnectionBreakdowns',
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