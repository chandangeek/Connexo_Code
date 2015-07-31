Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communicationTaskEdit',
    itemId: 'communicationTaskEdit',
    edit: false,

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

    setValues: function (record) {
        var me = this;

        if (!Ext.isEmpty(record.get('comTask'))) {
            me.down('#comTaskDisplayField').setValue(record.get('comTask').name);
        }
        if (!Ext.isEmpty(record.get('securityPropertySet'))) {
            me.down('#securityPropertySetComboBox').setValue(record.get('securityPropertySet').id);
        }
        if (!Ext.isEmpty(record.get('partialConnectionTask'))) {
            me.down('#partialConnectionTaskComboBox').setValue(record.get('partialConnectionTask').id);
        }
        if (!Ext.isEmpty(record.get('protocolDialectConfigurationProperties'))) {
            me.down('#protocolDialectConfigurationPropertiesComboBox').setValue(record.get('protocolDialectConfigurationProperties').id);
            me.down('#protocolDialectConfigurationPropertiesDisplayField').setValue(record.get('protocolDialectConfigurationProperties').name);
        }
        if (!Ext.isEmpty(record.get('priority'))) {
            me.down('#priorityNumberField').setValue(record.get('priority'));
        }
        if (!Ext.isEmpty(record.get('ignoreNextExecutionSpecsForInbound'))) {
            me.down('#ignoreNextExecutionSpecsForInboundRadioGroup').setValue({
                ignoreNextExecutionSpecsForInbound: record.get('ignoreNextExecutionSpecsForInbound')
            });
        }
    },

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                itemId: 'communicationTaskEditForm',
                defaults: {
                    labelWidth: 250
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
                        itemId: 'comTaskDisplayField',
                        hidden: true
                    },
                    {
                        xtype: 'combobox',
                        name: 'comTaskId',
                        fieldLabel: Uni.I18n.translate('communicationtasks.form.comTask', 'MDC', 'Communication task'),
                        itemId: 'comTaskComboBox',
                        store: this.comTasksStore,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id',
                        emptyText: Uni.I18n.translate('communicationtasks.form.selectComTask', 'MDC', 'Select communication task...'),
                        allowBlank: false,
                        forceSelection: true,
                        required: true,
                        editable: false,
                        msgTarget: 'under',
                        width: 600
                    },
                    {
                        xtype: 'combobox',
                        name: 'securityPropertySetId',
                        fieldLabel: Uni.I18n.translate('communicationtasks.form.securityPropertySet', 'MDC', 'Security set'),
                        itemId: 'securityPropertySetComboBox',
                        store: this.securityPropertySetsStore,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id',
                        emptyText: Uni.I18n.translate('communicationtasks.form.selectSecurityPropertySet', 'MDC', 'Select security set...'),
                        allowBlank: false,
                        forceSelection: true,
                        required: true,
                        editable: false,
                        msgTarget: 'under',
                        width: 600
                    },
                    {
                        xtype: 'combobox',
                        name: 'partialConnectionTaskId',
                        fieldLabel: Uni.I18n.translate('communicationtasks.form.partialConnectionTask', 'MDC', 'Connection method'),
                        itemId: 'partialConnectionTaskComboBox',
                        store: this.connectionMethodsStore,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id',
                        emptyText: Uni.I18n.translate('communicationtasks.form.selectPartialConnectionTask', 'MDC', 'Use the default connection method'),
                        forceSelection: true,
                        editable: false,
                        msgTarget: 'under',
                        width: 600
                    },
                    {
                        xtype: 'displayfield',
                        name: 'protocolDialectConfigurationPropertiesName',
                        fieldLabel: Uni.I18n.translate('communicationtasks.form.protocolDialectConfigurationProperties', 'MDC', 'Protocol dialect'),
                        itemId: 'protocolDialectConfigurationPropertiesDisplayField',
                        hidden: true
                    },
                    {
                        xtype: 'combobox',
                        name: 'protocolDialectConfigurationPropertiesId',
                        fieldLabel: Uni.I18n.translate('communicationtasks.form.protocolDialectConfigurationProperties', 'MDC', 'Protocol dialect'),
                        itemId: 'protocolDialectConfigurationPropertiesComboBox',
                        store: this.protocolDialectsStore,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id',
                        emptyText: Uni.I18n.translate('communicationtasks.form.selectProtocolDialectConfigurationProperties', 'MDC', 'Select the protocol dialect'),
                        forceSelection: true,
                        editable: false,
                        msgTarget: 'under',
                        width: 600,
                        required: true,
                        allowBlank: false
                    },
                    {
                        xtype: 'numberfield',
                        name: 'priority',
                        fieldLabel: Uni.I18n.translate('communicationtasks.form.priority', 'MDC', 'Urgency'),
                        itemId: 'priorityNumberField',
                        value: 100,
                        maxValue: 999,
                        minValue: 0,
                        listeners: {
                            blur: {
                                fn: function (field) {
                                    if (Ext.isEmpty(field.getValue())) {
                                        field.setValue(100);
                                    }
                                }
                            }
                        },
                        afterSubTpl: '<div class="x-form-display-field"><i>' + Uni.I18n.translate('communicationtasks.form.priorityMessage', 'MDC', '(Low=999 - High=0)') + '</i></div>'
                    },
                    {
                        xtype: 'radiogroup',
                        fieldLabel: Uni.I18n.translate('communicationtasks.form.ignoreNextExecutionSpecsForInbound', 'MDC', 'Always execute for inbound'),
                        itemId: 'ignoreNextExecutionSpecsForInboundRadioGroup',
                        columns: 1,
                        defaults: {
                            name: 'ignoreNextExecutionSpecsForInbound'
                        },
                        allowBlank: false,
                        required: true,
                        items: [
                            {boxLabel: Uni.I18n.translate('general.yes', 'MDC', 'Yes'), inputValue: true},
                            {boxLabel: Uni.I18n.translate('general.no', 'MDC', 'No'), inputValue: false, checked: true}
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
        ];

        me.callParent(arguments);
        me.setEdit(me.isEdit(), me.returnLink);
    }
});

