Ext.define('Mdc.view.setup.comservercomports.SerialForm', {
    extend: 'Ext.container.Container',
    alias: 'widget.serialForm',
    defaults: {
        labelWidth: 250,
        width: 600,
        validateOnChange : false,
        validateOnBlur : false
    },
    items: [
        {
            xtype: 'fieldcontainer',
            required: true,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.outPools', 'MDC', 'Outbound comport pools'),
            itemId: 'comportpoolid',
            items: [
                {
                    xtype: 'outboundportcomportpools'
                }
            ]
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
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.inPools', 'MDC', 'Inbound communication port pool'),
            editable: false,
            name: 'inboundPool'
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
            name: 'ringCount'
        },
        {
            xtype: 'numberfield',
            required: true,
            allowBlank: false,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.errorNum', 'MDC', 'Number of errors'),
            name: 'maximumNumberOfDialErrors'
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
                    margin: '0 8 0 0',
                    flex: 2
                },
                {
                    xtype: 'combobox',
                    required: true,
                    allowBlank: false,
                    editable: false,
                    name: 'connectTimeout[timeUnit]',
                    flex: 1,
                    displayField: 'timeUnit',
                    store: 'Mdc.store.TimeUnits'
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
                    required: true,
                    allowBlank: false,
                    name: 'connectDelay[count]',
                    margin: '0 8 0 0',
                    flex: 2
                },
                {
                    xtype: 'combobox',
                    required: true,
                    allowBlank: false,
                    editable: false,
                    name: 'connectDelay[timeUnit]',
                    flex: 1,
                    displayField: 'timeUnit',
                    store: 'Mdc.store.TimeUnits'
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.sendDelay', 'MDC', 'Delay before send'),
            required: true,
            itemId: 'sendDelay',
            layout: 'hbox',
            items: [
                {
                    xtype: 'numberfield',
                    required: true,
                    allowBlank: false,
                    name: 'sendDelay[count]',
                    margin: '0 8 0 0',
                    flex: 2
                },
                {
                    xtype: 'combobox',
                    required: true,
                    editable: false,
                    allowBlank: false,
                    name: 'sendDelay[timeUnit]',
                    flex: 1,
                    displayField: 'timeUnit',
                    store: 'Mdc.store.TimeUnits'
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.atTimeout', 'MDC', 'At command timeout'),
            required: true,
            itemId: 'atCommTimeout',
            layout: 'hbox',
            items: [
                {
                    xtype: 'numberfield',
                    required: true,
                    allowBlank: false,
                    name: 'atCommandTimeout[count]',
                    margin: '0 8 0 0',
                    flex: 2
                },
                {
                    xtype: 'combobox',
                    required: true,
                    allowBlank: false,
                    editable: false,
                    name: 'atCommandTimeout[timeUnit]',
                    flex: 1,
                    store: 'Mdc.store.TimeUnits'
                }
            ]
        },
        {
            xtype: 'combobox',
            required: true,
            allowBlank: false,
            fieldLabel: Uni.I18n.translate('comServerComPorts.form.atTry', 'MDC', 'AT command try'),
            name: 'atCommandTry'
        }
    ],


    showInbound: function(){
        this.down('#comportpoolid').hide();
        this.down('textfield[name=modemInitStrings]').show();
        this.down('textfield[name=addressSelector]').show();
        this.down('combobox[name=inboundPool]').show();
        this.down('#serialPortConfig').show();
        this.down('numberfield[name=ringCount]').show();
        this.down('numberfield[name=maximumNumberOfDialErrors]').show();
        this.down('#connectionTimeout').show();
        this.down('#delayAfterConnect').show();
        this.down('#sendDelay').show();
        this.down('#atCommTimeout').show();
        this.down('combobox[name=atCommandTry]').show();
    },

    showOutbound: function(){
        this.down('#comportpoolid').show();
        this.down('textfield[name=modemInitStrings]').hide();
        this.down('textfield[name=modemInitStrings]').disable();

        this.down('textfield[name=addressSelector]').hide();
        this.down('textfield[name=addressSelector]').disable();

        this.down('combobox[name=inboundPool]').hide();
        this.down('combobox[name=inboundPool]').disable();

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

        this.down('#sendDelay').hide();
        this.down('#sendDelay').disable();

        this.down('#atCommTimeout').hide();
        this.down('#atCommTimeout').disable();

        this.down('combobox[name=atCommandTry]').hide();
        this.down('combobox[name=atCommandTry]').disable();
    }
});

