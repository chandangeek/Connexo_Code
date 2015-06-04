Ext.define('Fwc.firmwarecampaigns.model.Device', {
    extend: 'Ext.data.Model',
    fields: [
        'mrid',
        'status',
        {name: 'startedOn', type: 'date', dateFormat: 'time'},
        {name: 'finishedOn', type: 'date', dateFormat: 'time'}
    ]
});