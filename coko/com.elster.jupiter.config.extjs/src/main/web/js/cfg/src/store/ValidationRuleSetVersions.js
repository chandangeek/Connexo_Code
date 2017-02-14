/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.ValidationRuleSetVersions', {
    //extend: 'Ext.data.Store',
    extend: 'Uni.data.store.Filterable',
    model: 'Cfg.model.ValidationRuleSetVersion',
    pageSize: 10,

    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        reader: {
            type: 'json',
            root: 'versions'           
        },
        buildUrl: function (request) {
            var me = this,
                format = me.format,
                url = me.getUrl(request),
                id = request.params.ruleSetId;

            if (!url.match(/\/$/)) {
                url += '/';
            }

            url += id;            
            url += '/versions';


            if (format) {
                if (!url.match(/\.$/)) {
                    url += '.';
                }

                url += format;
            }

            if (me.noCache) {
                url = Ext.urlAppend(url, Ext.String.format("{0}={1}", me.cacheString, Ext.Date.now()));
            }

            request.url = url;

            return url;
        }
    }
});

