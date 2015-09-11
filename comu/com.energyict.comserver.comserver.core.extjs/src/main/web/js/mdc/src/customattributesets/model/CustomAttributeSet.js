Ext.define('Mdc.customattributesets.model.CustomAttributeSet', {
    extend: 'Ext.data.Model',

    requires: [
        'Mdc.customattributesets.service.LevelMap'
    ],

    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'domainName', type: 'string'},
        {name: 'isRequired', type: 'boolean'},
        {name: 'isVersioned', type: 'boolean'},
        {name: 'status', type: 'boolean'},
        {name: 'viewPrivileges', type: 'auto'},
        {name: 'editPrivileges', type: 'auto'},
        {name: 'defaultViewPrivileges', type: 'auto'},
        {name: 'defaultEditPrivileges', type: 'auto'},
        {name: 'attributes', type: 'auto'},
        {
            name: 'viewPrivilegesString',
            persist: false,
            mapping: function (data) {
                return Mdc.customattributesets.service.LevelMap.getPrivilegesString(data.viewPrivileges);
            }
        },
        {
            name: 'editPrivilegesString',
            persist: false,
            mapping: function (data) {
                return Mdc.customattributesets.service.LevelMap.getPrivilegesString(data.editPrivileges);
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/mds/customattributesets',
        reader: {
            type: 'json'
        }
    }
});
