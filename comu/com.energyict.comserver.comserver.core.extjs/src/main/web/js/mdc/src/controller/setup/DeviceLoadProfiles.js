Ext.define('Mdc.controller.setup.DeviceLoadProfiles', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofiles.Setup'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice'
    ],

    stores: [
        'Mdc.store.LoadProfilesOfDevice',
        'Mdc.store.TimeUnits'
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
            timeUnitsStore = me.getStore('Mdc.store.TimeUnits'),
            widget,
            showPage = function () {
                me.getStore('Mdc.store.LoadProfilesOfDevice').getProxy().setUrl(mRID);
                widget = Ext.widget('deviceLoadProfilesSetup', {
                    mRID: mRID,
                    router: me.getController('Uni.controller.history.Router')
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                model.load(mRID, {
                    success: function (record) {
                        if (!widget.isDestroyed) {
                            me.getApplication().fireEvent('loadDevice', record);
                            widget.down('#stepsMenu').setTitle(record.get('mRID'));
                        }
                    }
                });
            };
        me.mRID = mRID;
        timeUnitsStore.getCount() ? showPage() : timeUnitsStore.on('load', showPage, me, {single: true});
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
            case 'viewChannels':
                route = 'devices/device/loadprofiles/loadprofile/channels';
                break;
            case 'viewData':
                filterParams.viewOnlySuspects = false;
                route = 'devices/device/loadprofiles/loadprofile/data';
                break;
            case 'viewDetails':
                route = 'devices/device/loadprofiles/loadprofile';
                break;
            case 'validateNow':
                me.showValidateNowMessage(menu.record);
                break;
            case 'viewSuspects':
                filterParams.viewOnlySuspects = true;
                route = 'devices/device/loadprofiles/loadprofile/data';
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
            });
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + me.mRID + '/validationrulesets/validationstatus',
            method: 'GET',
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                if (res.hasValidation) {
                    if (res.lastChecked) {
                        me.dataValidationLastChecked = new Date(res.lastChecked);
                    } else {
                        me.dataValidationLastChecked = new Date();
                    }
                    confirmationWindow.add(me.getValidationContent());
                    confirmationWindow.show({
                        title: Uni.I18n.translatePlural('deviceloadprofiles.validateNow', record.get('name'), 'MDC', 'Validate data of load profile {0}?'),
                        msg: ''
                    });
                } else {
                    var title = Uni.I18n.translatePlural('deviceloadprofiles.validateNow.error', record.get('name'), 'MDC', 'Failed to validate data of load profile {0}'),
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
        return Ext.create('Ext.container.Container', {
            defaults: {
                labelAlign: 'left',
                labelStyle: 'font-weight: normal; padding-left: 50px'
            },
            items: [
                {
                    xtype: 'datefield',
                    itemId: 'validateLoadProfileFromDate',
                    editable: false,
                    showToday: false,
                    value: me.dataValidationLastChecked,
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.validateNow.item1', 'MDC', 'The data of load profile will be validated starting from'),
                    labelWidth: 375,
                    labelPad: 0.5
                },
                {
                    xtype: 'panel',
                    itemId: 'validateLoadProfileDateErrors',
                    hidden: true,
                    bodyStyle: {
                        color: '#eb5642',
                        padding: '0 0 15px 65px'
                    },
                    html: ''
                },
                {
                    xtype: 'displayfield',
                    value: '',
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.validateNow.item2', 'MDC', 'Note: The date displayed by default is the last checked (the moment when the last interval was checked in the validation process).'),
                    labelWidth: 500
                }
            ]
        });
    },

    activateDataValidation: function (record, confWindow) {
        var me = this;
        if (confWindow.down('#validateLoadProfileFromDate').getValue() > me.dataValidationLastChecked) {
            confWindow.down('#validateLoadProfileDateErrors').update(Uni.I18n.translate('deviceloadprofiles.activation.error', 'MDC', 'The date should be before or equal to the default date.'));
            confWindow.down('#validateLoadProfileDateErrors').setVisible(true);
        } else {
            Ext.Ajax.request({
                url: '../../api/ddr/devices/' + me.mRID + '/loadprofiles/' + me.loadProfileId + '/validate',
                method: 'PUT',
                jsonData: {
                    lastChecked: confWindow.down('#validateLoadProfileFromDate').getValue().getTime()
                },
                success: function () {
                    confWindow.removeAll(true);
                    confWindow.destroy();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('deviceloadprofiles.activation.completed', me.loadProfileName, 'MDC', 'Data validation on load profile {0} was completed successfully'));
                    Ext.ComponentQuery.query('#deviceLoadProfilesGrid')[0].fireEvent('select', Ext.ComponentQuery.query('#deviceLoadProfilesGrid')[0].getSelectionModel(), record);
                }
            });
        }
    }
});