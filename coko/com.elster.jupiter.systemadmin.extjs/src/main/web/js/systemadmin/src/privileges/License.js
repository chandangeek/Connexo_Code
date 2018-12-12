/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Sam.privileges.License
 *
 * Class that defines privileges for License
 */
Ext.define('Sam.privileges.License', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.upload.license', 'privilege.view.license'],
    upload : ['privilege.upload.license'],
    all: function() {
        return Ext.Array.merge(Sam.privileges.License.view, Sam.privileges.License.upload);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Sam.privileges.License.view);
    }
});
