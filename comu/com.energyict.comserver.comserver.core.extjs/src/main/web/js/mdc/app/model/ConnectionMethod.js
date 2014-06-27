Ext.define('Mdc.model.ConnectionMethod', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'direction', type: 'string', useNull: true},
        {name: 'allowSimultaneousConnections', type: 'boolean', useNull: true},
        {name: 'isDefault', type: 'boolean', useNull: true},
        {name: 'comPortPool', type: 'string', useNull: true},
        {name: 'connectionType', type: 'string', useNull: true},
        {name: 'connectionStrategy', type: 'string', useNull: true},
        'rescheduleRetryDelay',
        {name: 'nextExecutionSpecs', useNull: true},
        {name: 'comWindowStart',type: 'int',useNull: true},
        {name: 'comWindowEnd',type: 'int',useNull: true}
    ],
    associations: [
        {name: 'rescheduleRetryDelay',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'rescheduleRetryDelay'},
        {name: 'properties', type: 'hasMany', model: 'Mdc.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/connectionmethods',
//        writer:{
//            type:'json',
//            getRecordData: function(record, operation) {
//                debugger;
//                var ampm = record.data.comWindowStart.substring(7,5);
//                var time = record.data.comWindowStart.substring(0,5).split(':');
//                record.data.comWindowStart = time[0]*3600+time[1]*60;
//                if(ampm === 'PM')record.data.comWindowStart+=43200;
//
//                ampm = record.data.comWindowEnd.substring(7,5);
//                time = record.data.comWindowEnd.substring(0,5).split(':');
//                record.data.comWindowEnd = time[0]*3600+time[1]*60;
//                if(ampm === 'PM')record.data.comWindowEnd+=43200;
//                return record.data;
//            }
//        },
//        reader: {
//            type: 'json',
//            getData:function(data){
//                var d1 = new Date();
//                d1.setHours(data.comWindowStart/3600);
//                d1.setMinutes((data.comWindowStart%3600)/60);
//                data.comWindowStart = d1;
//                var d2 = new Date();
//                d2.setHours(data.comWindowEnd/3600);
//                d2.setMinutes((data.comWindowEnd%3600)/60);
//                data.comWindowEnd = d2;
//                return data;
//            }
//        }
    }
});