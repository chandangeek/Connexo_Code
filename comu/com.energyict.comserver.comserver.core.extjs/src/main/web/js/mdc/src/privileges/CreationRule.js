/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Mdc.privileges.CreationRule
 *
 * Class that defines privileges for CreationRules in MDC
 */
Ext.define('Mdc.privileges.CreationRule', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,

    viewCreationRule:['privilege.view.creationRule'],

    all: function() {
        //return Ext.Array.merge(Mdc.privileges.CreationRule.viewCreationRule);
		return Mdc.privileges.CreationRule.viewCreationRule;
    },

    canViewCreationRule: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.CreationRule.viewCreationRule);
    }
});
