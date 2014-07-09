Ext.define('Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConnectionMethodEdit',
    itemId: 'deviceConnectionMethodEdit',
    edit: false,

    requires: [
        'Mdc.store.ConnectionTypes',
        'Mdc.widget.ScheduleField',
        'Mdc.widget.TimeInSecondsField',
        'Mdc.widget.TimeInfoField',
        'Mdc.view.setup.property.Edit',
        'Uni.property.form.Property'
    ],

    isEdit: function () {
        return this.edit;
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'container',
                layout: {
                    type: 'vbox'
                },

                items: [
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'deviceConnectionMethodEditAddTitle',
                        margins: '10 10 10 10'
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'deviceConnectionMethodEditForm',
                        width: 900,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'combobox',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('deviceconnectionmethod.configurationConnectionMethod', 'MDC', 'Configuration connection method'),
                                itemId: 'deviceConnectionMethodComboBox',
                                store: this.connectionMethods,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'name',
                                emptyText: Uni.I18n.translate('deviceconnectionmethod.selectConnectionMethod', 'MDC', 'Select a connection method...'),
                                required: true,
                                forceSelection: true,
                                typeAhead: true,
                                msgTarget: 'under'
                            },
                            {
                                xtype: 'combobox',
                                name: 'comPortPool',
                                fieldLabel: Uni.I18n.translate('deviceconnectionmethod.comPortPool', 'MDC', 'Communication port pool'),
                                itemId: 'communicationPortPoolComboBox',
                                store: this.comPortPools,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'name',
                                emptyText: Uni.I18n.translate('deviceconnectionmethod.selectComPortPool', 'MDC', 'Select a communication port pool...'),
                                forceSelection: true,
                                typeAhead: true,
                                msgTarget: 'under',
                                disabled: true,
                                required: true
                            },
                            {
                                xtype: 'combobox',
                                name: 'connectionStrategy',
                                fieldLabel: Uni.I18n.translate('deviceconnectionmethod.connectionStrategy', 'MDC', 'Connection strategy'),
                                itemId: 'connectionStrategyComboBox',
                                store: this.connectionStrategies,
                                queryMode: 'local',
                                required: true,
                                displayField: 'localizedValue',
                                valueField: 'connectionStrategy',
                                emptyText: Uni.I18n.translate('deviceconnectionmethod.selectconnectionStrategy', 'MDC', 'Select a connection strategy...'),
                                forceSelection: true,
                                typeAhead: true,
                                msgTarget: 'under',
                                disabled: true
                            },
                            {
                                xtype: 'scheduleField',
                                name: 'nextExecutionSpecs',
                                itemId: 'scheduleField',
                                hidden: true,
                                required: true,
                                fieldLabel: Uni.I18n.translate('deviceconnectionmethod.connectionSchedule', 'MDC', 'Connection schedule'),
                                hourCfg: {
                                    width: 60
                                },
                                minuteCfg: {
                                    width: 60
                                },
                                secondCfg: {
                                    width: 60
                                },
                                disabled: true
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'comWindowField',
                                fieldLabel: Uni.I18n.translate('connectionmethod.connectionWindow', 'MDC', 'Connection window'),
                                disabled: true,
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    margin: '0 5 0 0'
                                },
                                items: [
                                    {
                                        xtype: 'checkbox',
                                        itemId: 'activateComWindowCheckBox',
                                        submitValue: false
                                    },
                                    {
                                        xtype: 'displayfield',
                                        value: Uni.I18n.translate('connectionmethod.openConnectionBetween', 'MDC', 'Open connection between')
                                    },
                                    {
                                        xtype: 'timeInSecondsField',
                                        name: 'comWindowStart',
                                        itemId: 'comWindowStart',
                                        disabled: true

                                    },
                                    {
                                        xtype: 'displayfield',
                                        value: ' - '
                                    },
                                    {
                                        xtype: 'timeInSecondsField',
                                        name: 'comWindowEnd',
                                        itemId: 'comWindowEnd',
                                        disabled: true

                                    }
                                ]
                            },
//                                    {
//                                        xtype: 'radiogroup',
//                                        fieldLabel: 'isDefault',
//                                        itemId: 'isDefault',
//                                        allowBlank: false,
//                                        horizontal: true,
//                                        columns: 2,
//                                        items: [
//                                            {
//                                                boxLabel: 'yes',
//                                                name: 'isDefault',
//                                                inputValue: true,
//                                                margin: '0 10 5 0'
//                                            },
//                                            {
//                                                boxLabel: 'no',
//                                                name: 'isDefault',
//                                                checked: true,
//                                                inputValue: false,
//                                                margin: '0 10 5 0'
//                                            }
//                                        ]
//                                    },
                            {
                                xtype: 'radiogroup',
                                fieldLabel: Uni.I18n.translate('deviceconnectionmethod.allowSimultaneousConnections', 'MDC', 'Allow simultaneous connection'),
                                itemId: 'allowSimultaneousConnections',
                                allowBlank: false,
                                vertical: true,
                                required: true,
                                columns: 1,
                                disabled: true,
                                items: [
                                    {
                                        boxLabel: Uni.I18n.translate('general.yes', 'MDC', 'Yes'),
                                        name: 'allowSimultaneousConnections',
                                        inputValue: true,
                                        margin: '0 10 5 0'
                                    },
                                    {
                                        boxLabel:  Uni.I18n.translate('general.yes', 'MDC', 'No'),
                                        name: 'allowSimultaneousConnections',
                                        checked: true,
                                        inputValue: false,
                                        margin: '0 10 5 0'
                                    }
                                ]
                            },
                            {
                                xtype: 'radiogroup',
                                fieldLabel: Uni.I18n.translate('deviceconnectionmethod.active', 'MDC', 'Active'),
                                itemId: 'activeRadioGroup',
                                allowBlank: false,
                                vertical: true,
                                required: true,
                                columns: 1,
                                disabled: true,
                                items: [
                                    {
                                        boxLabel:  Uni.I18n.translate('general.yes', 'MDC', 'Yes'),
                                        name: 'status',
                                        checked: true,
                                        inputValue: 'connectionTaskStatusActive',
                                        margin: '0 10 5 0'
                                    },
                                    {
                                        boxLabel:  Uni.I18n.translate('general.yes', 'MDC', 'No'),
                                        name: 'status',
                                        inputValue: 'connectionTaskStatusInActive',
                                        margin: '0 10 5 0'
                                    }
                                ]
                            }

                        ]
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'connectionDetailsTitle',
                        hidden: true,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: '<h3>' + Uni.I18n.translate('deviceconnectionmethod.connectionDetails', 'MDC', 'Connection details') + '</h3>',
                                text: ''
                            }
                        ]
                    },
                    {
                        xtype: 'property-form',
                        width: '100%'
                    },

                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'connectionMethodEditButtonForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        width: '100%',
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                ui: 'actions',
                                fieldLabel: '&nbsp',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'addAction',
                                        itemId: 'addEditButton'
                                    },
                                    {
                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                        xtype: 'button',
                                        ui: 'link',
                                        itemId: 'cancelLink',
                                        href: '#/administration/devicetypes/'
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'component',
                        height: 100
                    }


                ]
            }
        ];
        this.callParent(arguments);
        if (this.isEdit()) {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            if (this.direction === 'Outbound') {
                this.down('#addEditButton').action = 'editDeviceOutboundConnectionMethod';
            } else if (this.direction === 'Inbound') {
                this.down('#addEditButton').action = 'editDeviceInboundConnectionMethod';
                this.down('#connectionStrategyComboBox').setVisible(false);
                this.down('#allowSimultaneousConnections').setVisible(false);
                this.down('#comWindowField').setVisible(false);
            }
        } else {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            if (this.direction === 'Outbound') {
                this.down('#addEditButton').action = 'addDeviceOutboundConnectionMethod';
            } else if (this.direction === 'Inbound') {
                this.down('#addEditButton').action = 'addDeviceInboundConnectionMethod';
                this.down('#connectionStrategyComboBox').setVisible(false);
                this.down('#allowSimultaneousConnections').setVisible(false);
                this.down('#comWindowField').setVisible(false);
            }
        }
        this.down('#cancelLink').href = this.returnLink;

    }


});


