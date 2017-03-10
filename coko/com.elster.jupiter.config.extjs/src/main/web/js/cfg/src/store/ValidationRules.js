/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.ValidationRules', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Cfg.model.ValidationRule',
    pageSize: 10,

    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        appendId: false,
        reader: {
            type: 'json',
            root: 'rules',
            totalProperty: 'total'
        },
        buildUrl: function (request) {
            var me = this,
                format = me.format,
                url = me.getUrl(request),
                ruleSetId = request.params.ruleSetId,
                versionId = request.params.versionId;

            if (!url.match(/\/$/)) {
                url += '/';
            }

            url += ruleSetId;
            url += '/versions/';
            url += versionId;
            url += '/rules';

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
