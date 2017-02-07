Ext.define('Mdc.model.RegisterReading', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'string', useNull: true, persist: false},
        {name: 'timeStamp', type: 'number', useNull: true},
        {name: 'reportedDateTime', type: 'int'},
        {name: 'validationStatus', type: 'auto', useNull: true, persist: false},
        {name: 'type', type: 'string'},
        {name: 'value', type: 'string'},
        {name: 'unit', type: 'string'},
        {name: 'calculatedValue', type: 'string'},
        {name: 'calculatedUnit', type: 'string'},
        {name: 'multiplier', type: 'auto'},
        {name: 'dataValidated', type: 'auto', persist: false},
        {name: 'suspectReason', type: 'auto', persist: false},
        {name: 'validationResult', type: 'auto', persist: false},
        {name: 'isConfirmed', type: 'boolean'},
        {name: 'readingQualities', type: 'auto', defaultValue: null},
        {name: 'slaveRegister', type: 'auto', defaultValue: null},
        {name: 'register', type: 'auto'},
        {name: 'dataValidated', type: 'auto', persist: false},
        {name: 'interval', type: 'auto'},  // for billing registers
        {name: 'eventDate', type:'number', useNull: true},
        {name: 'deltaValue', type:'string', useNull: true},
        {
            name: 'valueAndUnit',
            useNull: true,
            convert: function (v, record) {
                if (Ext.isEmpty(record.get('value'))) {
                    return '-';
                }
                if (record.get('type') === 'numerical' || record.get('type') === 'billing') {
                    return Ext.isEmpty(record.get('calculatedValue'))
                        ? Uni.Number.formatNumber(record.get('value'), -1) + ' ' + record.get('unit')
                        : Uni.Number.formatNumber(record.get('calculatedValue'), -1) + ' ' + record.get('calculatedUnit');
                } else if (record.data.type === 'text' || record.data.type === 'flags') {
                    return record.get('value');
                }
            }
        },
        {
            name: 'intervalStart',
            persist: false,
            mapping: 'interval.start',
            type: 'number'
        },
        {
            name: 'intervalEnd',
            persist: false,
            mapping: 'interval.end',
            type: 'number'
        },
        {
            name: 'dataloggerSlavemRID',
            persist: false,
            mapping: 'slaveRegister.mrid',
            type: 'string'
        }
    ]
});
