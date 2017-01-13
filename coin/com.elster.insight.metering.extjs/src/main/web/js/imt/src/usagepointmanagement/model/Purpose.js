Ext.define('Imt.usagepointmanagement.model.Purpose', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'required', type: 'boolean', useNull: true},
        {name: 'active', type: 'boolean', useNull: true},
        {name: 'status', type: 'auto', useNull: true},
        {name: 'validationInfo', defaultValue: null}
    ],
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes',
        reader: {
            type: 'json'
        }
    },
    triggerActivation: function (usagePoint, options) {
        var me = this,
            url = me.getProxy().url.replace('{usagePointId}', usagePoint.get('name'))
                + '/' + me.getId()
                + '/' + (me.get('active') ? 'deactivate' : 'activate');

        Ext.Ajax.request(Ext.Object.merge(
            {
                url: url,
                method: 'PUT',
                jsonData: {
                    parent: {
                        id: usagePoint.get('id'),
                        version: usagePoint.get('version')
                    }
                }
            }
            , options));
    }
});