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
            type: 'string'
        },
        {
            name: 'direction',
            type: 'string',
            mapping: function (data) {
                if (data.direction) {
                    return data.direction.charAt(0).toUpperCase() + data.direction.slice(1);
                }
            }
        },
        {
            name: 'comPortType',
            type: 'string'
        },
        {
            name: 'comServerName',
            type: 'string'
        },
        {
            name: 'active',
            type: 'boolean'
        },
        {
            name: 'comServerType',
            type: 'string'
        },
        {
            name: 'serverLogLevel',
            type: 'string'
        },
        {
            name: 'communicationLogLevel',
            type: 'string'
        },
        {
            name: 'changesInterPollDelay',
            type: 'auto'
        },
        {
            name: 'schedulingInterPollDelay',
            type: 'auto'
        },
        {
            name: 'inboundComPorts',
            type: 'auto'
        },
        {
            name: 'type',
            type: 'string'
        },
        {
            name: 'bound',
            type: 'boolean'
        },
        {
            name: 'comServer_id',
            type: 'int'
        },
        {
            name: 'description',
            type: 'string'
        },
        {
            name: 'numberOfSimultaneousConnections',
            type: 'int'
        },
        {
            name: 'comPortPool_id',
            type: 'int'
        },
        {
            name: 'portNumber',
            type: 'int'
        },
        {
            name: 'atCommandTimeout',
            type: 'string',
            mapping: function (data) {
                var val = data.atCommandTimeout;
                return val ? val.count ? val.count + (val.timeUnit ? ' ' + val.timeUnit : '') : undefined : undefined;
            }
        },
        {
            name: 'atCommandTry',
            type: 'int'
        },
        {
            name: 'baudrate',
            type: 'string'
        },
        {
            name: 'connectTimeout',
            type: 'string',
            mapping: function (data) {
                var val = data.connectTimeout;
                return val ? val.count ? val.count + (val.timeUnit ? ' ' + val.timeUnit : '') : undefined : undefined;
            }
        },
        {
            name: 'delayAfterConnect',
            type: 'string',
            mapping: function (data) {
                var val = data.delayAfterConnect;
                return val ? val.count ? val.count + (val.timeUnit ? ' ' + val.timeUnit : '') : undefined : undefined;
            }
        },
        {
            name: 'delayBeforeSend',
            type: 'string',
            mapping: function (data) {
                var val = data.delayBeforeSend;
                return val ? val.count ? val.count + (val.timeUnit ? ' ' + val.timeUnit : '') : undefined : undefined;
            }
        },
        {
            name: 'flowControl',
            type: 'string'
        },
        {
            name: 'maximumNumberOfDialErrors',
            type: 'int'
        },
        {
            name: 'nrOfDataBits',
            type: 'string'
        },
        {
            name: 'nrOfStopBits',
            type: 'string'
        },
        {
            name: 'parity',
            type: 'string'
        },
        {
            name: 'ringCount',
            type: 'int'
        },
        {
            name: 'contextPath',
            type: 'string'
        },
        {
            name: 'keyStoreFilePath',
            type: 'string'
        },
        {
            name: 'keyStorePassword',
            type: 'string'
        },
        {
            name: 'trustStoreFilePath',
            type: 'string'
        },
        {
            name: 'trustStorePassword',
            type: 'string'
        },
        {
            name: 'useHttps',
            type: 'string'
        },
        {
            name: 'addressSelector',
            type: 'auto'
        },
        {
            name: 'status',
            type: 'string',
            mapping: function (data) {
                return data.active ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
            }
        },
        {
            name: 'outboundComPortPoolIds',
            type: 'string',
            mapping: function (data) {
                var comPortPools = data.outboundComPortPoolIds,
                    portPoolsStore = Ext.getStore('Mdc.store.ComPortPools'),
                    result = undefined;
                if (Ext.isArray(comPortPools) && comPortPools.length && portPoolsStore.getCount()) {
                    result = '';
                    Ext.Array.each(comPortPools, function (item) {
                        var comPortPool = portPoolsStore.getById(item);
                        comPortPool && (result += '<a href="#/administration/comportpools/' + item + '">' + Ext.String.htmlEncode(comPortPool.get('name')) + '</a><br>');
                    });
                }
                return result;
            }
        },
        {
            name: 'serialPortConf',
            type: 'string',
            mapping: function (data) {
                var result = '',
                    conf = [
                        {
                            field: 'baudrate',
                            store: 'Mdc.store.BaudRates',
                            associatedField: 'baudRate',
                            localizedField: 'localizedValue',
                            unit: {
                                translateKey: 'comports.preview.bitsPerSecond',
                                defaultTranslation: 'bits per second'
                            }
                        },
                        {
                            field: 'nrOfDataBits',
                            store: 'Mdc.store.NrOfDataBits',
                            associatedField: 'nrOfDataBits',
                            localizedField: 'localizedValue',
                            unit: {
                                translateKey: 'comports.preview.bits',
                                defaultTranslation: 'bits'
                            }
                        },
                        {
                            field: 'nrOfStopBits',
                            store: 'Mdc.store.NrOfStopBits',
                            associatedField: 'nrOfStopBits',
                            localizedField: 'localizedValue',
                            unit: {
                                translateKey: 'comports.preview.stopBits',
                                defaultTranslation: 'stop bits'
                            }
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
                        unit = '',
                        store,
                        index;

                    item.unit && (unit = Uni.I18n.translatePlural(item.unit.translateKey, parseInt(value), 'MDC', item.unit.defaultTranslation));

                    if (value) {
                        store = Ext.getStore(item.store);
                        if (!store) {
                            return false;
                        }
                        index = store.find(item.associatedField, value);
                        if (index !== -1) {
                            result += store.getAt(index).get(item.localizedField) + (unit ? ' ' + unit : '') + '<br>';
                        } else {
                            result += value + (unit ? ' ' + unit : '') + '<br>';
                        }
                    }
                });

                return result;
            }
        },
        {
            name: 'inboundComPortPools',
            type: 'string',
            mapping: function (data) {
                var id = data.comPortPool_id,
                    result = '',
                    portPoolsStore,
                    comPortPool;
                if (id) {
                    portPoolsStore = Ext.getStore('Mdc.store.ComPortPools');
                    comPortPool = portPoolsStore.getById(id);
                    result = comPortPool ? '<a href="#/administration/comportpools/' + id + '">' + Ext.String.htmlEncode(comPortPool.get('name')) + '</a>' : '';
                }
                return result;
            }
        },
        {
            name: 'modemInitStrings',
            type: 'string',
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
        },
        {
            name: 'globalModemInitStrings',
            type: 'string',
            mapping: function (data) {
                var val = data.globalModemInitStrings,
                    result = undefined;
                if (Ext.isArray(val)) {
                    if (val.length) {
                        result = '';
                        Ext.Array.each(val, function (item) {
                            result += item.globalModemInitString + '<br>';
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
