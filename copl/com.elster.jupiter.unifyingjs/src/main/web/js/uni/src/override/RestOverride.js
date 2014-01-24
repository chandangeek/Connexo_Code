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
            records = operation.records || [],
            record = records[0],
            id = record ? record.getId() : operation.id;

        // Encodes HTML characters such as '/' and '@'.
        if (typeof id !== 'undefined') {
            id = encodeURIComponent(id);

            if (record) {
                record.setId(id);
            } else {
                operation.id = id;
            }
        }

        return me.callParent(arguments);
    }
});