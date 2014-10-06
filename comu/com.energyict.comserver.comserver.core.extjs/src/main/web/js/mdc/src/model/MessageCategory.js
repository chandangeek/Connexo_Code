Ext.define('Mdc.model.MessageCategory', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.Message'
    ],
    fields: [
        { name: 'DeviceMessageCategory', type: 'string' }
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Mdc.model.Message',
            name: 'DeviceMessageEnablements',
            instanceName: 'DeviceMessageEnablements',
            associationKey: 'DeviceMessageEnablements',
            getterName: 'getMessageEnablements',
            setterName: 'setMessageEnablements'
        }
    ],
    proxy: {
        type: 'rest',
//        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/validationrulesets',
        url: '/apps/mdc/fakedata/messages.json',
        reader: {
            type: 'json',
            root: 'categories'
        }
    }
});