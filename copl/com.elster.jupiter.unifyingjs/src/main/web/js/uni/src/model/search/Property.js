/**
 * @class Uni.model.search.Property
 */
Ext.define('Uni.model.search.Property', {
    extend: 'Ext.data.Model',
    removeDomain: function (url) {
        return url.replace(/http:\/\/.*:\d+(\/.*)/, '$1');
    },
    fields: [
        {name: 'name', type: 'string'},
        {name: 'displayValue', type: 'string'},
        {name: 'type', type: 'string'},
        {name: 'exhaustive', type: 'boolean'},
        {name: 'link', type: 'auto'},
        {name: 'selectionMode', type: 'string'},
        {name: 'visibility', type: 'string'},
        {name: 'constrains', type: 'auto'},
        {
            name: 'sticky', type: 'boolean', convert: function (v, record) {
            return record.raw.visibility === 'sticky';
        }
        },
        {
            name: 'linkHref', type: 'auto', convert: function (v, record) {
            var linkParams = record.raw.link,
                result = undefined;

            if(Ext.isDefined(linkParams)) {
                result = record.removeDomain(linkParams.href);
            }

            return result;
        }
        },
        {name: 'constraints', type: 'auto'}
    ]
});