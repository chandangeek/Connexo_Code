Ext.define('Mtr.model.Reading', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'timeStamp', type: 'int'},
        {name: 'recordTime', type: 'int'},
        {name: 'values', type: 'int'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints',
        appendId: false,
        reader: {
            type: 'json',
            root: 'readingInfos'
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
//            url += '/meteractivations/1/channels/1/intervalreadings?from=1357016400000&to=1370059200000';
			var totime = new Date().getTime();
			var fromtime = totime - (3600 * 1000 * 24);
            url += '/readingtypes/0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0/readings?from=' + fromtime + '&to=' + totime;

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