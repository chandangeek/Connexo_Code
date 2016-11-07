Ext.define('Mdc.model.CommunicationTaskSimple',{
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name:'comTask', type: 'auto'},
        {name:'schedule', type: 'auto'}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/schedules/',
        reader: {
            type: 'json'
        },
        setUrl: function (deviceMRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(deviceMRID));
        }
    }
});
