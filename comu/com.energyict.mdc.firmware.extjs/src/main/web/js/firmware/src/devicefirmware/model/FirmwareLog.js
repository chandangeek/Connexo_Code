/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.model.FirmwareLog', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'string', useNull: true},
        {name: 'timestamp', dateFormat: 'time', type: 'date'},
        {name: 'errorDetails', type: 'string'},
        {name: 'details', type: 'string'},
        {name: 'logLevel', type: 'string'}
    ]
});