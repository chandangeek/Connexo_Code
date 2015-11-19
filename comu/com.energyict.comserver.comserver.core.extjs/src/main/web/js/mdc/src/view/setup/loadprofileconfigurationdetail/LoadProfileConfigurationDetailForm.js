Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationDetailForm',
    loadProfileConfigurationChannelAction: null,
    loadProfileConfigurationId: null,
    deviceConfigurationId: null,
    deviceTypeId: null,
    requires: [
        'Uni.form.field.Obis',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.form.field.ReadingTypeCombo'
    ],

    initComponent: function () {
        var me = this;
        me.content = [
            {
                ui: 'large',
                title: Uni.I18n.translate('channel.channelConfiguration','MDC','Channel configuration'),
                xtype: 'form',
                width: '100%',
                itemId: 'loadProfileConfigurationDetailChannelFormId',
                defaults: {
                    labelWidth: 250,
                    validateOnChange: false,
                    validateOnBlur: false,
                    width: 650
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        name: 'errors',
                        hidden: true,
                        margin: '0 0 32 0'
                    },
                    {
                        xtype: 'reading-type-displayfield',
                        name: 'readingType',
                        hidden: true
                    },
                    {
                        xtype: 'combobox',
                        name: 'registerType',
                        fieldLabel: Uni.I18n.translate('registerConfig.registerType', 'MDC', 'Register type'),
                        itemId: 'mdc-channel-config-registerTypeComboBox',
                        queryMode: 'local',
                        allowBlank: false,
                        displayField: 'name',
                        valueField: 'id',
                        emptyText: Uni.I18n.translate('registerConfig.selectRegisterType', 'MDC', 'Select a register type...'),
                        required: true,
                        forceSelection: true,
                        editable: false,
                        msgTarget: 'under'
                    },
                    //{
                    //    xtype: 'reading-type-combo',
                    //    itemId: 'reading-type-field',
                    //    required: true,
                    //    allowBlank: false,
                    //    submitValue: false
                    //},
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
                                itemId: 'mdc-channel-config-editOverruledObisCodeField',
                                fieldLabel: '',
                                required: false,
                                afterSubTpl: null,
                                width: 150
                            },
                            {
                                xtype: 'uni-default-button',
                                itemId: 'mdc-channel-config-restore-obiscode-btn',
                                hidden: false,
                                disabled: true
                            }
                        ]
                    },
                    //{
                    //    xtype: 'reading-type-displayfield',
                    //    name: 'calculatedReadingType',
                    //    fieldLabel: Uni.I18n.translate('channelConfig.channelConfigForm.calculatedReadingType', 'MDC', 'Calculated reading type'),
                    //    required: true,
                    //    submitValue: false,
                    //    hidden: true
                    //},
                    //{
                    //    xtype: 'obis-displayfield',
                    //    name: 'obiscode',
                    //    value: Uni.I18n.translate('channelConfig.channelConfigForm.obisCodeEmptyTxt', 'MDC', 'Select a reading type first')
                    //},
                    //{
                    //    xtype: 'obis-field',
                    //    required: false,
                    //    name: 'overruledObisCode',
                    //    fieldLabel: Uni.I18n.translate('general.overruledObisCode', 'MDC', 'Overruled OBIS code')
                    //},
                    {
                        xtype: 'numberfield',
                        name: 'overflowValue',
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('registerConfig.overflowValue', 'MDC', 'Overflow value'),
                        itemId: 'mdc-lpcfg-detailForm-overflow-value-field',
                        allowBlank: false,
                        width: 450,
                        maxValue: 2147483647,
                        hideTrigger: true,
                        maxLength: 22,
                        enforceMaxLength: true,
                        required: true,
                        minValue: 1
                    },
                    {
                        xtype: 'numberfield',
                        fieldLabel: Uni.I18n.translate('registerConfig.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                        name: 'nbrOfFractionDigits',
                        required: true,
                        value: 0,
                        minValue: 0,
                        maxValue: 6,
                        maxLength: 1,
                        enforceMaxLength: true,
                        width: 450
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'mdc-channel-config-multiplierRadioGroup',
                        fieldLabel: Uni.I18n.translate('registerConfig.useMultiplier', 'MDC', 'Use multiplier'),
                        columns: 1,
                        defaults: {
                            name: 'useMultiplier'
                        },
                        allowBlank: false,
                        required: true,
                        items: [
                            {
                                boxLabel: Uni.I18n.translate('general.yes', 'MDC', 'Yes'),
                                itemId: 'mdc-channel-config-multiplierRadio',
                                inputValue: true
                            },
                            {
                                boxLabel: Uni.I18n.translate('general.no', 'MDC', 'No'),
                                itemId: 'mdc-channel-config-noMultiplierRadio',
                                inputValue: false,
                                checked: true
                            }
                        ]
                    },
                    {
                        xtype: 'reading-type-displayfield',
                        itemId: 'mdc-channel-config-collected-readingType-field',
                        fieldLabel: Uni.I18n.translate('general.collectedReadingType', 'MDC', 'Collected reading type'),
                        name: 'collectedReadingType',
                        hidden: true
                    },
                    {
                        xtype: 'reading-type-displayfield',
                        itemId: 'mdc-channel-config-calculated-readingType-field',
                        fieldLabel: Uni.I18n.translate('general.calculatedReadingType', 'MDC', 'Calculated reading type'),
                        name: 'calculatedReadingType',
                        hidden: true
                    },
                    {
                        xtype: 'reading-type-combo',
                        itemId: 'mdc-channel-config-calculated-readingType-combo',
                        name: 'calculatedReadingType',
                        tpl: null,
                        displayTpl: null,
                        fieldLabel: Uni.I18n.translate('general.calculatedReadingType', 'MDC', 'Calculated reading type'),
                        displayField: 'fullAliasName',
                        required: true,
                        width: 650,
                        hidden: true
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
                                xtype: 'container',
                                itemId: 'LoadProfileChannelAction'
                            },
                            {
                                xtype: 'container',
                                itemId: 'LoadProfileChannelCancel'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
        Ext.suspendLayouts();
        me.down('#LoadProfileChannelAction').add(
            {
                xtype: 'button',
                itemId: 'add-chanel-configuration-btn',
                name: 'loadprofilechannelaction',
                text: me.determineActionText(),
                action: me.loadProfileConfigurationChannelAction,
                ui: 'action'
            }
        );
        me.down('#LoadProfileChannelCancel').add(
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
                href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofiles/' + me.loadProfileConfigurationId + '/channels',
                ui: 'link'
            }
        );
        Ext.resumeLayouts();
    },

    determineActionText: function() {
        switch(this.loadProfileConfigurationChannelAction) {
            case 'Add':  return Uni.I18n.translate('general.add', 'MDC', 'Add');
            case 'Save': return Uni.I18n.translate('general.save', 'MDC', 'Save');
            default:     return this.loadProfileConfigurationChannelAction;
        }
    }
});

