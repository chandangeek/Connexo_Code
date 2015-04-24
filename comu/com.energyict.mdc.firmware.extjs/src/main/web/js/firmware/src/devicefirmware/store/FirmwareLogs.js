Ext.define('Fwc.devicefirmware.store.FirmwareLogs', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.devicefirmware.model.FirmwareLog'
    ],
    model: 'Fwc.devicefirmware.model.FirmwareLog',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/comtasks/{firmwareComTaskId}/comtaskexecutionsessions/{firmwareComTaskSessionId}/journals',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'comTaskExecutionSessions',
            totalProperty: 'total'
        },
        setUrl: function (mRID, firmwareComTaskId, firmwareComTaskSessionId) {
            this.url = this.urlTpl.replace('{mRID}', mRID).replace('{firmwareComTaskId}', firmwareComTaskId).replace('{firmwareComTaskSessionId}', firmwareComTaskSessionId);
        }
    }
});
