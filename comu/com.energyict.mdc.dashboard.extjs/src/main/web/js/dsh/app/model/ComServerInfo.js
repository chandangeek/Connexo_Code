Ext.define('Dsh.model.ComServerInfo', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'comServerName' },
        { name: 'comServerType' },
        { name: 'running', type: 'boolean' },
        { name: 'blocked', type: 'boolean' }
    ],
    associations: [
        { name: 'blockTime', type: 'hasOne', model: 'Dsh.model.TimeInfo', associationKey: 'blockTime' }
    ],
    proxy: {
        type: 'rest',
        url: '../../apps/dashboard/app/fakeData/ComServerStatusInfosFake.json',
        reader: {
            type: 'json',
            root: 'comServerStatusInfos',
            totalProperty: 'total'
        }
    }
});