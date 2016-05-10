Ext.define('Cfg.view.validationtask.DataSourcesContainer', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.cfg-data-sources-container',
    required: true,
    layout: 'vbox',
    appName: null,
    msgTarget: 'under',
    defaults: {
        width: 300
    },
    initComponent: function () {
        var me = this;

        switch (me.appName) {
            case 'MultiSense':
                me.fieldLabel = Uni.I18n.translate('validationTasks.general.deviceGroup', 'CFG', 'Device group');
                me.items = [
                    {
                        xtype: 'combobox',
                        itemId: 'cbo-validation-task-device-group',
                        required: true,
                        editable: false,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id',
                        name: 'deviceGroup',
                        store: 'Cfg.store.DeviceGroups',
                        emptyText: Uni.I18n.translate('validationTasks.addValidationTask.deviceGroupPrompt', 'CFG', 'Select a device group...'),
                        disabled: !Cfg.privileges.Validation.canAdministrate(),
                        setValue: function (value) {
                            var field = this,
                                combo = new Ext.form.field.ComboBox;

                            if (Ext.isObject(value)) {
                                value = value.id;
                            }
                            combo.setValue.apply(field, [value]);
                        }
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'no-items-defined',
                        hidden: true,
                        value: '<div style="color: #FF0000">' + Uni.I18n.translate('validationTasks.general.noDeviceGroup', 'CFG', 'No device group defined yet.') + '</div>',
                        htmlEncode: false
                    }
                ];
                break;

            case 'MdmApp':
                me.fieldLabel = Uni.I18n.translate('validationTasks.general.metrologyConfiguration', 'CFG', 'Metrology configuration');
                me.items = [
                    {
                        xtype: 'combobox',
                        itemId: 'cbo-validation-task-up-metrology-configuration',
                        required: true,
                        editable: false,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id',
                        name: 'metrologyConfiguration',
                        store: 'Cfg.store.MetrologyConfigurations',
                        emptyText: Uni.I18n.translate('validationTasks.addValidationTask.selectMetrologyConfiguration', 'CFG', 'Select a metrology configuration...'),
                        setValue: function (value) {
                            var field = this,
                                combo = new Ext.form.field.ComboBox;

                            if (Ext.isObject(value)) {
                                value = value.id;
                            }
                            combo.setValue.apply(field, [value]);
                        },
                        listeners: {
                            select: function (field, value) {
                                var configCombo = field.nextSibling(),
                                    valueId;

                                if (Ext.isArray(value)) {
                                    valueId = value[0].get('id');
                                } else {
                                    valueId = value.get('id');
                                }
                                configCombo.getStore().getProxy().setUrl(valueId);
                                if (configCombo.isDisabled()) {
                                    configCombo.enable();
                                } else {
                                    configCombo.reset();
                                }
                                configCombo.getStore().reload();
                            },
                            change: function (field, value) {
                                var configCombo = field.nextSibling();
                                if (value == '') {
                                    configCombo.reset();
                                    configCombo.disable();
                                }
                            },
                            blur: function (field) {
                                if (!field.getValue()) {
                                    field.nextSibling().reset();
                                    field.nextSibling().disable();
                                } else {
                                    Ext.Array.each(field.getStore().getRange(), function (item) {
                                        if (field.getValue() == item.get('name')) {
                                            field.fireEvent('select', field, item);
                                        }
                                    });
                                }
                            }
                        }

                    },
                    {
                        xtype: 'combobox',
                        itemId: 'cbo-validation-task-up-metrology-contract',
                        autoSelect: true,
                        disabled: true,
                        editable: false,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id',
                        name: 'metrologyContract',
                        store: 'Cfg.store.MetrologyContracts',
                        emptyText: Uni.I18n.translate('validationTasks.addValidationTask.selectPurpose', 'CFG', 'Select a purpose...'),
                        afterSubTpl: '<div style="color: #686868; margin-top: 6px"><i>'
                        + Uni.I18n.translate('deviceAdd.firstSelectMetrologyConfig', 'MDC', 'First select a metrology configuration.')
                        + '</i></div>',
                        setValue: function (value) {
                            var field = this,
                                combo = new Ext.form.field.ComboBox;

                            if (Ext.isObject(value)) {
                                value = value.id;
                            }
                            combo.setValue.apply(field, [value]);
                        }
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'no-items-defined',
                        hidden: true,
                        value: '<div style="color: #FF0000">' + Uni.I18n.translate('validationTasks.general.noMetrologyConfigYet', 'CFG', 'No metrology configuration defined yet.') + '</div>',
                        htmlEncode: false
                    }
                ];
                break;


        }
        me.callParent(arguments);
    },

    showNoItemsField: function () {
        var me = this;
        switch (me.appName) {
            case 'MultiSense':
                me.down('#cbo-validation-task-device-group').hide();

                break;
            case 'MdmApp' :
                me.down('#cbo-validation-task-up-metrology-configuration').hide();
                me.down('#cbo-validation-task-up-metrology-contract').hide();
                break;
        }
        me.down('#no-items-defined').show();
        me.combineErrors = true;
    },

    setDataSourcesToRecord: function (record) {
        var me = this,
            setToRecord = function (combo) {
                record.set(combo.name, {
                    id: combo.getValue(),
                    name: combo.getDisplayValue()
                });
            };
        switch (me.appName) {
            case 'MultiSense':
                setToRecord(me.down('#cbo-validation-task-device-group'));
                break;
            case 'MdmApp' :
                setToRecord(me.down('#cbo-validation-task-up-metrology-configuration'));
                setToRecord(me.down('#cbo-validation-task-up-metrology-contract'));
                break;
        }
    }
});

