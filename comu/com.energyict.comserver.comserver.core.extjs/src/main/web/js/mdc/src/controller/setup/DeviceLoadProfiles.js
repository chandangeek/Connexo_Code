/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceLoadProfiles', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofiles.Setup',
        'Mdc.view.setup.deviceloadprofiles.EditWindow',
        'Uni.view.error.NotFound'
    ],

    requires: [
        'Mdc.store.TimeUnits',
        'Mdc.view.setup.deviceloadprofiles.DataValidationContent',
        'Uni.controller.history.Router'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice'
    ],

    stores: [
        'Mdc.store.LoadProfilesOfDevice',
        'TimeUnits'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLoadProfilesSetup'
        },
        {
            ref: 'preview',
            selector: 'deviceLoadProfilesSetup #deviceLoadProfilesPreview'
        },
        {
            ref: 'deviceLoadProfileEditWindow',
            selector: 'deviceloadprofile-edit-window'
        },
        {
            ref: 'deviceLoadProfileOverview',
            selector: 'deviceLoadProfilesOverview'
        }
    ],

    init: function () {
        this.control({
            'deviceLoadProfilesSetup #deviceLoadProfilesGrid': {
                select: this.showPreview
            },
            '#deviceLoadProfilesActionMenu': {
                click: this.chooseAction
            },
            '#loadProfileActionMenu': {
                click: this.chooseAction
            },
            '#mdc-deviceloadprofile-edit-window-save': {
                click: this.saveLoadProfile
            }
        });
    },

    showView: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            model = me.getModel('Mdc.model.Device'),
            timeUnitsStore = me.getStore('TimeUnits'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            widget;

        viewport.setLoading();
        showPage = function () {
            me.getStore('Mdc.store.LoadProfilesOfDevice').getProxy().setExtraParam('deviceId', deviceId);
            model.load(deviceId, {
                success: function (record) {
                    if (record.get('hasLoadProfiles')) {
                        widget = Ext.widget('deviceLoadProfilesSetup', {
                            device: record,
                            router: router
                        });
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getApplication().fireEvent('loadDevice', record);
                    } else {
                        window.location.replace(router.getRoute('notfound').buildUrl());
                    }
                    viewport.setLoading(false);
                }
            });
        };
        me.deviceId = deviceId;
        timeUnitsStore.load({
            callback: function () {
                showPage();
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            loadProfileOfDeviceModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            loadProfileId = record.get('id'),
            preview = me.getPreview();
        me.loadProfileId = loadProfileId;
        me.loadProfileName = record.get('name');
        preview.setTitle(record.get('name'));
        loadProfileOfDeviceModel.getProxy().setExtraParam('deviceId', me.deviceId);
        preview.setLoading();
        loadProfileOfDeviceModel.load(loadProfileId, {
            success: function (rec) {
                if (!preview.isDestroyed) {
                    if (!rec.data.validationInfo.validationActive) {
                        if (preview.down('#validateNowLoadProfile')) {
                            preview.down('#validateNowLoadProfile').hide();
                        }
                        if (Ext.ComponentQuery.query('#loadProfileActionMenu #validateNowLoadProfile')[0]) {
                            Ext.ComponentQuery.query('#loadProfileActionMenu #validateNowLoadProfile')[0].hide();
                        }
                    } else {
                        if (preview.down('#validateNowLoadProfile')) {
                            preview.down('#validateNowLoadProfile').show();
                        }
                        if (Ext.ComponentQuery.query('#loadProfileActionMenu #validateNowLoadProfile')[0]) {
                            Ext.ComponentQuery.query('#loadProfileActionMenu #validateNowLoadProfile')[0].show();
                        }
                    }
                    preview.down('#deviceLoadProfilesPreviewForm').loadRecord(rec);
                    preview.setLoading(false);
                }
            }
        });
        preview.down('#deviceLoadProfilesActionMenu').record = record;
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
            route,
            filterParams = {};

        routeParams.loadProfileId = menu.record.getId();

        switch (item.action) {
            case 'validateNow':
                me.showValidateNowMessage(menu.record);
                break;
            case 'viewSuspects':
                filterParams.suspect = 'suspect';
                route = 'devices/device/loadprofiles/loadprofiletableData';
                break;
            case 'editLoadProfile':
                Ext.widget('deviceloadprofile-edit-window', {
                    loadProfileRecord: menu.record
                }).show();
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(routeParams, filterParams);
    },

    showValidateNowMessage: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'validateNowLoadProfileConfirmationWindow',
                confirmText: Uni.I18n.translate('general.validate', 'MDC', 'Validate'),
                confirmation: function () {
                    me.activateDataValidation(record, this);
                }
            }),
            router = this.getController('Uni.controller.history.Router'),
            deviceId = me.deviceId || router.arguments.deviceId;

        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(deviceId) + '/validationrulesets/validationstatus',
            method: 'GET',
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                if (res.hasValidation) {
                    if (res.lastChecked) {
                        me.dataValidationLastChecked = new Date(res.lastChecked);
                    } else {
                        me.dataValidationLastChecked = new Date();
                    }
                    confirmationWindow.insert(1,me.getValidationContent());
                    confirmationWindow.show({
                        title: Uni.I18n.translate('deviceloadprofiles.validateNow', 'MDC', 'Validate data of load profile {0}?', [record.get('name')]),
                        msg: ''
                    });
                } else {
                    var title = Uni.I18n.translate('deviceloadprofiles.validateNow.error', 'MDC', 'Failed to validate data of load profile {0}', [record.get('name')]),
                        message = Uni.I18n.translate('deviceloadprofiles.noData', 'MDC', 'There is currently no data for this load profile'),
                        config = {
                            icon: Ext.MessageBox.WARNING
                        };
                    me.getApplication().getController('Uni.controller.Error').showError(title, message, config);
                }
            }
        });
        confirmationWindow.on('close', function () {
            this.destroy();
        });
    },

    getValidationContent: function () {
        var me = this;
        return Ext.create('Mdc.view.setup.deviceloadprofiles.DataValidationContent', {
            dataValidationLastChecked: me.dataValidationLastChecked
        });
    },

    activateDataValidation: function (record, confWindow) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceId = me.deviceId || router.arguments.deviceId,
            loadProfileId = me.loadProfileId ? me.loadProfileId : router.arguments.loadProfileId;

        if (confWindow.down('#validateLoadProfileFromDate').getValue() > me.dataValidationLastChecked) {
            confWindow.down('#validateLoadProfileDateErrors').update(Uni.I18n.translate('deviceloadprofiles.activation.error', 'MDC', 'The date should be before or equal to the default date.'));
            confWindow.down('#validateLoadProfileDateErrors').setVisible(true);
        } else {
            confWindow.down('button').setDisabled(true);
            Ext.Ajax.request({
                url: '../../api/ddr/devices/' + encodeURIComponent(deviceId) + '/loadprofiles/' + loadProfileId + '/validate',
                method: 'PUT',
                isNotEdit: true,
                jsonData: Ext.merge(_.pick(record.getRecordData(), 'id', 'name', 'version', 'parent'), {
                    lastChecked: confWindow.down('#validateLoadProfileFromDate').getValue().getTime()
                }),
                success: function () {
                    confWindow.removeAll(true);
                    confWindow.destroy();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('deviceloadprofiles.activation.completed', 'MDC', 'Data validation completed'));
                    router.getRoute().forward();
                },
                failure: function () {
                    confWindow.destroy();
                }
            });
        }
    },

    saveLoadProfile: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            editWindow = me.getDeviceLoadProfileEditWindow(),
            datePicker = editWindow.down('#mdc-deviceloadprofile-edit-window-date-picker'),
            loadProfileRecordInEditWindow = editWindow.loadProfileRecord,
            loadProfileModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            deviceId = this.getController('Uni.controller.history.Router').arguments.deviceId,
            loadProfileId = loadProfileRecordInEditWindow.get('id'),
            onLoadProfileLoaded = function(loadProfileRecord) {
                loadProfileRecordInEditWindow.set('lastReading', datePicker.getValue());
                loadProfileRecord.beginEdit();
                loadProfileRecord.set('lastReading', datePicker.getValue());
                //if (!loadProfileRecord.get('lastChecked')) {
                //    loadProfileRecord.set('lastChecked', null);
                //}
                //if (!logbookRecord.get('lastEventDate')) {
                //    logbookRecord.set('lastEventDate', null);
                //}
                loadProfileRecord.endEdit();
                loadProfileRecord.save({
                    success: onLoadProfileSaved
                });
                editWindow.close();
            },
            onLoadProfileSaved = function() {
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translate('deviceloadpofiles.acknowledge.updateSuccess', 'MDC', 'Load profile saved')
                );
                if (router.getRoute().route === 'loadprofiles') {
                    me.showPreview(null, loadProfileRecordInEditWindow);
                } else if (router.getRoute().route === '{loadProfileId}') {
                    me.getDeviceLoadProfileOverview().down('#deviceLoadProfilesPreviewForm').loadRecord(loadProfileRecordInEditWindow);
                }
            };

        loadProfileModel.getProxy().setExtraParam('deviceId', deviceId);
        loadProfileModel.load(loadProfileId, {
            success: onLoadProfileLoaded
        });
    }
});