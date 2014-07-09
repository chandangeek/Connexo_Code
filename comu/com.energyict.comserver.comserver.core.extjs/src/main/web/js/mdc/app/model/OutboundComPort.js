Ext.define('Mdc.model.OutboundComPort', {
    extend: 'Mdc.model.ComPort',
    fields: [
        'outboundComPortPoolIds'
    ],
    proxy: {
        type: 'rest',
        url: '/api/mdc/comservers/{comServerId}/comports'
    }
});
