/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Uni.property.store.DynamicComboboxData', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.property.model.DynamicComboboxDataProperty'
    ],
    model: 'Uni.property.model.DynamicComboboxDataProperty',
    storeId: 'DynamicComboboxData',
    proxy: {
        type: 'rest',
        url: '',
        reader: {
            type: 'json'
        },
        setUrl: function (url) {
            this.url = url;
        }
    },
    getPropertiesData: function(){
        var records = this.getRange();
        if (records && records.length){
            var propertiesData = Ext.Array.map(records[0] && records[0].getData() && records[0].getData().propertiesData, function(item){
                item['value'] = item.value || item.name;
                return item;
            })

            if (propertiesData){
                return Ext.create('Ext.data.Store', {
                    fields: ['id', 'name', 'value'],
                    data : propertiesData
                })
            }
        }
        return null;
    }
});
