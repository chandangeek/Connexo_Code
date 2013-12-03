Ext.define('Mdc.model.ComPortPool', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'description',
        'active',
        'obsoleteFlag',
        'type',
        'direction',
        'taskExecutionTimeout',
        'discoveryProtocolPluggableClassId',
        'inboundComPorts',
        'outboundComPorts'
    ],
    associations: [
        {name: 'taskExecutionTimeout',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'taskExecutionTimeout'},
        {name: 'inboundComPorts',type: 'hasMany',model: 'Mdc.model.InboundComPort',foreignKey: 'comPortPool_id',associationKey: 'inboundComPorts',
            getTypeDiscriminator:function(node){
                return 'Mdc.model.InboundComPort';
            }
        },
        {name: 'outboundComPorts',type: 'hasMany',model: 'Mdc.model.OutboundComPort',foreignKey: 'comPortPool_id',associationKey: 'outboundComPorts',
            getTypeDiscriminator:function(node){
                return 'Mdc.model.OutboundComPort';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comportpools',
        reader: {
            type: 'json'
        }
    }


});
