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

        me.doRequest(usagePoint, options, url);
    },
    doRequest: function (usagePoint, options, url) {
        var me = this;

        Ext.Ajax.request(Ext.Object.merge(
            {
                url: url,
                method: 'PUT',
                jsonData: Ext.merge(me.getRecordData(), {
                    parent: {
                        id: usagePoint.get('id'),
                        version: usagePoint.get('version')
                    }
                })
            }
            , options));
    },
    estimate: function (usagePoint, options) {
        var me = this,
            url = me.getProxy().url.replace('{usagePointId}', usagePoint.get('name'))
                + '/' + me.getId()
                + '/' + 'estimate';

        me.doRequest(usagePoint, options, url);
    },
    validate: function (usagePoint, options) {
        var me = this,
            url = me.getProxy().url.replace('{usagePointId}', usagePoint.get('name'))
                + '/' + me.getId()
                + '/' + 'validate';

        me.doRequest(usagePoint, options, url);
    }
});