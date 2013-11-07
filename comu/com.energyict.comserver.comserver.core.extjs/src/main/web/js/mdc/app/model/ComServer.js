Ext.define('Mdc.model.ComServer', {
    extend: 'Ext.data.Model',
    fields: [
        'comServerDescriptor',
        'fullName',
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
        {name: 'changesInterPollDelayCount',mapping: 'changesInterPollDelay.count'},
        {name: 'changesInterPollDelayTimeUnit',mapping: 'changesInterPollDelay.timeUnit'},
        {name: 'schedulingInterPollDelayCount',mapping: 'schedulingInterPollDelay.count'},
        {name: 'schedulingInterPollDelayTimeUnit',mapping: 'schedulingInterPollDelay.timeUnit'}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comservers',
        reader: {
            type: 'json'
        }
    }
});
