Ext.define('Mdc.model.RegisterReading', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type:'number', useNull: true, persist: false},
        {name: 'timeStamp', type:'number', useNull: true},
        {name: 'reportedDateTime', type:'int'},
        {name: 'validationStatus', type:'auto', useNull: true, persist: false},
        {name: 'type', type:'string'},
        {name: 'value', type:'string'},
        {name: 'unit', type:'string'},
        {name: 'dataValidated', type:'auto', persist: false},
        {name: 'suspectReason', type:'auto', persist: false},
        {name: 'validationResult', type:'auto', persist: false},
        {name: 'isConfirmed', type: 'boolean'},
        {name: 'readingQualities', type: 'auto', defaultValue: null},
        {name: 'slaveRegister', type:'auto', defaultValue: null},
        {name: 'register', type: 'auto'},
        {name: 'dataValidated', type:'auto', persist: false},
        {name: 'interval', type: 'auto'},  // for billing registers
        {
            name: 'valueAndUnit',
            useNull: true,
            convert: function (v, record) {
                if (Ext.isEmpty(record.get('value'))) {
                    return '-';
                }
                if (record.get('type') === 'billing') {
                    return record.get('value') + ' ' + record.get('unit');
                } else if (record.get('type') === 'numerical') {
                    return Uni.Number.formatNumber(record.get('value'), -1) + ' ' + record.get('unit');
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
        }
    ]
});
