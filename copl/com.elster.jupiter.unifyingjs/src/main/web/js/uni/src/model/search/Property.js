/**
 * @class Uni.model.search.Property
 */
Ext.define('Uni.model.search.Property', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string'},
        {name: 'displayValue', type: 'string'},
        {name: 'type', type: 'string'},
        {name: 'exhaustive', type: 'boolean'},
        {name: 'link', type: 'auto'},
        {name: 'selectionMode', type: 'string'},
        {name: 'visibility', type: 'string'},
        {name: 'constraints', type: 'auto'},
        {name: 'group', type: 'auto'},
        {
            name: 'groupId', type: 'int', convert: function (v, record) {
            return record.raw.group ? record.raw.group.id : null;
        }
        },
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
                result = linkParams.href;
            }

            return result;
        }
        },
        {name: 'constraints', type: 'auto'}
    ]
});