/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'connectionMethodEditAddTitle',
                layout: {
                    type: 'vbox'
                },

                items: [
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'connectionMethodEditForm',
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
                                name: 'errors',
                                ui: 'form-error-framed',
                                itemId: 'connectionMethodEditFormErrors',
                                layout: 'hbox',
                                margin: '0 0 10 0',
                                hidden: true,
                                defaults: {
                                    xtype: 'container'
                                }
                            },
                            {
                                xtype: 'textfield',
                                name: 'name',
                                msgTarget: 'under',
                                required: true,
                                allowBlank: false,
                                fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                itemId: 'editConnectionMethodNameField',
                                maxLength: 80,
                                enforceMaxLength: true,
                                listeners: {
                                    afterrender: function (field) {
                                        field.focus(false, 200);
                                    }
                                }
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
                                emptyText: Uni.I18n.translate('connectionmethod.selectConnectionType', 'MDC', 'Select a connection type...'),
                                required: true,
                                forceSelection: true,
                                allowBlank: false,
                                typeAhead: true,
                                msgTarget: 'under'
                            },
                            {
                                xtype: 'combobox',
                                name: 'comPortPool',
                                fieldLabel: Uni.I18n.translate('general.comPortPool', 'MDC', 'Communication port pool'),
                                itemId: 'communicationPortPoolComboBox',
                                store: this.comPortPools,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'name',
                                emptyText: Uni.I18n.translate('connectionmethod.selectComPortPool', 'MDC', 'Select a communication port pool...'),
                                forceSelection: true,
                                typeAhead: true,
                                msgTarget: 'under',
                                listeners: {
                                    'change': function (combo, newValue) {
                                        if (newValue === null || newValue === '' )
                                            combo.reset();
                                    }
                                }
                            },
                            {
                                xtype: 'combobox',
                                name: 'connectionStrategy',
                                fieldLabel: Uni.I18n.translate('connectionmethod.connectionStrategy', 'MDC', 'Connection strategy'),
                                itemId: 'connectionStrategyComboBox',
                                store: this.connectionStrategies,
                                queryMode: 'local',
                                required: true,
                                allowBlank: false,
                                displayField: 'localizedValue',
                                valueField: 'connectionStrategy',
                                emptyText: Uni.I18n.translate('connectionmethod.selectconnectionStrategy', 'MDC', 'Select a connection strategy...'),
                                forceSelection: true,
                                typeAhead: true,
                                msgTarget: 'under'
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('connectionmethod.connectionSchedule', 'MDC', 'Connection schedule'),
                                itemId: 'scheduleFieldContainer',
                                hidden: true,
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        value: Uni.I18n.translate('connectionmethod.every', 'MDC', 'Every'),
                                        margin: '0 10 0 0'
                                    },
                                    {
                                        xtype: 'scheduleField',
                                        name: 'temporalExpression',
                                        itemId: 'scheduleField',
                                        hourCfg: {
                                            width: 60
                                        },
                                        minuteCfg: {
                                            width: 60
                                        },
                                        secondCfg: {
                                            width: 60
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('connectionmethod.rescheduleRetryDelay', 'MDC', 'Retry delay'),
                                itemId: 'rescheduleRetryDelayFieldContainer',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'timeInfoField',
                                        store: 'TimeUnitsWithoutMillisecondsAndSeconds',
                                        name: 'rescheduleRetryDelay',
                                        itemId: 'rescheduleRetryDelay',
                                        numberFieldWidth: 70,
                                        unitFieldWidth: 100
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
                                        xtype: 'radiogroup',
                                        itemId: 'activateConnWindowRadiogroup',
                                        vertical: true,
                                        columns: 1,
                                        allowBlank: false,
                                        items: [
                                            {
                                                name: 'enableConnWindow',
                                                boxLabel: Uni.I18n.translate('connectionmethod.norestriction', 'MDC', 'No restrictions'),
                                                inputValue: false,
                                                checked: true,
                                                margin: '0 10 5 0'
                                            },
                                            {
                                                name: 'enableConnWindow',
                                                boxLabel: Uni.I18n.translate('connectionmethod.between', 'MDC', 'Between'),
                                                inputValue: true,
                                                margin: '0 10 5 0'
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'container',
                                        layout: {
                                            type: 'vbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                renderer: function() {
                                                    return ''; // No dash!
                                                }
                                            },
                                            {
                                                xtype: 'container',
                                                layout: {
                                                    type: 'hbox',
                                                    align: 'stretch'
                                                },
                                                margin: '-9 0 0 0',
                                                items: [
                                                    {
                                                        xtype: 'timeInSecondsField',
                                                        name: 'comWindowStart',
                                                        itemId: 'comWindowStart',
                                                        disabled: true

                                                    },
                                                    {
                                                        xtype: 'displayfield',
                                                        value: Uni.I18n.translate('general.and', 'MDC', 'And').toLowerCase(),
                                                        margin: '0 5 0 0'
                                                    },
                                                    {
                                                        xtype: 'timeInSecondsField',
                                                        name: 'comWindowEnd',
                                                        itemId: 'comWindowEnd',
                                                        disabled: true

                                                    }
                                                ]

                                            }

                                        ]
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('deviceconnectionmethod.numberOfSimultaneousConnections', 'MDC', 'Number of simultaneous connections'),
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                itemId:'numberOfSimultaneousConnections',
                                items: [
                                    {

                                        xtype: 'numberfield',
                                        name: 'numberOfSimultaneousConnections',
                                        required: true,
                                        allowDecimals: false,
                                        minValue: 1,
                                        value: 1,
                                        width: 70
                                    }
                                ]
                            }

                        ]
                    },
                    {
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
                this.down('#numberOfSimultaneousConnections').setVisible(false);
                this.down('#comWindowField').setVisible(false);
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
                this.down('#numberOfSimultaneousConnections').setVisible(false);
                this.down('#comWindowField').setVisible(false);
            }
        }
        this.down('#cancelLink').href = this.returnLink;

    }


});


