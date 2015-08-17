Ext.define('Mdc.model.ComPortPool', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        'description',
        'active',
        'obsoleteFlag',
        'comPortType',
        'direction',
        'taskExecutionTimeout',
        'discoveryProtocolPluggableClassId',
        'outboundComPorts',
        'inboundComPorts',
        {
            name: 'comportslink',
            persist: false,
            mapping: function (data) {
                var inboundComPorts = data.inboundComPorts ? data.inboundComPorts.length : 0,
                    outboundComPorts = data.outboundComPorts ? data.outboundComPorts.length : 0,
                    comports = inboundComPorts + outboundComPorts;
                return '<a href="#/administration/comportpools/' + data.id + '/comports">' + comports + ' ' + Uni.I18n.translatePlural('comportpool.preview.communicationPorts.count', parseInt(comports), 'MDC', 'communication ports') + '</a>';
            }
        },
        {
            name: 'direction_visual',
            persist: false,
            mapping: function (data) {
                return data.direction;
            }
        }
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
        url: '/api/mdc/comportpools',
        reader: {
            type: 'json'
        }
    }


});
