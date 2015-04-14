Ext.define('Fwc.model.FirmwareStatus', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'string', useNull: true},
        {name: 'localizedValue', type: 'string', useNull: true}
    ]
});