Ext.define('Imt.metrologyconfiguration.model.MetrologyConfiguration', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'version', type: 'number'},        
        {
            name: 'created',
            persist: false,
            mapping: function(data){
                return Uni.DateTime.formatDateTimeLong(new Date(data.createTime));
            }
        },
        {
            name: 'updated',
            persist: false,
            mapping: function(data){
                return Uni.DateTime.formatDateTimeLong(new Date(data.modTime));
            }
        },
    ],
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations/',
        timeout: 240000,
        reader: {
            type: 'json',
 //           root: 'metrologyconfigurations'
        }
    }
});
