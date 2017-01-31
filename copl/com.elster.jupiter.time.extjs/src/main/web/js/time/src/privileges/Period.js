/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Tme.privileges.Period
 *
 * Class that defines privileges for Period
 */
Ext.define('Tme.privileges.Period', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.period',
        'privilege.view.period'],
    admin : ['privilege.administrate.period'],

    all: function() {
        return Ext.Array.merge(Tme.privileges.Period.view);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Tme.privileges.Period.view);
    }
});
