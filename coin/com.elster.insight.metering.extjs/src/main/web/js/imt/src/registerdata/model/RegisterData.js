Ext.define('Imt.registerdata.model.RegisterData', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
		{name: 'id', type:'number', useNull: true, persist: false},
		{name: 'readingTime', type:'number', useNull: true},
		{name: 'reportedDateTime', type:'number', useNull: true},        
		{name: 'value', type: 'auto'},
		{name: 'deltaValue', type: 'auto'},
		{name: 'validationStatus', type:'auto', useNull: true, persist: false},
		{name: 'dataValidated', type:'auto', persist: false},
		{name: 'validationResult', type:'auto', persist: false},
		{name: 'suspectReason', type:'auto', persist: false},
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
        urlTpl: '/api/udr/usagepoints/{mRID}/registers/{registerId}/data',
        timeout: 240000,
        reader: {
            type: 'json',
  //          root: 'data'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(params.mRID)).replace('{registerId}', params.registerId);
        },
//        pageParam: undefined,
//        startParam: undefined,
//        limitParam: undefined
    }
});

