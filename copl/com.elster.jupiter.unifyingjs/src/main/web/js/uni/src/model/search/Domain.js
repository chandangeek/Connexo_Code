/**
 * @class Uni.model.search.Domain
 */
Ext.define('Uni.model.search.Domain', {
    extend: 'Ext.data.Model',

    removeDomain: function (url) {
        return url.replace(/http:\/\/.*:\d+(\/.*)/, '$1');
    },

    fields: [
        {name: 'id', type: 'string'},
        {name: 'displayValue', type: 'string'},
        {
            name: 'selfHref', type: 'auto', convert: function (v, record) {
                var linkParams = record.raw.link,
                    result = undefined;

                if (Ext.isArray(linkParams)) {
                    linkParams.forEach(function (linkParam) {
                        if (linkParam.params.rel === 'self') {
                            result = record.removeDomain(linkParam.href);
                            return false;
                        }
                        return true;
                    })
                }

                return result;
            }
        }
        ,
        {
            name: 'glossaryHref', type: 'auto', convert: function (v, record) {
                var linkParams = record.raw.link,
                    result = undefined;

                if (Ext.isArray(linkParams)) {
                    linkParams.forEach(function (linkParam) {
                        if (linkParam.params.rel === 'glossary') {
                            result = record.removeDomain(linkParam.href);
                            return false;
                        }
                        return true;
                    })
                }

                return result;
            }
        },
        {
            name: 'describedByHref', type: 'auto', convert: function (v, record) {
                var linkParams = record.raw.link,
                    result = undefined;

                if (Ext.isArray(linkParams)) {
                    linkParams.forEach(function (linkParam) {
                        if (linkParam.params.rel === 'describedby') {
                            result = record.removeDomain(linkParam.href);
                            return false;
                        }
                        return true;
                    })
                }

                return result;
            }
        }
    ]
});