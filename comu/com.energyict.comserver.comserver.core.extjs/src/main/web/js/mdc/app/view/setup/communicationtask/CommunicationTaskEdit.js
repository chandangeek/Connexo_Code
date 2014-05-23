Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communicationTaskEdit',
    itemId: 'communicationTaskEdit',
    edit: false,

    requires: [
        'Mdc.widget.ScheduleField'
    ],

    isEdit: function () {
        return this.edit
    },

    setEdit: function (edit, returnLink) {
        var me = this;
        me.edit = edit;
        if (me.isEdit()) {
            me.down('#addEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            me.down('#addEditButton').action = 'editCommunicationTaskAction';
            me.down('#comTaskComboBox').hide();
            me.down('#protocolDialectConfigurationPropertiesComboBox').hide();
            me.down('#comTaskComboBox').setDisabled(true);
            me.down('#protocolDialectConfigurationPropertiesComboBox').setDisabled(true);
            me.down('#comTaskDisplayField').show();
            me.down('#protocolDialectConfigurationPropertiesDisplayField').show();
        } else {
            me.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            me.down('#addEditButton').action = 'addCommunicationTaskAction';
            me.down('#comTaskComboBox').setDisabled(false);
            me.down('#protocolDialectConfigurationPropertiesComboBox').setDisabled(false);
            me.down('#comTaskComboBox').show();
            me.down('#protocolDialectConfigurationPropertiesComboBox').show();
            me.down('#comTaskDisplayField').hide();
            me.down('#protocolDialectConfigurationPropertiesDisplayField').hide();
        }
        me.down('#cancelLink').href = returnLink;
    },

    setValues: function(record) {
        var me = this;

        if(!Ext.isEmpty(record.get('comTask'))) {
            me.down('#comTaskDisplayField').setValue(record.get('comTask').name);
        }
        if(!Ext.isEmpty(record.get('securityPropertySet'))) {
            me.down('#securityPropertySetComboBox').setValue(record.get('securityPropertySet').id);
        }
        if(!Ext.isEmpty(record.get('partialConnectionTask'))) {
            me.down('#partialConnectionTaskComboBox').setValue(record.get('partialConnectionTask').id);
        }
        if(!Ext.isEmpty(record.get('protocolDialectConfigurationProperties'))) {
            me.down('#protocolDialectConfigurationPropertiesDisplayField').setValue(record.get('protocolDialectConfigurationProperties').name);
        }
        if(!Ext.isEmpty(record.get('priority'))) {
            me.down('#priorityNumberField').setValue(record.get('priority'));
        }
        if(!Ext.isEmpty(record.get('nextExecutionSpecs'))) {
            me.down('#enableScheduleFieldItem').setValue(true);
            me.down('#scheduleFieldItem').setValue(record.get('nextExecutionSpecs'));
        }
        if(!Ext.isEmpty(record.get('ignoreNextExecutionSpecsForInbound'))) {
            me.down('#ignoreNextExecutionSpecsForInboundRadioGroup').setValue({
                ignoreNextExecutionSpecsForInbound : record.get('ignoreNextExecutionSpecsForInbound')
            });
        }
    },

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'communicationTaskEditAddTitle'
                    },
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column'
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                items: [
                                    {
                                        xtype: 'form',
                                        border: false,
                                        itemId: 'communicationTaskEditForm',
                                        layout: {
                                            type: 'vbox',
                                            align: 'stretch'
                                        },
                                        defaults: {
                                            labelWidth: 150,
                                            labelAlign: 'right'
                                        },
                                        items: [
                                            {
                                                name: 'errors',
                                                ui: 'form-error-framed',
                                                itemId: 'communicationTaskEditFormErrors',
                                                layout: 'hbox',
                                                margin: '0 0 10 0',
                                                hidden: true,
                                                defaults: {
                                                    xtype: 'container'
                                                }
                                            },
                                            {
                                                xtype: 'displayfield',
                                                name: 'comTaskName',
                                                fieldLabel: Uni.I18n.translate('communicationtasks.form.comTask', 'MDC', 'Communication task'),
                                                labelSeparator: ' ',
                                                itemId: 'comTaskDisplayField',
                                                hidden: true
                                            },
                                            {
                                                xtype: 'combobox',
                                                name: 'comTaskId',
                                                fieldLabel: Uni.I18n.translate('communicationtasks.form.comTask', 'MDC', 'Communication task'),
                                                labelSeparator: ' *',
                                                itemId: 'comTaskComboBox',
                                                store: this.comTasksStore,
                                                queryMode: 'local',
                                                displayField: 'name',
                                                valueField: 'id',
                                                emptyText: Uni.I18n.translate('communicationtasks.form.selectComTask', 'MDC', 'Select communication task...'),
                                                allowBlank: false,
                                                forceSelection: true,
                                                msgTarget: 'under'
                                            },
                                            {
                                                xtype: 'combobox',
                                                name: 'securityPropertySetId',
                                                fieldLabel: Uni.I18n.translate('communicationtasks.form.securityPropertySet', 'MDC', 'Security set'),
                                                labelSeparator: ' *',
                                                itemId: 'securityPropertySetComboBox',
                                                store: this.securityPropertySetsStore,
                                                queryMode: 'local',
                                                displayField: 'name',
                                                valueField: 'id',
                                                emptyText: Uni.I18n.translate('communicationtasks.form.selectSecurityPropertySet', 'MDC', 'Select security set...'),
                                                allowBlank: false,
                                                forceSelection: true,
                                                msgTarget: 'under'
                                            },
                                            {
                                                xtype: 'combobox',
                                                name: 'partialConnectionTaskId',
                                                fieldLabel: Uni.I18n.translate('communicationtasks.form.partialConnectionTask', 'MDC', 'Connection method'),
                                                labelSeparator: ' ',
                                                itemId: 'partialConnectionTaskComboBox',
                                                store: this.connectionMethodsStore,
                                                queryMode: 'local',
                                                displayField: 'name',
                                                valueField: 'id',
                                                emptyText: Uni.I18n.translate('communicationtasks.form.selectPartialConnectionTask', 'MDC', 'Use the default connection method'),
                                                forceSelection: true,
                                                msgTarget: 'under'
                                            },
                                            {
                                                xtype: 'displayfield',
                                                name: 'protocolDialectConfigurationPropertiesName',
                                                fieldLabel: Uni.I18n.translate('communicationtasks.form.protocolDialectConfigurationProperties', 'MDC', 'Protocol dialect'),
                                                labelSeparator: ' ',
                                                itemId: 'protocolDialectConfigurationPropertiesDisplayField',
                                                hidden: true
                                            },
                                            {
                                                xtype: 'combobox',
                                                name: 'protocolDialectConfigurationPropertiesId',
                                                fieldLabel: Uni.I18n.translate('communicationtasks.form.protocolDialectConfigurationProperties', 'MDC', 'Protocol dialect'),
                                                labelSeparator: ' ',
                                                itemId: 'protocolDialectConfigurationPropertiesComboBox',
                                                store: this.protocolDialectsStore,
                                                queryMode: 'local',
                                                displayField: 'name',
                                                valueField: 'id',
                                                emptyText: Uni.I18n.translate('communicationtasks.form.selectProtocolDialectConfigurationProperties', 'MDC', 'Use the default protocol dialect'),
                                                forceSelection: true,
                                                msgTarget: 'under'
                                            },
                                            {
                                                xtype: 'numberfield',
                                                name: 'priority',
                                                fieldLabel: Uni.I18n.translate('communicationtasks.form.priority', 'MDC', 'Urgency'),
                                                labelSeparator: ' ',
                                                itemId: 'priorityNumberField',
                                                value: 100,
                                                maxValue: 999,
                                                minValue: 0
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                columnWidth: 0.5,
                                                fieldLabel: ' ',
                                                labelSeparator: ' ',
                                                layout: {
                                                    type: 'vbox'
                                                },
                                                hidden: false,
                                                itemId: 'priorityMessage',
                                                items: [
                                                    {
                                                        xtype: 'component',
                                                        cls: 'x-form-display-field',
                                                        html: '<i>' + Uni.I18n.translate('communicationtasks.form.priorityMessage', 'MDC', '(Low=999 - High=0)') + '</i>'
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                itemId: 'scheduleField',
                                                fieldLabel: Uni.I18n.translate('communicationtasks.form.nextExecutionSpecs', 'MDC', 'Default schedule'),
                                                labelSeparator: ' ',
                                                layout: {
                                                    type: 'hbox',
                                                    align: 'stretch'
                                                },
                                                items: [
                                                    {
                                                        xtype: 'checkbox',
                                                        name: 'nextExecutionSpecsEnable',
                                                        itemId: 'enableScheduleFieldItem',
                                                        uncheckedValue : false,
                                                        boxLabel: Uni.I18n.translate('communicationtasks.form.repeat', 'MDC', 'Repeat every'),
                                                        boxLabelAlign: 'after'
                                                    },
                                                    {
                                                        xtype: 'scheduleField',
                                                        name: 'nextExecutionSpecs',
                                                        margin: '0 0 0 5',
                                                        itemId: 'scheduleFieldItem',
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
                                                xtype: 'radiogroup',
                                                fieldLabel: Uni.I18n.translate('communicationtasks.form.ignoreNextExecutionSpecsForInbound', 'MDC', 'Always execute for inbound'),
                                                labelSeparator: ' *',
                                                itemId: 'ignoreNextExecutionSpecsForInboundRadioGroup',
                                                columns: 1,
                                                defaults: {
                                                    name: 'ignoreNextExecutionSpecsForInbound'
                                                },
                                                allowBlank:false,
                                                items: [
                                                    {boxLabel: Uni.I18n.translate('general.yes', 'MDC', 'Yes'), inputValue: false},
                                                    {boxLabel: Uni.I18n.translate('general.no', 'MDC', 'No'), inputValue: true, checked: true}
                                                ]
                                            },
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
                                                        action: 'addCommunicationTaskAction',
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
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
        me.setEdit(me.isEdit(), me.returnLink);
    }
});

