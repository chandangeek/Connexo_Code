Ext.define('Mdc.controller.setup.DeviceLoadProfiles', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofiles.Setup'
    ],

    requires: [
        'Mdc.store.TimeUnits',
        'Mdc.view.setup.deviceloadprofiles.DataValidationContent'
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
            }
        });
    },

    showView: function (mRID) {
        var me = this,
            model = me.getModel('Mdc.model.Device'),
            timeUnitsStore = me.getStore('TimeUnits'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            widget;

        viewport.setLoading();
        showPage = function () {
            me.getStore('Mdc.store.LoadProfilesOfDevice').getProxy().setUrl(mRID);
            model.load(mRID, {
                success: function (record) {
                    widget = Ext.widget('deviceLoadProfilesSetup', {
                        device: record,
                        router: me.getController('Uni.controller.history.Router')
                    });
                    me.getApplication().fireEvent('changecontentevent', widget);
                    me.getApplication().fireEvent('loadDevice', record);
                    viewport.setLoading(false);
                }
            });
        };
        me.mRID = mRID;
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
        loadProfileOfDeviceModel.getProxy().setUrl(me.mRID);
        preview.setLoading();
        loadProfileOfDeviceModel.load(loadProfileId, {
            success: function (rec) {
                if (!preview.isDestroyed) {
                    if (!rec.data.validationInfo.validationActive) {
                        preview.down('#validateNowLoadProfile').hide();
                        Ext.ComponentQuery.query('#loadProfileActionMenu #validateNowLoadProfile')[0].hide();
                    } else {
                        preview.down('#validateNowLoadProfile').show();
                        Ext.ComponentQuery.query('#loadProfileActionMenu #validateNowLoadProfile')[0].show();
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
            mRID = me.mRID ? me.mRID : router.arguments.mRID;

        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + encodeURIComponent(mRID) + '/validationrulesets/validationstatus',
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
            mRID = me.mRID ? me.mRID : router.arguments.mRID,
            loadProfileId = me.loadProfileId ? me.loadProfileId : router.arguments.loadProfileId;

        if (confWindow.down('#validateLoadProfileFromDate').getValue() > me.dataValidationLastChecked) {
            confWindow.down('#validateLoadProfileDateErrors').update(Uni.I18n.translate('deviceloadprofiles.activation.error', 'MDC', 'The date should be before or equal to the default date.'));
            confWindow.down('#validateLoadProfileDateErrors').setVisible(true);
        } else {
            confWindow.down('button').setDisabled(true);
            Ext.Ajax.request({
                url: '../../api/ddr/devices/' + encodeURIComponent(mRID) + '/loadprofiles/' + loadProfileId + '/validate',
                method: 'PUT',
                jsonData: {
                    lastChecked: confWindow.down('#validateLoadProfileFromDate').getValue().getTime()
                },
                success: function () {
                    confWindow.removeAll(true);
                    confWindow.destroy();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('deviceloadprofiles.activation.completed', 'MDC', 'Data validation completed'));
                    Ext.ComponentQuery.query('#deviceLoadProfilesGrid')[0].fireEvent('select', Ext.ComponentQuery.query('#deviceLoadProfilesGrid')[0].getSelectionModel(), record);
                }
            });
        }
    }
});