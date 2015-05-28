Ext.define('Mdc.model.ValidationBlock', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'startTime',type:'number',useNull:true},
        {name: 'endTime', type: 'string', useNull: true},
        {name: 'amountOfSuspects', type: 'string', useNull: true}
    ]
});
