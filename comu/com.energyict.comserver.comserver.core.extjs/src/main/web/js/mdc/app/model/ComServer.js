Ext.define('Mdc.model.ComServer', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'comServerDescriptor',
        'name',
        'active',
        'serverLogLevel',
        'communicationLogLevel',
        'queryAPIPostUri',
        'usesDefaultQueryAPIPostUri',
        'eventRegistrationUri',
        'usesDefaultEventRegistrationUri',
        'storeTaskQueueSize',
        'numberOfStoreTaskThreads',
        'storeTaskThreadPriority',
        'changesInterPollDelay',
        'schedulingInterPollDelay'
    ],
    associations: [
        {name: 'changesInterPollDelay',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'changesInterPollDelay'},
        {name: 'schedulingInterPollDelay',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'schedulingInterPollDelay'},
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comservers',
        reader: {
            type: 'json'
        }
    }
});
