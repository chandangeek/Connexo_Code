/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Mdc.privileges.MasterData
 *
 * Class that defines privileges for MasterData
 */
Ext.define('Mdc.privileges.MasterData', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.masterData', 'privilege.view.masterData'],
    admin: ['privilege.administrate.masterData'],
    all: function() {
        return Ext.Array.merge(Mdc.privileges.MasterData.view,Mdc.privileges.MasterData.admin);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.MasterData.view );
    }
});
