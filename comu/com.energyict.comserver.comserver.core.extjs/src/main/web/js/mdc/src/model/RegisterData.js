Ext.define('Mdc.model.RegisterData', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type:'number', useNull: true, persist: false},
        {name: 'timeStamp', type:'number', useNull: true},
        {name: 'reportedDateTime', type:'date', dateFormat: 'time'},
        {name: 'validationStatus', type:'auto', useNull: true, persist: false},
        {name: 'type', type:'string', useNull: true},
        {name: 'value', type:'string', useNull: true, defaultValue: null},
        {name: 'dataValidated', type:'auto', persist: false},
        {name: 'suspectReason', type:'auto', persist: false},
        {name: 'validationResult', type:'auto', persist: false},
        {
            name: 'modificationState',
            persist: false,
            mapping: function (data) {
                var result = null;
                if (data.modificationFlag && data.reportedDateTime) {
                    result = {
                        flag: data.modificationFlag,
                        date: data.reportedDateTime
                    }
                }
                return result;
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/registers/{registerId}/data',
        timeout: 300000
    }
});
