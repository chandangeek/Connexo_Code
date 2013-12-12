Ext.define('Mdc.model.ComServer', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'comServerType',type: 'string', useNull:true},
        {name: 'name',type: 'string', useNull:true},
        {name: 'active',type: 'boolean', useNull:true},
        {name: 'serverLogLevel',type: 'string', useNull:true},
        {name: 'communicationLogLevel',type: 'string', useNull:true},
        {name: 'queryAPIPostUri',type: 'string', useNull:true},
        {name: 'usesDefaultQueryAPIPostUri',type: 'string', useNull:true},
        {name: 'eventRegistrationUri',type: 'string', useNull:true},
        {name: 'usesDefaultEventRegistrationUri',type: 'boolean', useNull:true},
        {name: 'storeTaskQueueSize',type: 'int', useNull:true},
        {name: 'numberOfStoreTaskThreads',type: 'int', useNull:true},
        {name: 'storeTaskThreadPriority',type: 'int', useNull:true},
        'changesInterPollDelay',
        'schedulingInterPollDelay',
        'outboundComPorts',
        'inboundComPorts',
        {name: 'queryAPIUsername',type: 'string', useNull:true},
        {name: 'queryAPIPassword',type: 'string', useNull:true},
        {name: 'onlineComServerId',type: 'int', useNull:true},
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
