Ext.define('Cfg.store.ValidationPropertySpecsForRule', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Cfg.model.ValidationPropertySpec',
    storeId: 'validationPropertySpec',

    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        appendId: false,
        reader: {
            type: 'json',
            root: 'propertySpecs'
        }  ,
        buildUrl: function (request) {
            var me = this,
                format = me.format,
                url = me.getUrl(request),
                id = request.params.id;

            if (!url.match(/\/$/)) {
                url += '/';
            }

            url += 'propertyspecs';
            //url += 'propertyspecsforrule/';
            //url += id;

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

})
