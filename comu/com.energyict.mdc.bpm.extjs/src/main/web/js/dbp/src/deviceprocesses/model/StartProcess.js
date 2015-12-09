Ext.define('Dbp.deviceprocesses.model.StartProcess', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'status', 'deploymentId', 'mrid'
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
        //url: '/api/bpm/runtime/processcontent',
        urlTpl: '/api/bpm/runtime/processcontent/{id}',
        reader: {
            type: 'json'
        },
        setUrl: function(processId, deploymentId){
            this.url = this.urlTpl.replace('{id}', encodeURIComponent(processId));
        }
    }
});