Ext.define('Apr.model.ServedMessageService', {
    extend: 'Ext.data.Model',
    fields: [
        'active',
        'subscriberSpec',
        {name: 'numberOfThreads', type: 'int'},
        {
            name: 'messageService',
            persist: false,
            mapping: function (data) {
                return data.subscriberSpec.displayName;
            }
        }
    ]
});