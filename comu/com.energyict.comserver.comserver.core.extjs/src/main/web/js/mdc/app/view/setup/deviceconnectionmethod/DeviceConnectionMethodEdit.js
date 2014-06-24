Ext.define('Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConnectionMethodEdit',
    itemId: 'deviceConnectionMethodEdit',
    edit: false,

    requires: [
        'Mdc.store.ConnectionTypes',
        'Mdc.widget.ScheduleField'
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
//                    align: 'stretch'
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
//                                padding: '10 10 0 10',
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
                                fieldLabel: Uni.I18n.translate('deviceconnectionmethod.connectionmethod', 'MDC', 'Connection method'),
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
                                disabled: true
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
                                emptyText: Uni.I18n.translate('deviceconnectionmethod.selectconnectionStrategy', 'MDC', 'Select a connection strategy'),
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
                                fieldLabel: 'Schedule',
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
                                xtype: 'timeInfoField',
                                name: 'rescheduleRetryDelay',
                                fieldLabel: Uni.I18n.translate('deviceconnectionmethod.rescheduleRetryDelay', 'MDC', 'Retry delay'),
                                itemId: 'rescheduleRetryDelay',
                                required: true,
                                disabled: true
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
                                        boxLabel: 'yes',
                                        name: 'allowSimultaneousConnections',
                                        inputValue: true,
                                        margin: '0 10 5 0'
                                    },
                                    {
                                        boxLabel: 'no',
                                        name: 'allowSimultaneousConnections',
                                        checked: true,
                                        inputValue: false,
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
                        xtype: 'propertyEdit',
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
                this.down('#rescheduleRetryDelay').setVisible(false);
                this.down('#allowSimultaneousConnections').setVisible(false);
//                this.down('#isDefault').setVisible(false);
            }
        } else {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            if (this.direction === 'Outbound') {
                this.down('#addEditButton').action = 'addDeviceOutboundConnectionMethod';
            } else if (this.direction === 'Inbound') {
                this.down('#addEditButton').action = 'addDeviceInboundConnectionMethod';
                this.down('#connectionStrategyComboBox').setVisible(false);
                this.down('#rescheduleRetryDelay').setVisible(false);
                this.down('#allowSimultaneousConnections').setVisible(false);
//                this.down('#isDefault').setVisible(false);
            }
        }
        this.down('#cancelLink').href = this.returnLink;

    }


});


