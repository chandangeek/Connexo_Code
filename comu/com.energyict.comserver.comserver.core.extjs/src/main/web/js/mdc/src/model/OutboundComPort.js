/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
