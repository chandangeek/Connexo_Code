Ext.define('Ddv.view.validations.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.ddv-validations-preview',
    requires: [],
    tools: [],

    items: [
        {
            xtype: 'form',
            itemId: 'validations-details-form',
            layout: 'column',
            items: [
                {
                    xtype: 'container',
                    columnWidth: 0.5,
                    layout: 'form',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 250
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('validations.serialNumber', 'DDV', 'Serial number'),
                            name: 'serialNumber',
                            itemId: 'serial-number-validations-preview',
                            renderer: function (value) {
                                return value ? Ext.String.htmlEncode(value) : null;
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('validations.deviceType', 'DDV', 'Device type'),
                            name: 'deviceType',
                            itemId: 'deviceType-validations-preview'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('validations.configuration', 'DDV', 'Configuration'),
                            name: 'configuration',
                            itemId: 'configuration-validations-preview'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('validations.allDataValidated', 'DDV', 'All data validated'),
                            name: 'allDataValidated',
                            itemId: 'all-dataV-validated-validations-preview'
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('validations.suspectReadings', 'DDV', 'Suspect readings'),
                            labelAlign: 'top',
                            layout: 'vbox',
                            defaults: {
                                xtype: 'displayfield',
                                labelWidth: 250
                            },
                            items: [
                                {
                                    fieldLabel: Uni.I18n.translate('validations.registers', 'DDV', 'Registers'),
                                    name: 'registers',
                                    itemId: 'registers-number-validations-preview'
                                },
                                {
                                    fieldLabel: Uni.I18n.translate('validations.channels', 'DDV', 'Channels'),
                                    name: 'channels',
                                    itemId: 'channels-validations-preview'
                                },
                                {
                                    name: 'lastSuspect',
                                    itemId: 'last-suspect-validations-preview',
                                    fieldLabel: Uni.I18n.translate('validations.lastSuspect', 'DDV', 'Last suspect'),
                                    renderer: function (value) {
                                        if (value) {
                                            return Uni.DateTime.formatDateTimeLong(value);
                                        } else {
                                            return '-';
                                        }
                                    }
                                },
                            ]
                        }
                    ]
                },
                {
                    xtype: 'container',
                    columnWidth: 0.5,
                    layout: 'form',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 250
                    },
                    items: [
                        {
                            name: 'lastValidation',
                            itemId: 'last-validation-validations-preview',
                            fieldLabel: Uni.I18n.translate('validations.lastValidation', 'DDV', 'Last validation'),
                            renderer: function (value) {
                                if (value) {
                                    return Uni.DateTime.formatDateTimeLong(value);
                                } else {
                                    return '-';
                                }
                            }
                        },
                        {
                            itemId: 'type-of-suspects-validations-preview',
                            fieldLabel: Uni.I18n.translate('validations.typeOfSuspects', 'DDV', 'Type of suspects'),
                            renderer: function (value, field) {
                                return value;
                            }
                        }
                    ]
                }

            ]
        }
    ]
});