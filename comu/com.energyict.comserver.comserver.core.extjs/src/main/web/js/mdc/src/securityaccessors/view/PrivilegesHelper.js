/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.PrivilegesHelper', {
    singleton: true,

    requires: [
        'Uni.Auth'
    ],

    hasPrivileges: function(levelObjectsArray) {
        if (Ext.isEmpty(levelObjectsArray)) {
            return false;
        }
        var levels = [];
        Ext.Array.each(levelObjectsArray, function(levelObject) {
            levels.push(levelObject.id);
        });
        return Uni.Auth.checkPrivileges(levels);
    }

});
