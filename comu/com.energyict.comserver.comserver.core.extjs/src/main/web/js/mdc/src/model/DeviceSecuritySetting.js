Ext.define('Mdc.model.DeviceSecuritySetting', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property',
        'Mdc.model.ExecutionLevel'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'authenticationLevel', type: 'auto', useNull: true},
        {name: 'encryptionLevel', type: 'auto', useNull: true},
        {name: 'status', type: 'auto', useNull: true},
        {name: 'userHasEditPrivilege', type: 'boolean', useNull: true},
        {name: 'userHasViewPrivilege', type: 'boolean', useNull: true}
    ],

    associations: [
        {name: 'executionLevels', type: 'hasMany', model: 'Mdc.model.ExecutionLevel', associationKey: 'executionLevels',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.ExecutionLevel';
            }
        },
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mrid}/securityproperties'
    }
});