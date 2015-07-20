Ext.define('Mdc.model.RegisterData', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type:'number', useNull: true, persist: false},
        {name: 'timeStamp', type:'number', useNull: true},
        {name: 'reportedDateTime', type:'int'},
        {name: 'validationStatus', type:'auto', useNull: true, persist: false},
        {name: 'type', type:'string'},
        {name: 'value', type:'string'},
        {name: 'deltaValue', type:'string'},
        {name: 'dataValidated', type:'auto', persist: false},
        {name: 'suspectReason', type:'auto', persist: false},
        {name: 'validationResult', type:'auto', persist: false},
        {name: 'isConfirmed', type: 'boolean'},
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
