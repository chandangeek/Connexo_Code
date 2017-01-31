/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.WorkgroupUser', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        'active',
        {
            name: 'statusDisplay',
            persist: false,
            mapping: function (data) {
                if (data.active) {
                    return Uni.I18n.translate('general.active', 'USR', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'USR', 'Inactive');
                }
            }
        }
    ]
});