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
        'ruleSet',
        {
            name: 'ruleSetId',
            persist: false,
            convert: function (value, record) {
                return record.data.ruleSet.id;
            }
        },
        {
            name: 'ruleSetName',
            persist: false,
            convert: function (value, record) {
                return record.data.ruleSet.name;
            }
        },
        {
            name: 'rule_name',
            persist: false,
            mapping: function (data) {
                return '<a href="#/administration/validation/rulesets/validationrules/' + data.ruleSet.id + '/ruleoverview/' + data.id + '">' + data.name + '</a>';
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

