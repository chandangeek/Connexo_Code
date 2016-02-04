Ext.define('Imt.customattributesets.model.CustomAttributeSet', {
    extend: 'Uni.model.Version',

    requires: [
        'Uni.util.LevelMap'
    ],

    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'isVersioned', type: 'boolean', persist: false},
        {name: 'viewPrivileges', type: 'auto', persist: false},
        {name: 'editPrivileges', type: 'auto', persist: false},
        {name: 'properties', type: 'auto', persist: false},
        {name: 'customPropertySetId', type: 'string', persist: false},
        {
            name: 'viewPrivilegesString',
            persist: false,
            mapping: function (data) {
                return Uni.util.LevelMap.getPrivilegesString(data.viewPrivileges);
            }
        },
        {
            name: 'editPrivilegesString',
            persist: false,
            mapping: function (data) {
                return Uni.util.LevelMap.getPrivilegesString(data.editPrivileges);
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations/{mcid}/custompropertysets',
        reader: 'json'
    }
});