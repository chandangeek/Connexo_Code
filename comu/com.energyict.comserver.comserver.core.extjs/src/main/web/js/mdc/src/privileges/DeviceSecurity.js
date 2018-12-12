/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Mdc.privileges.DeviceSecurity
 *
 * Class that defines privileges for DeviceSecurity
 */
Ext.define('Mdc.privileges.DeviceSecurity', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,

    viewLevels:['view.device.security.properties.level1','view.device.security.properties.level2','view.device.security.properties.level3','view.device.security.properties.level4'],
    editLevels:['edit.device.security.properties.level1','edit.device.security.properties.level2','edit.device.security.properties.level3','edit.device.security.properties.level4'],
    viewOrEditLevels : ['view.device.security.properties.level1','view.device.security.properties.level2','view.device.security.properties.level3','view.device.security.properties.level4',
        'edit.device.security.properties.level1','edit.device.security.properties.level2','edit.device.security.properties.level3','edit.device.security.properties.level4'],
    level1:['edit.device.security.properties.level1'],
    level2:['edit.device.security.properties.level2'],
    level3:['edit.device.security.properties.level3'],
    level4:['edit.device.security.properties.level4'],

    all: function() {
        return Ext.Array.merge(Mdc.privileges.DeviceSecurity.viewLevels,
            Mdc.privileges.DeviceSecurity.editLevels
        );
    }

});
