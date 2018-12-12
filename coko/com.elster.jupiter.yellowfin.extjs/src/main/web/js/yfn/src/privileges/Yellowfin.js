/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Yfn.privileges.Yellowfin
 *
 * Class that defines privileges for Yellowfin
 */
Ext.define('Yfn.privileges.Yellowfin', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.view.reports'],
    design: ['privilege.design.reports'],
    all: function() {
        return Ext.Array.merge(Yfn.privileges.Yellowfin.view,Yfn.privileges.Yellowfin.design);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Yfn.privileges.Yellowfin.view );
    },
    canDesign:function(){
        return Uni.Auth.checkPrivileges(Yfn.privileges.Yellowfin.design );
    }
});
