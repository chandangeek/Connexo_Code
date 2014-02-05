Ext.define('Mdc.MdcProxy', {
    override: 'Ext.data.proxy.Rest',

    buildUrl: function (request) {
        //Create a template with the URL and replace the variables
        var me = this,
            url = me.getUrl(request),
            format = me.format;


        if (!url.match(/\/$/)) {
            url += '/';
        }

        if (format) {
            if (!url.match(/\.$/)) {
                url += '.';
            }

            url += format;
        }

        if (me.noCache) {
            url = Ext.urlAppend(url, Ext.String.format("{0}={1}", me.cacheString, Ext.Date.now()));
        }

        var urlTemplate = new Ext.Template(url),
            params = request.proxy.extraParams,
            newUrl = urlTemplate.apply(params);


        //Remove variables embedded into URL
        Ext.Object.each(params, function (key, value) {
            var regex = new RegExp('{' + key + '.*?}');
            if (regex.test(url)) {
                delete params[key];
            }
        });

        request.url = url;

        return newUrl;
    }

});
