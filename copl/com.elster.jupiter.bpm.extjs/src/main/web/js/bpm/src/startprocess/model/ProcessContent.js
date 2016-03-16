Ext.define('Bpm.startprocess.model.ProcessContent', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'status', 'deploymentId', 'businessObject'
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
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/bpm/runtime/processcontent/{id}',
        reader: {
            type: 'json'
        },
        setUrl: function(processId){
            this.url = this.urlTpl.replace('{id}', encodeURIComponent(processId));
        }
    }
});