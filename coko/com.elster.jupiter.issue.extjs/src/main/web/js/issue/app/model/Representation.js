Ext.define('Mtr.model.Representation', {
    extend: 'Ext.data.Model',
    fields: [
        'partyId',
        'start',
        'end'
    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Mtr.model.User',
            associationKey: 'userInfo',
            name: 'user',
            reader: {
                type: 'array'
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/prt/parties',
        appendId: false,
        reader: {
            type: 'json',
            root: 'delegates'
        },
        buildUrl: function (request) {
            var me = this,
                format = me.format,
                url = me.getUrl(request),
                id = request.params.id;

            if (!url.match(/\/$/)) {
                url += '/';
            }

            url += id;
            url += '/delegates';

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