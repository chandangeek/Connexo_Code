Ext.define('Mdc.view.setup.device.DeviceAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceAdd',
    itemId: 'deviceAdd',
    requires: [
        'Mdc.store.DeviceTypes',
        'Mdc.store.DeviceConfigurations'
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
                        xtype: 'textfield',
                        labelWidth: 250
                    },
                    items: [
                        {
                            name: 'mRID',
                            itemId: 'deviceAddMRID',
                            fieldLabel: Uni.I18n.translate('deviceAdd.mrid', 'MDC', 'MRID'),
                            required: true,
                            msgTarget: 'under',
                            maxLength: 80,
                            enforceMaxLength: true
                        },
                        {
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
                            autoSelect: true,
                            labelAlign: 'right',
                            fieldLabel: Uni.I18n.translate('deviceAdd.type.label', 'MDC', 'Device type'),
                            emptyText: Uni.I18n.translate('deviceAdd.type.value', 'MDC', 'Select a device type'),
                            displayField: 'name',
                            valueField: 'id',
                            store: 'DeviceTypes',
                            listConfig:
                                {
                                    loadMask: false,
                                    maxHeight: 300
                                },
                            listeners: {
                                select: function (field, value) {
                                    var configCombo = field.nextSibling();

                                    configCombo.getStore().getProxy().setExtraParam('deviceType', value[0].data.id);
                                    if (configCombo.isDisabled()) {
                                        configCombo.enable();
                                    }
                                    else {
                                        configCombo.reset();
                                        configCombo.getStore().reload();
                                    }

                                    field.up('form').getRecord().set('deviceTypeId', value[0].data.id);
                                }
                            }
                        },
                        {
                            xtype: 'combobox',
                            required: true,
                            msgTarget: 'under',
                            itemId: 'deviceAddConfig',
                            name: 'deviceConfiguration',
                            autoSelect: true,
                            enabled: false,
                            labelAlign: 'right',
                            fieldLabel: Uni.I18n.translate('deviceAdd.config.label', 'MDC', 'Device configuration'),
                            emptyText: Uni.I18n.translate('deviceAdd.config.value', 'MDC', 'Select a device configuration'),
                            displayField: 'name',
                            valueField: 'id',
                            disabled: true,
                            store: 'DeviceConfigurations',
                            listConfig:
                                {
                                    loadMask: false,
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
                            fieldLabel: Uni.I18n.translate('deviceAdd.arrivalDate', 'MDC', 'Arrival date')
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
                            name: 'batch',
                            itemId: 'deviceAddBatch',
                            fieldLabel: Uni.I18n.translate('deviceAdd.batch', 'MDC', 'Batch'),
                            maxLength: 80,
                            enforceMaxLength: true
                        }
                    ],
                    buttons: [
                        {
                            ui: 'action',
                            action: 'save',
                            itemId: 'deviceAddSaveButton',
                            //margin: '0 0 0 10',
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add')
                        },
                        {
                            ui: 'link',
                            action: 'cancel',
                            itemId: 'deviceAddCancelButton',
                            itemId: 'cancelLink',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
                        }
                    ]
                }
            ]
        }
    ]
});
