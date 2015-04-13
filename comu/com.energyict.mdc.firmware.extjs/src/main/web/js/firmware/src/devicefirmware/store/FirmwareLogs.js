Ext.define('Fwc.devicefirmware.store.FirmwareLogs', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.devicefirmware.model.FirmwareLog'
    ],
    model: 'Fwc.devicefirmware.model.FirmwareLog',
    autoLoad: false,

    data: [
        {
            id: '1',
            description: 'Desc',
            timestamp: '1428922620000',
            details: 'details',
            level: 'level'
        }
    ]

//    proxy: {
//        type: 'rest',
//        urlTpl: '/api/fwc/device/{mRID}/firmwareslogs',
//        reader: {
//            type: 'json',
//            root: 'firmwarelogs',
//            totalProperty: 'total'
//        },
//        setUrl: function (mRID) {
//            this.url = this.urlTpl.replace('{mRID}', mRID);
//        }
//    }
});
