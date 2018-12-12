/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesets.model.CustomAttributeSet', {
    extend: 'Uni.model.Version',
    idProperty: 'customPropertySetId',
    requires: [
        'Uni.util.LevelMap'
    ],

    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'isVersioned', type: 'boolean'},
        {name: 'viewPrivileges', type: 'auto'},
        {name: 'editPrivileges', type: 'auto'},
        {name: 'properties', type: 'auto'},
        {name: 'customPropertySetId', type: 'string'},
        {name: 'parent', type: 'auto', useNull: true, defaultValue: null},
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