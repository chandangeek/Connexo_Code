/**
 * @class Uni.override.RestOverride
 *
 * Formats reserved HTML tokens in the url builder.
 */
Ext.define('Uni.override.RestOverride', {
    override: 'Ext.data.proxy.Rest',

    buildUrl: function (request) {
        var me = this,
            operation = request.operation,
            records = operation.records,
            record = Ext.isArray(records) ? records[0] : null,
            id = record ? record.getId() : operation.id;

        // Encodes HTML characters such as '/' and '@'.
        if (typeof id !== 'undefined') {
            operation.id = encodeURIComponent(id);
        }

        operation.records = [];
        var url = me.callParent(arguments);
        operation.records = records;

        var urlTemplate = new Ext.Template(url),
            params = request.proxy.extraParams,
            newUrl = urlTemplate.apply(params);


        //Remove variables embedded into URL
        Ext.Object.each(params, function (key, value) {
            var regex = new RegExp('{' + key + '.*?}');
            /*     if (regex.test(url)) {
             delete params[key];
             }*/
        });

        request.url = url;

        return newUrl;
    }
});