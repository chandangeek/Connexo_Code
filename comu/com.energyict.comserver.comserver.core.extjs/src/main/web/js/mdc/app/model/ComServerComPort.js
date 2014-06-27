Ext.define('Mdc.model.ComServerComPort', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest',
        'Mdc.store.ComPortPools',
        'Mdc.store.BaudRates',
        'Mdc.store.Parities',
        'Mdc.store.TimeUnits',
        'Mdc.store.LogLevels',
        'Mdc.store.ComPortTypes',
        'Mdc.store.FlowControls',
        'Mdc.store.NrOfDataBits',
        'Mdc.store.NrOfStopBits'
    ],
    fields: [
        {
            name: 'name',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'direction',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'comPortType',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'active',
            type: 'boolean',
            defaultValue: '-'
        },
        {
            name: 'comServerType',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'serverLogLevel',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'communicationLogLevel',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'changesInterPollDelay',
            type: 'auto',
            defaultValue: '-'
        },
        {
            name: 'schedulingInterPollDelay',
            type: 'auto',
            defaultValue: '-'
        },
        {
            name: 'inboundComPorts',
            type: 'auto',
            defaultValue: '-'
        },
        {
            name: 'type',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'bound',
            type: 'boolean',
            defaultValue: '-'
        },
        {
            name: 'comServer_id',
            type: 'int',
            defaultValue: '-'
        },
        {
            name: 'description',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'modificationDate',
            type: 'date',
            dateFormat: 'time',
            defaultValue: '-'
        },
        {
            name: 'numberOfSimultaneousConnections',
            type: 'int',
            defaultValue: '-'
        },
        {
            name: 'comPortPool_id',
            type: 'int',
            defaultValue: '-'
        },
        {
            name: 'portNumber',
            type: 'int',
            defaultValue: '-'
        },
        {
            name: 'atCommandTimeout',
            type: 'string',
            defaultValue: '-',
            mapping: function (data) {
                var val = data.atCommandTimeout;
                return val ? val.count ? val.count + (val.timeUnit ? ' ' + val.timeUnit : '') : undefined : undefined;
            }
        },
        {
            name: 'atCommandTry',
            type: 'int',
            defaultValue: '-'
        },
        {
            name: 'baudrate',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'connectTimeout',
            type: 'string',
            defaultValue: '-',
            mapping: function (data) {
                var val = data.connectTimeout;
                return val ? val.count ? val.count + (val.timeUnit ? ' ' + val.timeUnit : '') : undefined : undefined;
            }
        },
        {
            name: 'delayAfterConnect',
            type: 'string',
            defaultValue: '-',
            mapping: function (data) {
                var val = data.delayAfterConnect;
                return val ? val.count ? val.count + (val.timeUnit ? ' ' + val.timeUnit : '') : undefined : undefined;
            }
        },
        {
            name: 'delayBeforeSend',
            type: 'string',
            defaultValue: '-',
            mapping: function (data) {
                var val = data.delayBeforeSend;
                return val ? val.count ? val.count + (val.timeUnit ? ' ' + val.timeUnit : '') : undefined : undefined;
            }
        },
        {
            name: 'flowControl',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'maximumNumberOfDialErrors',
            type: 'int',
            defaultValue: '-'
        },
        {
            name: 'nrOfDataBits',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'nrOfStopBits',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'parity',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'ringCount',
            type: 'int',
            defaultValue: '-'
        },
        {
            name: 'contextPath',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'keyStoreFilePath',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'keyStorePassword',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'trustStoreFilePath',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'trustStorePassword',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'useHttps',
            type: 'string',
            defaultValue: '-'
        },
        {
            name: 'addressSelector',
            type: 'auto',
            defaultValue: '-'
        },
        {
            name: 'status',
            type: 'string',
            defaultValue: '-',
            mapping: function (data) {
                return data.active ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
            }
        },
        {
            name: 'outboundComPortPoolIds',
            type: 'string',
            defaultValue: '-',
            mapping: function (data) {
                var comPortPools = data.outboundComPortPoolIds,
                    portPoolsStore = Ext.getStore('Mdc.store.ComPortPools'),
                    result = undefined;
                if (Ext.isArray(comPortPools) && comPortPools.length && portPoolsStore.getCount()) {
                    result = '';
                    Ext.Array.each(comPortPools, function (item) {
                        var comPortPool = portPoolsStore.getById(item);
                        comPortPool && (result += '<a href="#/administration/comportpools/' + item + '/overview">' + comPortPool.get('name') + '</a><br>');
                    });
                }
                return result;
            }
        },
        {
            name: 'serialPortConf',
            type: 'string',
            defaultValue: '-',
            mapping: function (data) {
                var result = '',
                    conf = [
                        {
                            field: 'baudrate',
                            store: 'Mdc.store.BaudRates',
                            associatedField: 'baudRate',
                            localizedField: 'localizedValue'
                        },
                        {
                            field: 'nrOfDataBits',
                            store: 'Mdc.store.NrOfDataBits',
                            associatedField: 'nrOfDataBits',
                            localizedField: 'localizedValue'
                        },
                        {
                            field: 'nrOfStopBits',
                            store: 'Mdc.store.NrOfStopBits',
                            associatedField: 'nrOfStopBits',
                            localizedField: 'localizedValue'
                        },
                        {
                            field: 'parity',
                            store: 'Mdc.store.Parities',
                            associatedField: 'parity',
                            localizedField: 'localizedValue'
                        },
                        {
                            field: 'flowControl',
                            store: 'Mdc.store.FlowControls',
                            associatedField: 'flowControl',
                            localizedField: 'localizedValue'
                        }
                    ];

                Ext.Array.each(conf, function (item) {
                    var value = data[item.field],
                        store,
                        index;

                    if (value) {
                        store = Ext.getStore(item.store);
                        index = store.find(item.associatedField, value);
                        if (index !== -1) {
                            result += store.getAt(index).get(item.localizedField) + '<br>';
                        } else {
                            result += value + '<br>';
                        }
                    }
                });

                return result;
            }
        },
        {
            name: 'inboundComPortPools',
            type: 'string',
            defaultValue: '-',
            mapping: function (data) {
                var id = data.comPortPool_id,
                    result = '',
                    portPoolsStore,
                    comPortPool;
                if (id) {
                    portPoolsStore = Ext.getStore('Mdc.store.ComPortPools');
                    comPortPool = portPoolsStore.getById(id);
                    result = comPortPool ? '<a href="#/administration/comportpools/' + id + '/overview">' + comPortPool.get('name') + '</a>' : '';
                }
                return result;
            }
        },
        {
            name: 'modemInitStrings',
            type: 'string',
            defaultValue: '-',
            mapping: function (data) {
                var val = data.modemInitStrings,
                    result = undefined;
                if (Ext.isArray(val)) {
                    if (val.length) {
                        result = '';
                        Ext.Array.each(val, function (item) {
                            result += item.modemInitString + '<br>';
                        })
                    }
                }
                return result;
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comservers/{comServerId}/comports',
        reader: {
            type: 'json'
        }
    }
});
