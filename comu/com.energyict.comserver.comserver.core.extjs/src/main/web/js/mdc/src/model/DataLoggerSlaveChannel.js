Ext.define('Mdc.model.DataLoggerSlaveChannel', {

    fields: [
        {name: 'slaveChannel', type: 'auto', defaultValue: null},
        {name: 'dataLoggerChannel', type: 'auto', defaultValue: null}
    ],
    associations: [
        {
            name: 'slaveChannel',
            type: 'hasOne',
            model: 'Mdc.model.Channel',
            associationKey: 'slaveChannel',
            foreignKey: 'slaveChannel'
        },
        {
            name: 'dataLoggerChannel',
            type: 'hasOne',
            model: 'Mdc.model.Channel',
            associationKey: 'dataLoggerChannel',
            foreignKey: 'dataLoggerChannel'
        }
    ]
});