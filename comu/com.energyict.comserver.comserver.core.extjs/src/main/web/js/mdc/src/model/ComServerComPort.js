/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ComServerComPort', {
    extend: 'Uni.model.ParentVersion',
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
        'comPortType',
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
            type: 'auto',
            defaultValue: null
        },
        {
            name: 'inboundComPorts',
            type: 'auto',
            defaultValue: null
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
            type: 'auto',
            defaultValue: null
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
        {name:'flowControl',useNull: true, defaultValue: null},
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
        {name:'parity',useNull: true, defaultValue: null},
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
            type: 'auto',
            defaultValue: null
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
            type: 'auto',
            defaultValue: null
        },
        {
            name: 'outboundComPortPoolIdsDisplay',
            type: 'string',
            useNull: true,
            defaultValue: null,
            persist: false,
            mapping: function (data) {
                var comPortPools = data.outboundComPortPoolIds,
                    portPoolsStore = Ext.getStore('Mdc.store.ComPortPools'),
                    result = undefined;
                if (Ext.isArray(comPortPools) && comPortPools.length && portPoolsStore.getCount()) {
                    result = '';
                    Ext.Array.each(comPortPools, function (item) {
                        var comPortPool = portPoolsStore.getById(item.id);
                        comPortPool && (result += '<a href="#/administration/comportpools/' + item.id + '">' + Ext.String.htmlEncode(comPortPool.get('name')) + '</a><br>');
                    });
                }
                return result;
            }
        },
        {
            name: 'serialPortConf',
            type: 'string',
            mapping: function (data) {
                // ATTENTION: Do NOT remove this comment !!!
                // It is used by our script to generate the needed translation strings (used by the code beneath)
                // Uni.I18n.translatePlural('general.x.bps', 0, 'MDC', '{0} bits per second', '{0} bits per second', '{0} bits per second')
                // Uni.I18n.translatePlural('general.x.bits', 0, 'MDC', '{0} bits', '{0} bit', '{0} bits')
                // Uni.I18n.translatePlural('general.x.stopBits', 0, 'MDC', '{0} stop bits', '{0} stop bit', '{0} stop bits')
                var result = '',
                    conf = [
                        {
                            field: 'baudrate',
                            store: 'Mdc.store.BaudRates',
                            associatedField: 'baudRate',
                            localizedField: 'localizedValue',
                            unit: {
                                translateKey: 'general.x.bps',
                                defaultTranslationZero: '{0} bits per second',
                                defaultTranslationOne: '{0} bit per second',
                                defaultTranslationMany: '{0} bits per second'
                            }
                        },
                        {
                            field: 'nrOfDataBits',
                            store: 'Mdc.store.NrOfDataBits',
                            associatedField: 'nrOfDataBits',
                            localizedField: 'localizedValue',
                            unit: {
                                translateKey: 'general.x.bits',
                                defaultTranslationZero: '{0} bits',
                                defaultTranslationOne: '{0} bit',
                                defaultTranslationMany: '{0} bits'
                            }
                        },
                        {
                            field: 'nrOfStopBits',
                            store: 'Mdc.store.NrOfStopBits',
                            associatedField: 'nrOfStopBits',
                            localizedField: 'localizedValue',
                            unit: {
                                translateKey: 'general.x.stopBits',
                                defaultTranslationZero: '{0} stop bits',
                                defaultTranslationOne: '{0} stop bit',
                                defaultTranslationMany: '{0} stop bits'
                            }
                        },
                        {
                            field: 'parity',
                            store: 'Mdc.store.Parities',
                            associatedField: 'id',
                            localizedField: 'localizedValue'
                        },
                        {
                            field: 'flowControl',
                            store: 'Mdc.store.FlowControls',
                            associatedField: 'id',
                            localizedField: 'localizedValue'
                        }
                    ];
                Ext.Array.each(conf, function (item) {
                    var value = data[item.field],
                        unit = null;

                    if (value) {
                        item.unit && (unit = Uni.I18n.translatePlural(item.unit.translateKey, parseInt(value), 'MDC',
                            item.unit.defaultTranslationZero, item.unit.defaultTranslationOne, item.unit.defaultTranslationMany));

                        if (unit) {
                            result += (unit ? unit : value);
                        } else {
                            store = Ext.getStore(item.store);
                            if (!store) {
                                return false;
                            }
                            index = Ext.isObject(value) ? store.find(item.associatedField, value.id) : store.find(item.associatedField, value);
                            if (index !== -1) {
                                result += store.getAt(index).get(item.localizedField);
                            } else {
                                result += value;
                            }
                        }
                        result += '<br>';
                    }
                });

                return result;
            }
        },
        {
            name: 'inboundComPortPools',
            type: 'string',
            mapping: function (data) {
                var id = data.comPortPool_id ? data.comPortPool_id.id : null,
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
            defaultValue: null,
            useNull: true
        },
        {
            name: 'globalModemInitStrings',
            defaultValue: null,
            useNull: true
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
