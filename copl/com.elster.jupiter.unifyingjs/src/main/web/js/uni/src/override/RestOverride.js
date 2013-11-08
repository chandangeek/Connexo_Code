/**
 * Formats reserved HTML tokens in the url builder.
 */
Ext.define('Uni.override.RestOverride', {
    override: 'Ext.data.proxy.Rest',

    buildUrl: function (request) {
        var me = this,
            operation = request.operation,
            records = operation.records || [],
            record = records[0],
            format = me.format,
            url = me.getUrl(request),
            id = record ? record.getId() : operation.id;

        if (me.appendId && me.isValidId(id)) {
            if (!url.match(/\/$/)) {
                url += '/';
            }

            id = Ext.String.htmlEncode(id);
            url += id;
        }

        if (format) {
            if (!url.match(/\.$/)) {
                url += '.';
            }

            url += format;
        }

        request.url = url;

        return me.callParent(arguments);
    }
});