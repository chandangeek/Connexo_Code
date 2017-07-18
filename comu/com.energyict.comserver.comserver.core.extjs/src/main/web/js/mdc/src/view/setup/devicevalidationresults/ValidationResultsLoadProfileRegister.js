/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicevalidationresults.ValidationResultsLoadProfileRegister', {
    extend: 'Ext.container.Container',
    alias: 'widget.mdc-device-validation-results-load-profile-register',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    store: 'Mdc.store.DeviceValidationResults',
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    requires: [
        'Mdc.view.setup.devicevalidationresults.RegisterList',
        'Mdc.view.setup.devicevalidationresults.LoadProfileList',
        'Mdc.store.LoadProfilesOfDevice'

    ],
    router: null,
    loadProfiles: null,
    intervalRegisterStart: null,
    intervalRegisterEnd: null,
    mainView: null,

    initComponent: function () {
        var me = this;

        me.bindStore(me.store || 'ext-empty-store', true);
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
                                fieldLabel: Uni.I18n.translate('validationResults.allDataValidated', 'MDC', 'All data validated'),
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
                        xtype: 'mdc-load-profile-list',
                        router: me.router
                    },
                    {
                        ui: 'medium',
                        itemId: 'validation-result-register-list',
                        hidden: true,
                        title: Uni.I18n.translate('general.registers', 'MDC', 'Registers'),
                        xtype: 'mdc-register-list',
                        router: me.router
                    }

                ]
            }


        ];
        me.callParent(arguments);
        me.mainView = Ext.ComponentQuery.query('#contentPanel')[0];
        me.on('beforedestroy', me.onBeforeDestroy, me);
    },

    getStoreListeners: function () {
        return {
            beforeload: this.onBeforeLoad,
            load: this.onLoad
        };
    },

    onBeforeLoad: function (store, options) {
        var me = this,
            filter = options.params && options.params.filter ? Ext.decode(options.params.filter) : null,
            loadProfiles,
            intervalRegisterStart,
            intervalRegisterEnd;

        me.mainView.setLoading();

        if (filter) {
            loadProfiles = Ext.Array.findBy(filter, function (value) {
                return value.property === 'intervalLoadProfile'
            });
            if (loadProfiles) {
                me.loadProfiles = loadProfiles.value;
            }

            intervalRegisterStart = Ext.Array.findBy(filter, function (value) {
                return value.property === 'intervalRegisterStart'
            });
            if (intervalRegisterStart) {
                me.intervalRegisterStart = intervalRegisterStart.value;
            }

            intervalRegisterEnd = Ext.Array.findBy(filter, function (value) {
                return value.property === 'intervalRegisterEnd'
            });
            if (intervalRegisterEnd) {
                me.intervalRegisterEnd = intervalRegisterEnd.value;
            }
        }
    },

    onLoad: function (store, records, success) {
        var me = this,
            record = records.length ? records[0] : null,
            form = me.down('#frm-device-validation-results-load-profile-register'),
            dataViewValidateNowBtn = me.down('#btn-data-view-validate-now'),
            loadProfilesGrid = me.down('mdc-load-profile-list'),
            registerssGrid = me.down('mdc-register-list'),
            zoomLevelsStore = Ext.getStore('Uni.store.DataIntervalAndZoomLevels'),
            loadProfilesStore = Ext.getStore('Mdc.store.LoadProfilesOfDevice'),
            loadProfiles,
            registers;

        me.mainView.setLoading(false);

        if (success && record) {
            Ext.suspendLayouts();
            if (form) {
                form.loadRecord(record);
            }

            if (dataViewValidateNowBtn) {
                dataViewValidateNowBtn.setDisabled(!record.get('isActive'));
            }

            loadProfiles = record.get('detailedValidationLoadProfile');
            if (loadProfiles && loadProfiles.length && loadProfilesGrid) {
                record.detailedValidationLoadProfile().each(function (record) {
                    var id = record.getId(),
                        appropriateRecord = Ext.Array.findBy(me.loadProfiles, function (value) {
                            return value.id === id
                        }),
                        interval = loadProfilesStore.getById(id).get('interval');

                    record.beginEdit();
                    record.set('interval', interval);
                    record.set('intervalRecord', zoomLevelsStore.getIntervalRecord(interval));
                    record.set('intervalInMs', zoomLevelsStore.getIntervalInMs(record.get('intervalRecord').get('all')));
                    record.set('intervalStart', appropriateRecord.intervalStart);
                    record.set('intervalEnd', appropriateRecord.intervalEnd);
                    record.endEdit();
                });
                loadProfilesGrid.bindStore(record.detailedValidationLoadProfile());
                loadProfilesGrid.show();
            } else {
                loadProfilesGrid.hide();
            }

            registers = record.get('detailedValidationRegister');
            if (registers && registers.length && registerssGrid) {
                record.detailedValidationRegister().each(function (record) {
                    var id = record.getId(),
                        interval = {count: 1, timeUnit: 'years'};

                    record.beginEdit();
                    record.set('interval', interval);
                    record.set('intervalRecord', zoomLevelsStore.getIntervalRecord(interval));
                    record.set('intervalInMs', zoomLevelsStore.getIntervalInMs(record.get('intervalRecord').get('all')));
                    record.set('intervalStart', me.intervalRegisterStart);
                    record.set('intervalEnd', me.intervalRegisterEnd);
                    record.endEdit();
                });
                registerssGrid.bindStore(record.detailedValidationRegister());
                registerssGrid.show();
            } else {
                registerssGrid.hide();
            }
            Ext.resumeLayouts(true);
        }
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    }
});

