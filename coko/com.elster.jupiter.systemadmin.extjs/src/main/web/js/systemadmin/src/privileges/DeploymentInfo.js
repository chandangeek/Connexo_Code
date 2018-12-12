/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Sam.privileges.DeploymentInfo
 *
 * Class that defines privileges for DeploymentInfo
 */
Ext.define('Sam.privileges.DeploymentInfo', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.view.deploymentInfo'],
    all: function() {
        return Ext.Array.merge(Sam.privileges.DeploymentInfo.view);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Sam.privileges.DeploymentInfo.view);
    }
});