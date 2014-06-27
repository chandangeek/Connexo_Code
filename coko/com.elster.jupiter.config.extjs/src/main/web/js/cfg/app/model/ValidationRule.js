Ext.define('Cfg.model.ValidationRule', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'active',
        'implementation',
        'displayName',
        'readingTypes',
        'properties',
        'name',
        'ruleSetId',
        {
            name: 'ruleSetName',
            persist: false
        },
        {
            name: 'rule_name',
            persist: false,
            mapping: function (data) {
                return '<a href="#/administration/validation/rulesets/validationrules/' + data.ruleSetId + '/ruleoverview/' + data.id + '">' + data.name + '</a>';
            }
        },
        {
            name: 'reading_type_definition',
            persist: false,
            mapping: function (data) {
                var str = '';
                Ext.Array.each(data.readingTypes, function (item) {
                    str += item.mRID + '&nbsp;&nbsp;&nbsp;&nbsp;<span style="color:grey"><i>' + item.aliasName + '</i></span><br><br>';
                });
                return str;
            }
        },
        {
            name: 'properties_minimum',
            persist: false,
            mapping: function (data) {
                var str = '';
                if (!Ext.isEmpty(data.properties)) {
                Ext.Array.each(data.properties, function (item) {
                    if (item.name === 'minimum') {
                    str = item.value;
                    }
                });
                }
                return str;
            }
        },
        {
            name: 'properties_maximum',
            persist: false,
            mapping: function (data) {
                var str = '';
                if (!Ext.isEmpty(data.properties)) {
                    Ext.Array.each(data.properties, function (item) {
                        if (item.name === 'maximum') {
                            str = item.value;
                        }
                    });
                }
                return str;
            }
        },
        {
            name: 'properties_consequtive',
            persist: false,
            mapping: function (data) {
                var str = '';
                if (!Ext.isEmpty(data.properties)) {
                    Ext.Array.each(data.properties, function (item) {
                        if (item.name === 'NumberOfConsecutiveZerosAllowed') {
                            str = item.value;
                        }
                    });
                }
                return str;
            }
        }
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Cfg.model.ValidationRuleProperty',
            associationKey: 'properties',
            name: 'properties'
        },
        {
            type: 'hasMany',
            model: 'Cfg.model.ReadingType',
            associationKey: 'readingTypes',
            name: 'readingTypes'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        appendId: false,
        reader: {
            type: 'json',
            root: 'rules'
        },
        buildUrl: function (request) {
            var me = this,
                format = me.format,
                url = me.getUrl(request),
                id = request.params.id;

            if (!url.match(/\/$/)) {
                url += '/';
            }

            url += 'rules/';
            url += id;

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

