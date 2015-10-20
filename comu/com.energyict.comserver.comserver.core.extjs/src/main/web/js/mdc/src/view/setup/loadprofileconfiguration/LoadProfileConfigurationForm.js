Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationForm',
    loadProfileConfigurationAction: null,
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Uni.form.field.Obis',
        'Uni.form.field.ObisDisplay'
    ],
    edit: false,
    cancelLink: undefined,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                width: '100%',
                itemId: 'LoadProfileConfigurationFormId',
                title: !me.edit ? Uni.I18n.translate('loadProfileConfigurations.add', 'MDC', 'Add load profile configuration') : ' ',
                defaults: {
                    labelWidth: 150,
                    validateOnChange: false,
                    validateOnBlur: false,
                    anchor: '50%'
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        name: 'errors',
                        hidden: true,
                        margin: '0 0 32 0'
                    },
                    {
                        xtype: 'displayfield',
                        required: true,
                        fieldLabel: 'Load profile type',
                        name: 'name',
                        value: 'LoadProfileType',
                        hidden: !me.edit
                    },
                    {
                        xtype: 'combobox',
                        itemId: 'load-profile-type-combo',
                        store: 'Mdc.store.LoadProfileConfigurationsOnDeviceConfigurationAvailable',
                        required: true,
                        allowBlank: false,
                        fieldLabel: 'Load profile type',
                        emptyText: Uni.I18n.translate('loadprofileconfiguration.selectLoadProfileType','MDC','Select a load profile type'),
                        name: 'id',
                        displayField: 'name',
                        valueField: 'id',
                        queryMode: 'local',
                        hidden: me.edit,
                        listeners: {
                            change: {
                                fn: me.edit ? undefined : function (combo, newValue) {
                                    var record = combo.findRecordByValue(newValue);

                                    if (record) {
                                        combo.nextSibling('[name=obisCode]').setValue(record.get('obisCode'));
                                    }
                                }
                            }
                        }
                    },
                    {
                        xtype: 'obis-displayfield',
                        name: 'obisCode'
                    },
                    {
                        xtype: 'obis-field',
                        itemId: 'obis-code-field',
                        required: false,
                        fieldLabel: Uni.I18n.translate('general.overruledObisCode', 'MDC', 'Overruled OBIS code'),
                        name: 'overruledObisCode'
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'add-load-profile-config-button',
                                name: 'loadprofileconfigurationaction',
                                action: me.edit ? 'Save' : 'Add',
                                text: me.edit
                                    ? Uni.I18n.translate('general.save', 'MDC', 'Save')
                                    : Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-load-profile-config-button',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                href: me.cancelLink,
                                ui: 'link'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

