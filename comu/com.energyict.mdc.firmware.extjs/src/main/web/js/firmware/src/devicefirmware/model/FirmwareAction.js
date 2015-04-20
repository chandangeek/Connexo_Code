Ext.define('Fwc.devicefirmware.model.FirmwareAction', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'string', useNull: true},
        {name: 'localizedValue', type: 'string', useNull: true}
    ]
});