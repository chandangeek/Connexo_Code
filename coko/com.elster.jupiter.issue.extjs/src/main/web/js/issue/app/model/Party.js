Ext.define('Mtr.model.Party', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'mRID',
        'name',
        'aliasName',
        'description',
        'version'
    ],
    associations: [
        {
            type: 'hasOne',
            model: 'Mtr.model.ElectronicAddress',
            associationKey: 'electronicAddress'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/prt/parties',
        reader: {
            type: 'json',
            root: 'parties'
        }
    }
});