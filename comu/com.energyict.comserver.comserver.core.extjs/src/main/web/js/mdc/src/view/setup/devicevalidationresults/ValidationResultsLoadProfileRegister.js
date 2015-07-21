Ext.define('Mdc.view.setup.devicevalidationresults.ValidationResultsLoadProfileRegister', {
    extend: 'Ext.container.Container',
    alias: 'widget.mdc-device-validation-results-load-profile-register',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    requires: [
        'Mdc.view.setup.devicevalidationresults.RegisterList',
        'Mdc.view.setup.devicevalidationresults.LoadProfileList'

    ],

    initComponent: function () {
        this.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'form',
                        itemId: 'frm-device-validation-results-load-profile-register',
                        flex: 1,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 150,
                            labelAlign: 'left'
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                itemId: 'dpl-data-view-data-validated',
                                fieldLabel: Uni.I18n.translate('validationResults.dataValidated', 'MDC', 'Data validated'),
                                name: 'allDataValidatedDisplay',
                                htmlEncode: false,
                                value: Uni.I18n.translate('validationResults.updatingStatus', 'MDC', 'Updating status...')
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'dpl-data-view-validation-results',
                                fieldLabel: Uni.I18n.translate('validationResults.validationResults', 'MDC', 'Validation results'),
                                name: 'total',
                                value: Uni.I18n.translate('validationResults.updatingStatus', 'MDC', 'Updating status...')
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'bottom',
                            pack: 'end'
                        },
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'btn-data-view-validate-now',
                                text: Uni.I18n.translate('validationResults.validateNow', 'MDC', 'Validate now'),
                                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.validationActions,
                                disabled: true
                            }
                        ]
                    }

                ]
            },
            {
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                margin: '0 -16 0 -16',
                ui: 'medium',
                itemId: 'con-data-view-validation-results-browse',
                items: [
                    {
                        ui: 'medium',
                        itemId: 'validation-result-load-profile-list',
                        hidden: true,
                        title: Uni.I18n.translate('device.dataValidation.loadProfiles', 'MDC', 'Load profiles'),
                        xtype: 'mdc-load-profile-list'
                    },
                    {
                        ui: 'medium',
                        itemId: 'validation-result-register-list',
                        hidden: true,
                        title: Uni.I18n.translate('device.dataValidation.registers', 'MDC', 'Registers'),
                        xtype: 'mdc-register-list'
                    }

                ]
            }


        ];
        this.callParent(arguments);

    }

});

