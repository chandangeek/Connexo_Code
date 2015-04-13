Ext.define('Fwc.devicefirmware.model.FirmwareLog', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'string', useNull: true},
        {name: 'timestamp', dateFormat: 'time', type: 'date'},
        {name: 'description', type: 'string'},
        {name: 'details', type: 'string'},
        {name: 'level', type: 'string'}
    ]
});