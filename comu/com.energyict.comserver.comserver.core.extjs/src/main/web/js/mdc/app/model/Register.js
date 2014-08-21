Ext.define('Mdc.model.Register', {
    extend: 'Mdc.model.RegisterConfiguration',
    fields: [
        {name: 'lastReading', type: 'auto', useNull: true},
        {name: 'validationStatus', type:'boolean', useNull: true},
        {name: 'type', type:'string', useNull: true},
        {
            name: 'value',
            useNull: true,
            convert: function(v, record) {
                if(!Ext.isEmpty(record.data.lastReading)) {
                    if(record.data.type == 'billing') {
                        return record.data.lastReading.value + ' ' + record.data.lastReading.unitOfMeasure;
                    }
                    if(record.data.type == 'numerical') {
                        return record.data.lastReading.value + ' ' + record.data.lastReading.unitOfMeasure;
                    }
                    if(record.data.type == 'text') {
                        return record.data.lastReading.value;
                    }
                    if(record.data.type == 'flags') {
                        return record.data.lastReading.value;
                    }
                }

                return '-';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/registers'
    }
});