Ext.define('Mtr.model.InRole', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'id', type: 'int', useNull: true},
        'partyId',
        {name: 'roleMRID', mapping: 'partyRoleInfo.roleMRID'},
        'start',
        'end',
        'version'
    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Mtr.model.Role',
            associationKey: 'partyRoleInfo',
            name: 'partyRoleInfo',
            foreignKey: 'roleMRID',
            instanceName: 'roleInstance',
            getterName: 'getRole',
            setterName: 'setRole',
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
            root: 'roles'
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
            url += '/roles';

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