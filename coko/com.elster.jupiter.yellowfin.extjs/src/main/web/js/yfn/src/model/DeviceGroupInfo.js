/**
 * @class Yfn.model.DeviceGroupInfo
 */
Ext.define('Yfn.model.DeviceGroupInfo', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'dynamic',
        {
            name: 'value1',
            type: 'string',
            convert: function(v, record){
                return record.get('name');
            }
        },
        {
            name: 'value2',
            type: 'string',
            convert: function(v, record){
                return record.get('name');
            }
        }
    ]
});