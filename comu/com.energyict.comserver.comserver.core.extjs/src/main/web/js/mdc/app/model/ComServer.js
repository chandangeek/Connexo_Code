Ext.define('Mdc.model.ComServer', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'comServerType',
        'name',
        'active',
        'serverLogLevel',
        'communicationLogLevel',
        'queryAPIPostUri',
        'usesDefaultQueryAPIPostUri',
        'eventRegistrationUri',
        'usesDefaultEventRegistrationUri',
        'storeTaskQueueSize',
        'numberOfStoreTaskThreads',
        'storeTaskThreadPriority',
        'changesInterPollDelay',
        'schedulingInterPollDelay',
        'outboundComPorts',
        'inboundComPorts',
        'queryAPIUsername',
        'queryAPIPassword',
        'onlineComServerId'
    ],
    associations: [
        {name: 'changesInterPollDelay',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'changesInterPollDelay'},
        {name: 'schedulingInterPollDelay',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'schedulingInterPollDelay'},
        {name: 'outboundComPorts',type: 'hasMany',model: 'Mdc.model.ComPort',foreignKey: 'comserver_id',associationKey: 'outboundComPorts',
            getTypeDiscriminator:function(node){
                return 'Mdc.model.OutboundComPort';
            }
        },
        {name: 'inboundComPorts',type: 'hasMany',model: 'Mdc.model.ComPort',foreignKey: 'comserver_id',associationKey: 'inboundComPorts',
            getTypeDiscriminator:function(node){
                return 'Mdc.model.InboundComPort';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comservers',
        reader: {
            type: 'json'
        }
    }
});
