/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileConfigurationDetailForm',
    itemId: 'mdc-loadProfileConfigurationDetailForm',
    loadProfileConfigurationChannelAction: null,
    loadProfileConfigurationId: null,
    deviceConfigurationId: null,
    deviceTypeId: null,
    requires: [
        'Uni.form.field.Obis',
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
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('channelConfig.registerType', 'MDC', 'Register type'),
                        itemId: 'mdc-channel-config-registerTypeComboBox',
                        queryMode: 'local',
                        allowBlank: false,
                        displayField: 'name',
                        valueField: 'id',
                        emptyText: Uni.I18n.translate('channelConfig.selectRegisterType', 'MDC', 'Select a register type...'),
                        required: true,
                        forceSelection: true,
                        editable: false,
                        submitValue: false,
                        msgTarget: 'under'
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'obis-code-container',
                        required: true,
                        width: 450,
                        layout: 'hbox',
                        fieldLabel: Uni.I18n.translate('channelConfig.obisCode', 'MDC', 'OBIS code'),
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
                    {
                        xtype: 'numberfield',
                        name: 'overflowValue',
                        msgTarget: 'under',
                        fieldLabel: Uni.I18n.translate('channelConfig.overflowValue', 'MDC', 'Overflow value'),
                        itemId: 'mdc-lpcfg-detailForm-overflow-value-field',
                        allowBlank: false,
                        width: 450,
                        hideTrigger: true,
                        maxLength: 15, // don't increase this value. Javascript can't handle precise values larger than 9007199254740992
                        enforceMaxLength: true,
                        required: true,
                        minValue: 1
                    },
                    {
                        xtype: 'numberfield',
                        fieldLabel: Uni.I18n.translate('channelConfig.numberOfFractionDigits', 'MDC', 'Number of fraction digits'),
                        name: 'nbrOfFractionDigits',
                        itemId: 'nr-of-fraction-digits',
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
                        disabled: true,
                        fieldLabel: Uni.I18n.translate('channelConfig.useMultiplier', 'MDC', 'Use multiplier'),
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
                        fieldLabel: Uni.I18n.translate('general.calculatedReadingType', 'MDC', 'Calculated reading type'),
                        required: true,
                        width: 650,
                        hidden: true,
                        submitValue: false
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
            case 'add':  return Uni.I18n.translate('general.add', 'MDC', 'Add');
            case 'edit': return Uni.I18n.translate('general.save', 'MDC', 'Save');
            default:     return this.loadProfileConfigurationChannelAction;
        }
    },

    isEdit: function() {
        return this.loadProfileConfigurationChannelAction === 'edit';
    }
});

