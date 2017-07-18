/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.model.RelativePeriod', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        'from',
        'to',
        'categories',
        {
            name: 'listOfCategories',
            persist: false,
            mapping: function (data) {
                var arr = data.categories;
                if (!Ext.isEmpty(arr)) {
                    var str = '';
                    Ext.Array.each(arr, function (item) {
                        if (item !== arr[arr.length - 1]) {
                            str += item.name + ', ';
                        } else {
                            str += item.name;
                        }
                    });
                    return str;
                } else {
                    return ''
                }
            }
        }
    ],

    idProperty: 'id',
    associations: [
        {
            name: 'categories',
            type: 'hasMany',
            model: 'Uni.property.model.Categories',
            associationKey: 'categories',
            getterName: 'getCategories',
            setterName: 'setCategories',
            foreignKey: 'relativePeriodCategories'
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/tmr/relativeperiods'
    }
});
