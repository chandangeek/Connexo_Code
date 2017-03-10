/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Mdc.privileges.CommunicationSchedule
 *
 * Class that defines privileges for CommunicationSchedule
 */
Ext.define('Mdc.privileges.CommunicationSchedule', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.sharedCommunicationSchedule', 'privilege.view.sharedCommunicationSchedule'],
    admin: ['privilege.administrate.sharedCommunicationSchedule'],
    all: function() {
        return Ext.Array.merge(Mdc.privileges.CommunicationSchedule.view,Mdc.privileges.CommunicationSchedule.admin);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.CommunicationSchedule.view );
    }
});
