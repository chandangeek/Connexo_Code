Ext.define('Mdc.view.setup.device.DeviceAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceAdd',
    itemId: 'deviceAdd',
    requires: [
        'Mdc.store.AvailableDeviceTypes',
        'Mdc.store.AvailableDeviceConfigurations',
        'Uni.util.Hydrator'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            layout: 'vbox',
            title: Uni.I18n.translate('deviceAdd.title', 'MDC', 'Add device'),
            itemId: 'deviceAddPanel',

            items: [
                {
                    xtype: 'form',
                    width: 650,
                    itemId: 'editForm',
                    hydrator: 'Uni.util.Hydrator',
                    buttonAlign: 'left',
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
                            itemId: 'addDeviceFormErrors',
                            layout: 'hbox',
                            margin: '0 0 10 0',
                            hidden: true,
                            defaults: {
                                xtype: 'container',
                                margin: '0 0 0 10'
                            }
                        },
                        {
                            xtype: 'textfield',
                            name: 'mRID',
                            itemId: 'deviceAddMRID',
                            fieldLabel: Uni.I18n.translate('deviceAdd.mrid', 'MDC', 'MRID'),
                            required: true,
                            msgTarget: 'under',
                            maxLength: 80,
                            enforceMaxLength: true,
                            validateOnBlur: false,
                            validateOnChange: false
                        },
                        {
                            xtype: 'textfield',
                            name: 'serialNumber',
                            itemId: 'deviceAddSerial',
                            fieldLabel: Uni.I18n.translate('deviceAdd.serialNumber', 'MDC', 'Serial number'),
                            maxLength: 80,
                            enforceMaxLength: true
                        },
                        {
                            xtype: 'combobox',
                            required: true,
                            msgTarget: 'under',
                            itemId: 'deviceAddType',
                            name: 'deviceType',
                            queryMode: 'local',
                            typeAhead: true,
                            autoSelect: true,
                            labelAlign: 'right',
                            fieldLabel: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                            emptyText: Uni.I18n.translate('deviceAdd.type.value', 'MDC', 'Select a device type...'),
                            displayField: 'name',
                            valueField: 'id',
                            store: 'AvailableDeviceTypes',
                            validateOnBlur: false,
                            validateOnChange: false,
                            listConfig:
                                {
                                    loadMask: true,
                                    maxHeight: 300
                                },
                            listeners: {
                                select: function (field, value) {
                                    var configCombo = field.nextSibling(),
                                        valueId;

                                    if (Ext.isArray(value)) {
                                        valueId = value[0].data.id;
                                    } else {
                                        valueId = value.data.id;
                                    }
                                    configCombo.getStore().getProxy().setExtraParam('deviceType', valueId);
                                    if (configCombo.isDisabled()) {
                                        configCombo.enable();
                                    } else {
                                        configCombo.reset();
                                        configCombo.getStore().reload();
                                    }

                                    field.up('form').getRecord().set('deviceTypeId', valueId);
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
                            msgTarget: 'under',
                            itemId: 'deviceAddConfig',
                            name: 'deviceConfiguration',
                            autoSelect: true,
                            enabled: false,
                            labelAlign: 'right',
                            fieldLabel: '&nbsp',
                            emptyText: Uni.I18n.translate('deviceAdd.config.value', 'MDC', 'Select a device configuration...'),
                            afterSubTpl: '<div class="x-form-display-field"><i>'
                                         + Uni.I18n.translate('deviceAdd.firstSelectDeviceType', 'MDC', 'First select a device type.')
                                         + '</i></div>',
                            displayField: 'name',
                            valueField: 'id',
                            disabled: true,
                            store: 'AvailableDeviceConfigurations',
                            validateOnBlur: false,
                            validateOnChange: false,
                            listConfig:
                                {
                                    loadMask: true,
                                    maxHeight: 300
                                },
                            listeners: {
                                select: function (field, value) {
                                    field.up('form').getRecord().set('deviceConfigurationId', value[0].data.id);
                                }
                            }
                        },
                        {
                            xtype: 'datefield',
                            name: 'arrivalDate',
                            itemId: 'deviceAddArrival',
                            maxWidth: 400,
                            editable: false,
                            value: new Date(),
                            fieldLabel: Uni.I18n.translate('deviceAdd.arrivalDate', 'MDC', 'Arrival date'),
                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                        },
                        {
                            xtype: 'combobox',
                            maxWidth: 400,
                            name: 'yearOfCertification',
                            itemId: 'deviceAddCertification',
                            fieldLabel: Uni.I18n.translate('deviceAdd.yearOfCertification', 'MDC', 'Year of certification'),
                            displayField: 'year',
                            valueField: 'year',
                            store: undefined,
                            editable: false,
                            listConfig: { maxHeight: 100 },
                            listeners: {
                                beforerender: function (combo) {
                                    var currentTime = new Date();
                                    var year = currentTime.getFullYear();
                                    var years = [];

                                    for(y=0; y<=20; y++){
                                        years.push([year-y]);
                                    }

                                    var yearStore = new Ext.data.SimpleStore
                                    ({
                                        fields : ['year'],
                                        data : years
                                    });

                                    combo.bindStore(yearStore);
                                    combo.setValue(year);
                                }
                            }
                        },
                        {
                            xtype: 'textfield',
                            name: 'batch',
                            itemId: 'deviceAddBatch',
                            fieldLabel: Uni.I18n.translate('deviceAdd.batch', 'MDC', 'Batch'),
                            maxLength: 80,
                            enforceMaxLength: true
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
                                    action: 'save',
                                    itemId: 'deviceAddSaveButton'
                                },
                                {
                                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                    xtype: 'button',
                                    ui: 'link',
                                    itemId: 'cancelLink',
                                    href: '#/devices/'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
});
