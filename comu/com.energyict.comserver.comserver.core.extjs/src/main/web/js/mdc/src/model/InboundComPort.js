Ext.define('Mdc.model.InboundComPort', {
    extend: 'Mdc.model.ComPort',
    fields: [
        {name:'portNumber',type: 'int',useNull: true},
        {name:'bufferSize',type: 'int',useNull: true},
        {name:'comPortPool_id',type: 'int',useNull: true},
        {name:'ringCount',type: 'int',useNull: true},
        {name:'maximumNumberOfDialErrors',type: 'int',useNull: true},
        {name:'atCommandTry',type: 'string',useNull: true},
        {name:'addressSelector',type: 'string',useNull: true},
        {name:'baudrate',type: 'string',useNull: true},
        {name:'nrOfDataBits',type: 'int',useNull: true},
        {name:'nrOfStopBits',type: 'int',useNull: true},
        {name:'flowControl',type: 'string',useNull: true},
        {name:'parity',type: 'string',useNull: true},
        'connectTimeout',
        'delayAfterConnect',
        'delayBeforeSend',
        'atCommandTimeout',
        {name:'contextPath',type: 'string',useNull: true},
        {name:'useHttps',type: 'boolean',useNull: true},
        {name:'keyStoreFilePath',type: 'string',useNull: true},
        {name:'trustStoreFilePath',type: 'string',useNull: true},
        {name:'keyStorePassword',type: 'string',useNull: true},
        {name:'trustStorePassword',type: 'string',useNull: true},
        {name: 'modemInitStrings', type: 'auto',
            mapping: function (data) {
                if (!data.modemInitStrings){
                    return [];
                } else {
                    return data.modemInitStrings;
                }
            }},
        {name: 'globalModemInitStrings', type: 'auto',
            mapping: function (data) {
                if (!data.globalModemInitStrings){
                    return [];
                } else {
                    return data.globalModemInitStrings;
                }
            }}
    ],
    associations: [
        {name: 'connectTimeout',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'connectTimeout'},
        {name: 'delayAfterConnect',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'delayAfterConnect'},
        {name: 'delayBeforeSend',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'delayBeforeSend'},
        {name: 'atCommandTimeout',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'atCommandTimeout'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/mdc/comservers/{comServerId}/comports'
    }
});
