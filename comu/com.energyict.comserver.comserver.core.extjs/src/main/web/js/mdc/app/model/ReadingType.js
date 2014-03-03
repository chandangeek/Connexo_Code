Ext.define('Mdc.model.ReadingType', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'mrid',type:'string',useNull:true}
    ],
    proxy: {
            type: 'rest',
            url: '../../api/dtc/readingtypes'
    }
});
