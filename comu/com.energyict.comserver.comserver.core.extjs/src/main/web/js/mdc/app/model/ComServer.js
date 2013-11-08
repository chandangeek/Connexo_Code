Ext.define('Mdc.model.ComServer', {
    extend: 'Ext.data.Model',
    fields: [
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
        {name: 'changesInterPollDelay',type: 'hasOne',model:'Mdc.model.TimeInfo',associationKey: 'changesInterPollDelay'},
        {name: 'schedulingInterPollDelay',type: 'hasOne',model:'Mdc.model.TimeInfo',associationKey: 'schedulingInterPollDelay'},
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comservers',
        reader: {
            type: 'json'
        }
    }
});
