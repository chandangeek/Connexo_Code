Ext.define('Cps.customattributesets.model.CustomAttributeSet', {
    extend: 'Uni.model.ParentVersion',

    requires: [
        'Uni.util.LevelMap'
    ],

    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'domainName', type: 'string'},
        {name: 'isRequired', type: 'boolean'},
        {name: 'isVersioned', type: 'boolean'},
        {name: 'viewPrivileges', type: 'auto'},
        {name: 'editPrivileges', type: 'auto'},
        {name: 'defaultViewPrivileges', type: 'auto'},
        {name: 'defaultEditPrivileges', type: 'auto'},
        {name: 'properties', type: 'auto'},
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
        url: '/api/cps/custompropertysets',
        reader: {
            type: 'json'
        }
    }
});
