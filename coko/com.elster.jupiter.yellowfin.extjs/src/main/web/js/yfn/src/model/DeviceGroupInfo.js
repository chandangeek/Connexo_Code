/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Yfn.model.DeviceGroupInfo
 */
Ext.define('Yfn.model.DeviceGroupInfo', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'dynamic',
        {
            name: 'value1',
            type: 'string',
            convert: function(v, record){
                v = record.get('name');
                if(v.indexOf("__##SEARCH_RESULTS##__")!=-1){
                    return Uni.I18n.translate('generatereport.searchResults', 'YFN', 'Search results')
                }
                return Ext.String.htmlEncode(v);
            }
        },
        {
            name: 'value2',
            type: 'string',
            convert: function(v, record){
                return record.get('name');
            }
        }
    ]
});