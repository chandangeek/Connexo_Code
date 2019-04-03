Ext.define('Itk.model.Group', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'description',
            type: 'text'
        },
        {
            name: 'number',
            type: 'int'
        },
        {
            name: 'href',
            persist: false
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/itk/issues/groupedlist',
        reader: {
            type: 'json',
            root: 'issueGroups'
        }
    }
});