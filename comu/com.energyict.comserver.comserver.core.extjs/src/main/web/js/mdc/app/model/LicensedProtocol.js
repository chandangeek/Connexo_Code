Ext.define('Mdc.model.LicensedProtocol', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'licensedProtocolRuleCode', type: 'int', useNull: true},
        'protocolName',
        'protocolJavaClassName',
        'protocolFamilies'
    ],
    associations: [
        {name: 'protocolFamilies', type: 'hasMany', model: 'Mdc.model.ProtocolFamily', associationKey: 'protocolFamilies',
            getTypeDiscriminator:function(node){
                return 'Mdc.model.ProtocolFamily';
            }
        }
    ],
    idProperty: 'licensedProtocolRuleCode',
    proxy: {
        type: 'rest',
        url: '../../api/mdc/licensedprotocols',
        reader: {
            type: 'json'
        }
    }
});