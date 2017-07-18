/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ComPortPool', {
    extend: 'Uni.model.Version',
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
        'properties',
        {
            name: 'comportslink',
            persist: false,
            mapping: function (data) {
                var inboundComPorts = data.inboundComPorts ? data.inboundComPorts.length : 0,
                    outboundComPorts = data.outboundComPorts ? data.outboundComPorts.length : 0,
                    comports = inboundComPorts + outboundComPorts;
                return '<a href="#/administration/comportpools/' + data.id + '/comports">'
                    + Uni.I18n.translatePlural('comportpool.preview.communicationPorts.count', parseInt(comports), 'MDC',
                        'No communication ports', '{0} communication port', '{0} communication ports')
                    + '</a>';
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
        {name: 'inboundComPorts',type: 'hasMany',model: 'Mdc.model.InboundComPort',associationKey: 'inboundComPorts',
            getTypeDiscriminator:function(node){
                return 'Mdc.model.InboundComPort';
            }
        },
        {name: 'outboundComPorts',type: 'hasMany',model: 'Mdc.model.OutboundComPort',foreignKey: 'comPortPool_id',associationKey: 'outboundComPorts',
            getTypeDiscriminator:function(node){
                return 'Mdc.model.OutboundComPort';
            }
        },
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
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
