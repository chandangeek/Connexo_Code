Ext.define('Imt.customattributesets.model.CustomAttributeSet', {
    extend: 'Ext.data.Model',

    requires: [
        'Uni.util.LevelMap'
    ],

    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'isVersioned', type: 'boolean'},
        {name: 'viewPrivileges', type: 'auto'},
        {name: 'editPrivileges', type: 'auto'},
        {name: 'attributes', type: 'auto'},
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
    ]
});