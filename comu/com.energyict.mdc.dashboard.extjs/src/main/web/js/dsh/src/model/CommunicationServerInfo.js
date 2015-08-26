Ext.define('Dsh.model.CommunicationServerInfo', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'comServerId', type: 'int' },
        { name: 'comServerName', type: 'string' },
        { name: 'comServerType', type: 'string' },
        { name: 'running', type: 'boolean' },
        { name: 'blocked', type: 'boolean' },
        { name: 'blockTime', type: 'auto' },
        { name: 'status', type: 'string', convert: function (v, record) {
            //Blocked: All communication servers with attributes "running:true" and "blocked:true"
            //Stopped: All communication servers with attribute "running:false"
            //Running: All communication servers with attributes "running:true" and "blocked:false"
            if (record.get('running')) {
                return record.get('blocked')
                    ? Uni.I18n.translate('overview.widget.communicationServer.blocked','DSH','blocked')
                    : Uni.I18n.translate('overview.widget.communicationServer.running','DSH','running');
            } else {
                return Uni.I18n.translate('overview.widget.communicationServer.stopped','DSH','stopped');
            }
        }}
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