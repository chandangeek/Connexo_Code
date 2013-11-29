Ext.define('Mdc.model.ComPortPool', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'description',
        'active',
        'obsoleteFlag',
        'type',
        'direction',
        'taskExecutionTimeout'
    ],
    associations: [
        {name: 'taskExecutionTimeout',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'taskExecutionTimeout'},
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comportpools',
        reader: {
            type: 'json'
        }
    }


});
