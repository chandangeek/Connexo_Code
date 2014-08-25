Ext.define('Dsh.model.CommunicationServerInfo', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'comServerId', type: 'int' },
        { name: 'comServerName', type: 'string' },
        { name: 'comServerType', type: 'string' },
        { name: 'running', type: 'boolean' },
        { name: 'blocked', type: 'boolean' },
        { name: 'blockTime', type: 'auto' }
    ],
    associations: [
        { name: 'blockTime', type: 'hasOne', model: 'Dsh.model.TimeInfo', associationKey: 'blockTime' }
    ],
    proxy: {
        type: 'ajax',
        url: '../../api/dsr/comserverstatussummary',
        reader: {
            type: 'json',
            root: 'comServerStatusInfos'
        }
    }
});