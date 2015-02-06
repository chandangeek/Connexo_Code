Ext.define('Apr.model.UnservedMessageService', {
    extend: 'Ext.data.Model',
    fields: [
        'destination', 'subscriber','displayName',
        {
            name: 'messageService',
            persist: false,
            mapping: function (data) {
                return data.displayName;
            }
        }
    ]
});
