Ext.define('Imt.usagepointsetup.model.Device', {
    extend: 'Ext.data.Model',
    fields: [
        'mRID',
        'id',
        'version',
        'name',
        {name: 'meterActivations', type: 'auto'}
    ],
    idProperty: 'name'
});