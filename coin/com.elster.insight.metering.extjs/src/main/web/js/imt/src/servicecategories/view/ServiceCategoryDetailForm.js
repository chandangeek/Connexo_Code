/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.servicecategories.view.ServiceCategoryDetailForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.service-category-detail-form',
    layout: 'form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
            name: 'displayName'
        },
        {
            fieldLabel: Uni.I18n.translate('general.meterRoles', 'IMT', 'Meter roles'),
            name: 'meterRoles',
            renderer: function (value) {
                var result = '';

                Ext.Array.each(value, function (role, index) {
                    if (index) {
                        result += '<br>';
                    }
                    if (Ext.isObject(role)) {
                        result += role.name;
                    }
                });

                return result || '-';
            }
        }
    ]
});