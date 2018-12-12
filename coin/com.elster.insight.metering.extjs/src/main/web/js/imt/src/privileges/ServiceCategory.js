/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.privileges.ServiceCategory', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.view.serviceCategory'],
    all: function() {
        return Ext.Array.merge(Imt.privileges.ServiceCategory.view);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.ServiceCategory.view );
    }
});