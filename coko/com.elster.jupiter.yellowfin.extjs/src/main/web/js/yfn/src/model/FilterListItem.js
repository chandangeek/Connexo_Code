/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Yfn.model.FilterInfo
 */
Ext.define('Yfn.model.FilterListItem', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'value1',
            type: 'string',
            convert: function(v, record){
                if(v.indexOf("__##SEARCH_RESULTS##__")!=-1){
                    return Uni.I18n.translate('generatereport.searchResults', 'YFN', 'Search results')
                }
                return Ext.String.htmlEncode(v);
            }
        },
        'value2'
    ]
});