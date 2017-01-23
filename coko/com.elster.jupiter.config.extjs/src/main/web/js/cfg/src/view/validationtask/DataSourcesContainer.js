Ext.define('Cfg.view.validationtask.DataSourcesContainer', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.cfg-data-sources-container',
    layout: 'vbox',
    appName: null,
    edit: false,
    htmlEncode: true,

    initComponent: function () {
        var me = this,
            setValue = function (value) {
                var field = this,
                    combo = new Ext.form.field.ComboBox;

                if (Ext.isObject(value)) {
                    value = value.id;
                }
                combo.setValue.apply(field, [value]);
            };

        switch (me.appName) {
            case 'MultiSense':
                me.items = [
                    {
                        xtype: 'fieldcontainer',
                        layout: {
                            type: 'vbox'
                        },
                        fieldLabel: Uni.I18n.translate('validationTasks.general.deviceGroup', 'CFG', 'Device group'),
                        required: true,
                        items: [
                            {
                                xtype: 'combobox',
                                width: 300,
                                itemId: 'cbo-validation-task-device-group',
                                required: true,
                                editable: false,
                                queryMode: 'local',
                                displayField: 'displayValue',
                                valueField: 'id',
                                name: 'deviceGroup',
                                store: 'Cfg.store.DeviceGroups',
                                emptyText: Uni.I18n.translate('validationTasks.addValidationTask.deviceGroupPrompt', 'CFG', 'Select a device group...'),
                                disabled: !Cfg.privileges.Validation.canAdministrate(),
                                setValue: setValue
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'no-items-defined',
                                hidden: true,
                                value: '<div style="color: #eb5642">' + Uni.I18n.translate('validationTasks.general.noDeviceGroup', 'CFG', 'No device group defined yet.') + '</div>',
                                htmlEncode: false
                            }
                        ]
                    }

                ];
                break;

            case 'MdmApp':
                me.items = [
                    {
                        xtype: 'fieldcontainer',
                        layout: {
                            type: 'vbox'
                        },
                        fieldLabel: Uni.I18n.translate('validationTasks.general.usagePointGroup', 'CFG', 'Usage point group'),
                        required: true,
                        items: [
                            {
                                xtype: 'combobox',
                                width: 300,
                                itemId: 'cbo-validation-task-usage-point-group',
                                required: true,
                                editable: false,
                                queryMode: 'local',
                                displayField: 'displayValue',
                                valueField: 'id',
                                name: 'usagePointGroup',
                                store: 'Cfg.store.UsagePointGroups',
                                emptyText: Uni.I18n.translate('validationTasks.addValidationTask.usagePointGroupPrompt', 'CFG', 'Select a usage point group...'),
                                disabled: !Cfg.privileges.Validation.canAdministrate(),
                                setValue: setValue
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'no-items-defined',
                                hidden: true,
                                value: '<div style="color: #FF0000">' + Uni.I18n.translate('validationTasks.general.noUsagePointGroup', 'CFG', 'No usage point group defined yet.') + '</div>',
                                htmlEncode: false
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('validationTasks.general.purpose', 'CFG', 'Purpose'),
                        itemId: 'purpose-group-container',

                        layout: {
                            type: 'hbox',
                        },
                        items: [
                            {
                                xtype: 'combobox',
                                width: 300,
                                itemId: 'cbo-validation-task-purpose',
                                editable: false,
                                queryMode: 'local',
                                displayField: 'displayValue',
                                valueField: 'id',
                                name: 'metrologyPurpose',
                                store: 'Cfg.store.MetrologyPurposes',
                                emptyText: Uni.I18n.translate('validationTasks.addValidationTask.purposePrompt', 'CFG', 'Select a purpose...'),
                                disabled: !Cfg.privileges.Validation.canAdministrate(),
                                setValue: setValue,
                                listeners: {
                                    change: function () {
                                        me.down('#reset-purpose-btn').enable();
                                    }
                                }
                            },
                            {
                                xtype: 'uni-default-button',
                                itemId: 'reset-purpose-btn',
                                disabled: true,
                                tooltip: Uni.I18n.translate('validationTasks.addValidationTask.reset', 'CFG', 'Reset'),
                                hidden: false
                            }
                        ]
                    }
                ];
                break;
        }
        me.callParent(arguments);
    },

    showNoItemsField: function () {
        var me = this;
        Ext.suspendLayouts();
        switch (me.appName) {
            case 'MultiSense':
                me.down('#cbo-validation-task-device-group').hide();
                break;
            case 'MdmApp' :
                me.down('#cbo-validation-task-usage-point-group').hide();
                me.down('#purpose-group-container').hide();
                break;
        }
        me.down('#no-items-defined').show();
        Ext.resumeLayouts(true);
        me.combineErrors = true;
    },

    setDataSourcesToRecord: function (record) {
        var me = this,
            setToRecord = function (combo) {
                record.set(combo.name, {
                    id: combo.getValue() || 0,
                    displayValue: combo.getDisplayValue()
                });
            };
        switch (me.appName) {
            case 'MultiSense':
                setToRecord(me.down('#cbo-validation-task-device-group'));
                break;
            case 'MdmApp' :
                setToRecord(me.down('#cbo-validation-task-usage-point-group'));
                setToRecord(me.down('#cbo-validation-task-purpose'));
                break;
        }
    }
});

