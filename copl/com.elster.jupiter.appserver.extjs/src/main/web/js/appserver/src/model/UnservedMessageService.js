Ext.define('Apr.model.UnservedMessageService', {
    extend: 'Ext.data.Model',
    fields: [
        'destination', 'subscriber',
        {
            name: 'messageService',
            persist: false,
            mapping: function (data) {
                return data.subscriber + ' : ' + data.destination;
            }
        }
    ]
});
