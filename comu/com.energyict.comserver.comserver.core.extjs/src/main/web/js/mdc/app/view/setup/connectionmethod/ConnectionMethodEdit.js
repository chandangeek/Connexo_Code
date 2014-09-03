Ext.define('Mdc.view.setup.connectionmethod.ConnectionMethodEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.connectionMethodEdit',
    itemId: 'connectionMethodEdit',
    edit: false,

    requires: [
        'Uni.property.form.Property',
        'Mdc.store.ConnectionTypes',
        'Mdc.widget.ScheduleField',
        'Mdc.widget.TimeInSecondsField',
        'Mdc.widget.TimeInfoField',
        'Mdc.view.setup.property.Edit'
    ],

    isEdit: function () {
        return this.edit;
    },

//    setEdit: function (edit, returnLink) {
//        if (edit) {
//            this.edit = edit;
//            this.down('#addEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
//            this.down('#addEditButton').action = 'editConnectionMethod';
//        } else {
//            this.edit = edit;
//            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
//            this.down('#addEditButton').action = 'addConnectionMethod';
//        }
//        this.down('#cancelLink').href = returnLink;
//    },

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
                        itemId: 'connectionMethodEditAddTitle',
                        margins: '10 10 10 10'
                    },
//                    {
//                        xtype: 'container',
//                        layout: {
//                            type: 'column'
//                        },
//                        items: [
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'connectionMethodEditForm',
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
                                xtype: 'textfield',
                                name: 'name',
                                msgTarget: 'under',
                                required: true,
                                fieldLabel: Uni.I18n.translate('connectionmethod.name', 'MDC', 'Name'),
                                itemId: 'editConnectionMethodNameField',
                                maxLength: 80,
                                enforceMaxLength: true
                            },
                            {
                                xtype: 'combobox',
                                name: 'connectionTypePluggableClass',
                                fieldLabel: Uni.I18n.translate('connectionmethod.connectionType', 'MDC', 'Connection type'),
                                itemId: 'connectionTypeComboBox',
                                store: this.connectionTypes,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'name',
                                emptyText: Uni.I18n.translate('connectionmethod.selectConnectionMethod', 'MDC', 'Select a connection type...'),
                                required: true,
                                forceSelection: true,
                                typeAhead: true,
                                msgTarget: 'under'
                            },
                            {
                                xtype: 'combobox',
                                name: 'comPortPool',
                                fieldLabel: Uni.I18n.translate('connectionmethod.comPortPool', 'MDC', 'Communication port pool'),
                                itemId: 'communicationPortPoolComboBox',
                                store: this.comPortPools,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'name',
                                emptyText: Uni.I18n.translate('connectionmethod.selectComPortPool', 'MDC', 'Select a communication port pool...'),
                                forceSelection: true,
                                typeAhead: true,
                                msgTarget: 'under'
                            },
                            {
                                xtype: 'combobox',
                                name: 'connectionStrategy',
                                fieldLabel: Uni.I18n.translate('connectionmethod.connectionStrategy', 'MDC', 'Connection strategy'),
                                itemId: 'connectionStrategyComboBox',
                                store: this.connectionStrategies,
                                queryMode: 'local',
                                required: true,
                                displayField: 'localizedValue',
                                valueField: 'connectionStrategy',
                                emptyText: Uni.I18n.translate('connectionmethod.selectconnectionStrategy', 'MDC', 'Select a connection strategy...'),
                                forceSelection: true,
                                typeAhead: true,
                                msgTarget: 'under'
                            },
                            {
                                xtype: 'scheduleField',
                                name: 'temporalExpression',
                                itemId: 'scheduleField',
                                hidden: true,
                                required: true,
                                fieldLabel: 'Schedule',
                                hourCfg: {
                                    width: 60
                                },
                                minuteCfg: {
                                    width: 60
                                },
                                secondCfg: {
                                    width: 60
                                }
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel:Uni.I18n.translate('connectionmethod.rescheduleRetryDelay', 'MDC', 'Retry delay'),
                                itemId: 'rescheduleRetryDelayFieldContainer',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        value: Uni.I18n.translate('connectionmethod.retryEvery', 'MDC', 'Retry every'),
                                        margin: '0 5 0 0'
                                    },
                                    {
                                        xtype: 'timeInfoField',
                                        name: 'rescheduleRetryDelay',
                                        itemId: 'rescheduleRetryDelay',
                                        required: true,
                                        value: {
                                            count: 5,
                                            timeUnit: 'minutes'
                                        }
                                    }
                                ]
                            },

                            {
                                xtype: 'fieldcontainer',
                                itemId: 'comWindowField',
                                fieldLabel: Uni.I18n.translate('connectionmethod.connectionWindow', 'MDC', 'Connection window'),
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
                                fieldLabel: Uni.I18n.translate('connectionmethod.allowSimultaneousConnections', 'MDC', 'Allow simultaneous connection'),
                                itemId: 'allowSimultaneousConnections',
                                allowBlank: false,
                                vertical: true,
                                required: true,
                                columns: 1,
                                items: [
                                    {
                                        boxLabel: 'Yes',
                                        name: 'allowSimultaneousConnections',
                                        inputValue: true,
                                        margin: '0 10 5 0'
                                    },
                                    {
                                        boxLabel: 'No',
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
                        title: '<span class="x-form-item-label-right x-form-item-label" style="width: 250px;"><h3>' +
                            Uni.I18n.translate('connectionmethod.connectionDetails', 'MDC', 'Connection details') +
                            '</h3></span>',
                        xtype: 'property-form',
                        hidden: true,
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
                this.down('#addEditButton').action = 'editOutboundConnectionMethod';
            } else if (this.direction === 'Inbound') {
                this.down('#addEditButton').action = 'editInboundConnectionMethod';
                this.down('#connectionStrategyComboBox').setVisible(false);
                this.down('#rescheduleRetryDelayFieldContainer').setVisible(false);
                this.down('#allowSimultaneousConnections').setVisible(false);
                this.down('#comWindowField').setVisible(false);

//                this.down('#isDefault').setVisible(false);
            }
        } else {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            var me = this;
            this.down('#scheduleField').setValue({
                every: {
                    count: 5,
                    timeUnit: 'minutes'
                },
                lastDay: false,
                offset: {
                    count: 0,
                    timeUnit: 'seconds'
                }
            });
            if (this.direction === 'Outbound') {
                this.down('#addEditButton').action = 'addOutboundConnectionMethod';
            } else if (this.direction === 'Inbound') {
                this.down('#addEditButton').action = 'addInboundConnectionMethod';
                this.down('#connectionStrategyComboBox').setVisible(false);
                this.down('#rescheduleRetryDelayFieldContainer').setVisible(false);
                this.down('#allowSimultaneousConnections').setVisible(false);
                this.down('#comWindowField').setVisible(false);
//                this.down('#isDefault').setVisible(false);
            }
        }
        this.down('#cancelLink').href = this.returnLink;

    }


});


