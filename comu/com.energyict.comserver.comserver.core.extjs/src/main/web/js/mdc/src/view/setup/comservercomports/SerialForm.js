Ext.define('Mdc.view.setup.comservercomports.SerialForm', {
    extend: 'Ext.container.Container',
    alias: 'widget.serialForm',
    requires: [
      'Mdc.store.TimeUnits'
    ],
    defaults: {
        labelWidth: 250,
        width: 600,
        validateOnChange: false,
        validateOnBlur: false
    },
    items: [
        {
            xtype: 'fieldcontainer',
            required: false,
            fieldLabel: Uni.I18n.translate('general.comPortPools', 'MDC', 'Communication port pools'),
            itemId: 'comportpoolid',
            items: [
                {
                    xtype: 'outboundportcomportpools'
                }
            ]
        },
        {
            xtype: 'textfield',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.globalModemInitStrings', 'MDC', 'Global modem initialization'),
            name: 'globalModemInitStrings'
        },
        {
            xtype: 'textfield',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.modemInit', 'MDC', 'Modem initialization'),
            name: 'modemInitStrings'
        },
        {
            xtype: 'textfield',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.addrSelector', 'MDC', 'Address selector'),
            name: 'addressSelector'
        },
        {
            xtype: 'combobox',
            fieldLabel: Uni.I18n.translate('general.comPortPool', 'MDC', 'Communication port pool'),
            required: false,
            store: 'Mdc.store.InboundComPortPools',
            editable: false,
            queryMode: 'local',
            itemId: 'inboundPool',
            name: 'comPortPool_id',
            displayField: 'name',
            valueField: 'id',
            emptyText: 'Select inbound communication port pool...'
        },
        {
            xtype: 'fieldcontainer',
            itemId: 'helpLabel',
            fieldLabel: '&nbsp;',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    html: '<span style="color: grey"><i>' + Uni.I18n.translate('comports.preview.noInboundCommPortPool', 'MDC', 'When no inbound communication port pool is selected,<br> the port cannot be activated') + '</i></span>',
                    xtype: 'component'

                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.serialPortConf', 'MDC', 'Serial port configuration'),
            required: true,
            width: 600,
            itemId: 'serialPortConfig',
            name: 'portConfig',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    margin: '0 0 8 0',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'combobox',
                            margin: '0 16 0 0',
                            allowBlank: false,
                            editable: false,
                            validateOnChange : false,
                            validateOnBlur : false,
                            name: 'baudrate',
                            itemId: 'bitsPerSecond',
                            store: 'Mdc.store.BaudRates',
                            displayField: 'localizedValue',
                            valueField: 'baudRate'
                        },
                        {
                            xtype: 'label',
                            text: Uni.I18n.translate('comServerComPorts.form.bps', 'MDC', 'bits per second')
                        }
                    ]
                },
                {
                    xtype: 'container',
                    margin: '0 0 8 0',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'combobox',
                            margin: '0 16 0 0',
                            name: 'nrOfDataBits',
                            itemId: 'bits',
                            editable: false,
                            required: true,
                            allowBlank: false,
                            validateOnChange : false,
                            validateOnBlur : false,
                            store: 'Mdc.store.NrOfDataBits',
                            valueField: 'nrOfDataBits',
                            displayField: 'localizedValue'
                        },
                        {
                            xtype: 'label',
                            text: Uni.I18n.translate('comServerComPorts.form.bits', 'MDC', 'bits')
                        }
                    ]
                },
                {
                    xtype: 'container',
                    margin: '0 0 8 0',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'combobox',
                            margin: '0 16 0 0',
                            required: true,
                            editable: false,
                            allowBlank: false,
                            validateOnChange : false,
                            validateOnBlur : false,
                            name: 'nrOfStopBits',
                            itemId: 'stopBits',
                            store: 'Mdc.store.NrOfStopBits',
                            valueField: 'nrOfStopBits',
                            displayField: 'localizedValue'
                        },
                        {
                            xtype: 'label',
                            text: Uni.I18n.translate('comServerComPorts.form.stopBits', 'MDC', 'stop bits')
                        }
                    ]
                },
                {
                    xtype: 'combobox',
                    required: true,
                    editable: false,
                    allowBlank: false,
                    validateOnChange : false,
                    validateOnBlur : false,
                    name: 'parity',
                    itemId: 'parity',
                    store: 'Mdc.store.Parities',
                    valueField: 'parity',
                    displayField: 'localizedValue',
                    emptyText: Uni.I18n.translate('comServerComPorts.form.parity', 'MDC', 'Parity')
                },
                {
                    xtype: 'combobox',
                    required: true,
                    editable: false,
                    allowBlank: false,
                    validateOnChange : false,
                    validateOnBlur : false,
                    name: 'flowControl',
                    itemId: 'flowControl',
                    valueField: 'flowControl',
                    displayField: 'localizedValue',
                    store: 'Mdc.store.FlowControls',
                    emptyText: Uni.I18n.translate('comServerComPorts.form.flowControl', 'MDC', 'Flow control')
                }
            ]
        },
        {
            xtype: 'numberfield',
            required: true,
            allowBlank: false,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.ringCount', 'MDC', 'Ring count'),
            name: 'ringCount',
            minValue: 0,
            stripCharsRe: /\D/,
            value: 3
        },
        {
            xtype: 'numberfield',
            required: true,
            allowBlank: false,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.errorNum', 'MDC', 'Max. number of dial errors'),
            name: 'maximumNumberOfDialErrors',
            minValue: 0,
            stripCharsRe: /\D/,
            value: 3
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.connTimeout', 'MDC', 'Connection timeout'),
            itemId: 'connectionTimeout',
            required: true,
            layout: 'hbox',
            items: [
                {
                    xtype: 'numberfield',
                    required: true,
                    allowBlank: false,
                    name: 'connectTimeout[count]',
                    itemId: 'connectTimeoutCount',
                    margin: '0 8 0 0',
                    flex: 2,
                    minValue: 0,
                    stripCharsRe: /\D/,
                    value: 5
                },
                {
                    xtype: 'combobox',
                    required: true,
                    allowBlank: false,
                    editable: false,
                    name: 'connectTimeout[timeUnit]',
                    flex: 1,
                    displayField: 'timeUnit',
                    itemId: 'connectTimeoutUnit',
                    store: 'TimeUnits'
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.connDelay', 'MDC', 'Delay after connect'),
            required: true,
            itemId: 'delayAfterConnect',
            layout: 'hbox',
            items: [
                {
                    xtype: 'numberfield',
                    itemId: 'connectDelayCount',
                    required: true,
                    allowBlank: false,
                    name: 'delayAfterConnect[count]',
                    margin: '0 8 0 0',
                    flex: 2,
                    minValue: 0,
                    stripCharsRe: /\D/,
                    value: 5
                },
                {
                    xtype: 'combobox',
                    itemId: 'connectDelayUnit',
                    required: true,
                    allowBlank: false,
                    editable: false,
                    name: 'delayAfterConnect[timeUnit]',
                    flex: 1,
                    displayField: 'timeUnit',
                    store: 'TimeUnits'
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.sendDelay', 'MDC', 'Delay before send'),
            required: true,
            itemId: 'delayBeforeSend',
            layout: 'hbox',
            items: [
                {
                    xtype: 'numberfield',
                    itemId: 'sendDelayCount',
                    required: true,
                    allowBlank: false,
                    name: 'delayBeforeSend[count]',
                    margin: '0 8 0 0',
                    flex: 2,
                    minValue: 0,
                    stripCharsRe: /\D/,
                    value: 5
                },
                {
                    xtype: 'combobox',
                    itemId: 'sendDelayUnit',
                    required: true,
                    editable: false,
                    allowBlank: false,
                    name: 'delayBeforeSend[timeUnit]',
                    flex: 1,
                    displayField: 'timeUnit',
                    store: 'TimeUnits'
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.atTimeout', 'MDC', 'AT command timeout'),
            required: true,
            itemId: 'atCommandTimeout',
            layout: 'hbox',
            items: [
                {
                    xtype: 'numberfield',
                    itemId: 'atCommandTimeoutCount',
                    required: true,
                    allowBlank: false,
                    name: 'atCommandTimeout[count]',
                    margin: '0 8 0 0',
                    flex: 2,
                    minValue: 0,
                    stripCharsRe: /\D/,
                    value: 5
                },
                {
                    xtype: 'combobox',
                    itemId: 'atCommandTimeoutUnit',
                    required: true,
                    allowBlank: false,
                    editable: false,
                    name: 'atCommandTimeout[timeUnit]',
                    flex: 1,
                    displayField: 'timeUnit',
                    store: 'TimeUnits'
                }
            ]
        },
        {
            xtype: 'numberfield',
            required: true,
            allowBlank: false,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.atTry', 'MDC', 'AT command try'),
            name: 'atCommandTry',
            minValue: 0,
            stripCharsRe: /\D/,
            value: 3
        }
    ],


    showInbound: function(){
        this.down('#comportpoolid').hide();
        this.down('textfield[name=globalModemInitStrings]').show();
        this.down('textfield[name=modemInitStrings]').show();
        this.down('textfield[name=addressSelector]').show();
        this.down('#inboundPool').show();
        this.down('#serialPortConfig').show();
        this.down('numberfield[name=ringCount]').show();
        this.down('numberfield[name=maximumNumberOfDialErrors]').show();
        this.down('#connectionTimeout').show();
        this.down('#delayAfterConnect').show();
        this.down('#delayBeforeSend').show();
        this.down('#atCommandTimeout').show();
        this.down('#helpLabel').show();
        this.down('numberfield[name=atCommandTry]').show();
    },

    showOutbound: function(){
        this.down('#comportpoolid').show();
        this.down('textfield[name=globalModemInitStrings]').hide();
        this.down('textfield[name=modemInitStrings]').hide();
        this.down('textfield[name=globalModemInitStrings]').disable();
        this.down('textfield[name=modemInitStrings]').disable();

        this.down('textfield[name=addressSelector]').hide();
        this.down('textfield[name=addressSelector]').disable();

        this.down('#inboundPool').hide();
        this.down('#inboundPool').disable();

        this.down('#serialPortConfig').hide();
        this.down('#serialPortConfig').disable();

        this.down('numberfield[name=ringCount]').hide();
        this.down('numberfield[name=ringCount]').disable();

        this.down('numberfield[name=maximumNumberOfDialErrors]').hide();
        this.down('numberfield[name=maximumNumberOfDialErrors]').disable();

        this.down('#connectionTimeout').hide();
        this.down('#connectionTimeout').disable();

        this.down('#delayAfterConnect').hide();
        this.down('#delayAfterConnect').disable();

        this.down('#delayBeforeSend').hide();
        this.down('#delayBeforeSend').disable();

        this.down('#atCommandTimeout').hide();
        this.down('#atCommandTimeout').disable();

        this.down('numberfield[name=atCommandTry]').hide();
        this.down('numberfield[name=atCommandTry]').disable();

        this.down('#helpLabel').hide();
    }
});

