Ext.define('Bpm.processes.model.EditProcess', {
    extend: 'Ext.data.Model',
    requires: [
        'Bpm.processes.model.Privilege',

    ],

    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'version',
            type: 'string'
        },
        {
            name: 'type',
            type: 'string'
        },
        {
            name: 'active',
            type: 'string'
        },
        {
            name: 'associatedData',
            persist: false
        },
        {
            name: 'privileges',
            persist: false
        }
    ],
    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        },
        {
            type: 'hasMany',
            model: 'Bpm.processes.model.Privilege',
            associationKey: 'privileges',
            name: 'privileges'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/process',
        reader: {
            type: 'json'
        },
        setUrl: function(url){
            this.url = url;
        }
    }
});