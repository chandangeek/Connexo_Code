Ext.define('Mdc.model.DeviceCommand', {
    extend: 'Mdc.model.DeviceMessageSpec',
    idProperty: 'id',
    fields: [
        {name: 'trackingId', type: 'string', useNull: false},
        {name: 'category', type: 'string', useNull: false},
        {name: 'status', type: 'auto', useNull: true, persist: false},
        {name: 'sentDate', type: 'int', useNull: true},
        {name: 'creationDate', type: 'int', useNull: true},
        {name: 'releaseDate', type: 'int', useNull: true},
        {name: 'user', type: 'string', useNull: false},
        {name: 'errorMessage', type: 'string', useNull: false},
        {name: 'messageSpecification', type: 'auto', useNull: true},
        {
            name: 'preferredComTask',
            persist: false
        },
        {
            name: 'command',
            persist: false,
            mapping: function (data) {
                var res = {};
                data.messageSpecification ? res.name = data.messageSpecification.name : res.name = '';
                res.willBePickedUpByComTask = data.willBePickedUpByComTask;
                res.willBePickedUpByPlannedComTask = data.willBePickedUpByPlannedComTask;
                res.status = data.status ? data.status.value : null;
                return res
            }
        }
    ],
    proxy: {
        type: 'rest',
        pageParam: false,
        timeout: 60000,
        reader: {
            type: 'json',
            root: 'deviceMessages',
            totalProperty: 'total'
        }
    }
});