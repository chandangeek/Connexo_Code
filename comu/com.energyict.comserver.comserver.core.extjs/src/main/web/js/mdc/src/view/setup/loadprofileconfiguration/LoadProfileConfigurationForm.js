/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationForm',
    loadProfileConfigurationAction: null,
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Uni.form.field.Obis',
        'Uni.util.FormErrorMessage'
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
                    labelWidth: 250,
                    validateOnChange: false,
                    validateOnBlur: false
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        name: 'errors',
                        hidden: true,
                        margin: '0 0 32 0',
                        width: 650
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
                        forceSelection: !me.edit,
                        //fieldLabel: 'Load profile type',
                        fieldLabel: Uni.I18n.translate('general.loadProfileType', 'MDC', 'Load profile type'),
                        emptyText: Uni.I18n.translate('loadprofileconfiguration.selectLoadProfileType','MDC','Select a load profile type'),
                        name: 'id',
                        displayField: 'name',
                        valueField: 'id',
                        queryMode: 'local',
                        hidden: me.edit,
                        width: 650,
                        listeners: {
                            change: {
                                fn: me.edit ? undefined : function (combo, newValue) {
                                    var record = combo.findRecordByValue(newValue);
                                    if (record) {
                                        me.down('#obis-code-field').setValue(record.get('obisCode'));
                                    }
                                }
                            },
                            afterrender: function (field) {
                                field.focus(false, 200);
                            }
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        required: true,
                        width: 450,
                        layout: 'hbox',
                        fieldLabel: Uni.I18n.translate('registerConfig.obisCode', 'MDC', 'OBIS code'),
                        items: [
                            {
                                xtype: 'obis-field',
                                name: 'overruledObisCode',
                                itemId: 'obis-code-field',
                                fieldLabel: '',
                                required: false,
                                afterSubTpl: null,
                                allowBlank: false,
                                width: 150
                            },
                            {
                                xtype: 'uni-default-button',
                                itemId: 'mdc-restore-obiscode-btn',
                                hidden: false,
                                disabled: true
                            }
                        ]
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

