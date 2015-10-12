/**
 * @class Uni.model.search.Domain
 */
Ext.define('Uni.model.search.Domain', {
    extend: 'Ext.data.Model',

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
                            result = linkParam.href;
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
                            result = linkParam.href;
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
                            result = linkParam.href;
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