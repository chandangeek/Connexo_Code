/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Uni.property.model.DynamicComboboxDataProperty', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.DynamicComboboxData'
    ],
    fields: [
        {
            name: 'propertiesData',
            type: 'auto',
            mapping: function (data) {
                for (var key in data){
                    if (data[key] instanceof Array) return data[key];
                }
            }
        }
    ],
    associations: [
        {
            name: 'propertiesData',
            type: 'hasMany',
            model: 'Uni.property.model.DynamicComboboxData',
            associationKey: 'propertiesData',
            foreignKey: 'propertiesData',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.DynamicComboboxData';
            }
        }
    ]
});