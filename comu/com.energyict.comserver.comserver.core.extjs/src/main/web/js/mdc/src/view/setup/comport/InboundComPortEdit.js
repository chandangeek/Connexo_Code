Ext.define('Mdc.view.setup.comport.InboundComPortEdit', {
    extend: 'Ext.window.Window',
    requires: [
        'Mdc.store.ComPortPools',
        'Mdc.store.ComPortTypes',
        'Mdc.store.FlowControls',
        'Mdc.store.NrOfDataBits',
        'Mdc.store.NrOfStopBits',
        'Mdc.store.Parities',
        'Mdc.store.BaudRates',
        'Mdc.view.setup.comport.ModemInitStrings',
        'Mdc.view.setup.comport.GlobalModemInitStrings'
    ],
    alias: 'widget.inboundComPortEdit',
    autoScroll: true,
    title: Uni.I18n.translate('comserver.comServer','MDC','ComServer'),
    // layout: 'fit',
    width: '80%',
    height: '90%',
    modal: true,
    constrain: true,
    autoShow: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    border: 0,


    initComponent: function () {
        var comporttypes = Ext.create('Mdc.store.ComPortTypes');
        var comportpools = Ext.create('Mdc.store.ComPortPools');
        var flowControls =  Ext.create('Mdc.store.FlowControls');
        var nrOfDataBits =  Ext.create('Mdc.store.NrOfDataBits');
        var nrOfStopBits =  Ext.create('Mdc.store.NrOfStopBits');
        var parities =  Ext.create('Mdc.store.Parities');
        var baudRates =  Ext.create('Mdc.store.BaudRates');
        comportpools.filter('direction','inbound');
        comportpools.load();
        this.items = [
            {
                xtype: 'form',
                shrinkWrap: 1,
                padding: 10,
                border: 0,
                defaults: {
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: 'fieldset',
                        title: Uni.I18n.translate('general.required','MDC','Required'),
                        defaults: {
                            labelWidth: 200
                        },
                        collapsible: true,
                        layout: 'anchor',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('general.name','MDC','Name')
                            },
                            {
                                xtype: 'combobox',
                                itemId: 'comPortTypeComboBox',
                                name: 'comPortType',
                                fieldLabel: 'Communication port type',
                                store: comporttypes,
                                queryMode: 'local',
                                displayField: 'comPortType',
                                valueField: 'comPortType'
                            },
                            {
                                xtype: 'textfield',
                                name: 'description',
                                fieldLabel: 'description'
                            },
                            {
                                xtype: 'numberfield',
                                name: 'numberOfSimultaneousConnections',
                                fieldLabel: 'numberOfSimultaneousConnections'
                            },
                            {
                                xtype: 'checkbox',
                                inputValue: true,
                                uncheckedValue: 'false',
                                name: 'active',
                                fieldLabel: 'active'
                            },
                            {
                                xtype: 'textfield',
                                name: 'portNumber',
                                fieldLabel: 'portNumber'
                            },
                            {
                                xtype: 'combobox',
                                name: 'comPortPool_id',
                                fieldLabel: 'Communication port pool',
                                store: comportpools,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            }
                        ]},
                    {
                        xtype: 'fieldset',
                        title: Uni.I18n.translate('comport.serial','MDC','Serial'),
                        itemId: 'serialFieldSet',
                        defaults: {
                            labelWidth: 200
                        },
                        collapsible: true,
                        layout: 'anchor',
                        hidden: true,
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'ringCount',
                                fieldLabel: 'ringCount'
                            },
                            {
                                xtype: 'textfield',
                                name: 'maximumNumberOfDialErrors',
                                fieldLabel: 'maximumNumberOfDialErrors'
                            },
                            {
                                xtype: 'timeInfoField',
                                name: 'connectTimeout',
                                fieldLabel: 'connectTimeout '
                            },
                            {
                                xtype: 'timeInfoField',
                                name: 'delayAfterConnect',
                                fieldLabel: 'delayAfterConnect '
                            },
                            {
                                xtype: 'timeInfoField',
                                name: 'delayBeforeSend',
                                fieldLabel: 'delayBeforeSend '
                            },
                            {
                                xtype: 'timeInfoField',
                                name: 'atCommandTimeout',
                                fieldLabel: 'atCommandTimeout '
                            },
                            {
                                xtype: 'textfield',
                                name: 'atCommandTry',
                                fieldLabel: 'atCommandTry'
                            },
                            {
                                xtype: 'combobox',
                                name: 'baudrate',
                                fieldLabel: 'Baudrate',
                                store: baudRates,
                                queryMode: 'local',
                                displayField: 'baudRate',
                                valueField: 'baudRate'
                            },
                            {
                                xtype: 'combobox',
                                name: 'nrOfDataBits',
                                fieldLabel: 'Nr of data bits',
                                store: nrOfDataBits,
                                queryMode: 'local',
                                displayField: 'nrOfDataBits',
                                valueField: 'nrOfDataBits'
                            },
                            {
                                xtype: 'combobox',
                                name: 'nrOfStopBits',
                                fieldLabel: 'Nr of stopbits',
                                store: nrOfStopBits,
                                queryMode: 'local',
                                displayField: 'nrOfStopBits',
                                valueField: 'nrOfStopBits'
                            },
                            {
                                xtype: 'combobox',
                                name: 'flowControl',
                                fieldLabel: 'Flow control',
                                store: flowControls,
                                queryMode: 'local',
                                displayField: 'flowControl',
                                valueField: 'flowControl'
                            },
                            {
                                xtype: 'combobox',
                                name: 'parity',
                                fieldLabel: 'Parity',
                                store: parities,
                                queryMode: 'local',
                                displayField: 'parity',
                                valueField: 'parity'
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: 'Global modem init strings',
                                items: [{"xtype": 'globalModemInitStrings'}]
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: 'Modem init strings',
                                items: [{"xtype": 'modemInitStrings'}]
                            }
                        ]
                    },
                    {
                        xtype: 'fieldset',
                        title: Uni.I18n.translate('comport.servlet','MDC','Servlet'),
                        itemId: 'servletFieldSet',
                        defaults: {
                            labelWidth: 200
                        },
                        collapsible: true,
                        layout: 'anchor',
                        hidden: true,
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'contextPath',
                                fieldLabel: 'contextPath'
                            },
                            {
                                xtype: 'checkbox',
                                inputValue: true,
                                uncheckedValue: 'false',
                                name: 'useHttps',
                                fieldLabel: 'useHttps'
                            },
                            {
                                xtype: 'textfield',
                                name: 'keyStoreFilePath',
                                fieldLabel: 'keyStoreFilePath'
                            },
                            {
                                xtype: 'textfield',
                                name: 'trustStoreFilePath',
                                fieldLabel: 'trustStoreFilePath'
                            },
                            {
                                xtype: 'textfield',
                                name: 'keyStorePassword',
                                fieldLabel: 'keyStorePassword'
                            },
                            {
                                xtype: 'textfield',
                                name: 'trustStorePassword',
                                fieldLabel: 'trustStorePassword'
                            }
                        ]
                    },
                    {
                        xtype: 'fieldset',
                        title: Uni.I18n.translate('comport.udp','MDC','Udp'),
                        itemId: 'udpFieldSet',
                        defaults: {
                            labelWidth: 200
                        },
                        collapsible: true,
                        layout: 'anchor',
                        hidden: true,
                        items: [
                            {
                                xtype: 'numberfield',
                                name: 'bufferSize',
                                fieldLabel: 'bufferSize'
                            }
                        ]
                    }
                ]
            }
        ];

        this.buttons = [
            {
                text: Uni.I18n.translate('general.save','MDC','Save'),
                action: 'save'
            },
            {
                text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
                action: 'cancel'
            }
        ];

        this.callParent(arguments);
    }
});

