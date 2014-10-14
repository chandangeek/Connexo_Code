Ext.define('Mdc.controller.setup.DeviceLoadProfileChannels', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceloadprofilechannels.Setup'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice'
    ],

    stores: [
        'Mdc.store.ChannelsOfLoadProfilesOfDevice',
        'Mdc.store.TimeUnits'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLoadProfileChannelsSetup'
        },
        {
            ref: 'preview',
            selector: 'deviceLoadProfileChannelsSetup #deviceLoadProfileChannelsPreview'
        }
    ],

    init: function () {
        this.control({
            'deviceLoadProfileChannelsSetup #deviceLoadProfileChannelsGrid': {
                select: this.showPreview
            },
            '#deviceLoadProfileChannelsActionMenu': {
                click: this.chooseAction
            },
            '#channelActionMenu': {
                click: this.chooseAction
            }
        });
    },

    showOverview: function (mRID, loadProfileId) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            loadProfileOfDeviceModel = me.getModel('Mdc.model.LoadProfileOfDevice'),
            channelsOfLoadProfilesOfDeviceStore = me.getStore('Mdc.store.ChannelsOfLoadProfilesOfDevice'),
            timeUnitsStore = me.getStore('Mdc.store.TimeUnits'),
            widget,
            showPage = function () {
                channelsOfLoadProfilesOfDeviceStore.getProxy().setUrl({
                    mRID: mRID,
                    loadProfileId: loadProfileId
                });
                channelsOfLoadProfilesOfDeviceStore.load();
                widget = Ext.widget('deviceLoadProfileChannelsSetup', {
                    mRID: mRID,
                    loadProfileId: loadProfileId,
                    router: me.getController('Uni.controller.history.Router')
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                deviceModel.load(mRID, {
                    success: function (record) {
                        me.getApplication().fireEvent('loadDevice', record);
                    }
                });
                loadProfileOfDeviceModel.getProxy().setUrl(mRID);
                loadProfileOfDeviceModel.load(loadProfileId, {
                    success: function (record) {
                        if (!widget.isDestroyed) {
                            me.getApplication().fireEvent('loadProfileOfDeviceLoad', record);
                            widget.down('#deviceLoadProfilesSubMenuPanel').setParams(mRID, record);
                            widget.down('#deviceLoadProfileChannelsIntervalAndLastReading').loadRecord(record);
                        }
                    }
                });
            };
        me.mRID = mRID;
        me.loadProfileId = loadProfileId;
        timeUnitsStore.getCount() ? showPage() : timeUnitsStore.on('load', showPage, me, {single: true});
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPreview();
        if (!record.data.validationInfo.validationActive) {
            preview.down('#validateNowChannel').hide();
            Ext.ComponentQuery.query('#channelActionMenu #validateNowChannel')[0].hide();
        } else {
            preview.down('#validateNowChannel').show();
            Ext.ComponentQuery.query('#channelActionMenu #validateNowChannel')[0].show();
        }
        preview.setTitle(record.get('name'));
        preview.down('#deviceLoadProfileChannelsPreviewForm').loadRecord(record);
        preview.down('#deviceLoadProfileChannelsActionMenu').record = record;
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            route,
            filterParams = {};

        switch (item.action) {
            case 'viewData':
                route = 'devices/device/loadprofiles/loadprofile/channels/channel/data';
                break;
            case 'viewDetails':
                filterParams.onlySuspect = false;
                route = 'devices/device/loadprofiles/loadprofile/channels/channel';
                break;
            case 'validateNow':
                me.showValidateNowMessage(menu.record);
                break;
            case 'viewSuspects':
                filterParams.onlySuspect = true;
                route = 'devices/device/loadprofiles/loadprofile/channels/channel/data';
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward({
            channelId: menu.record.getId()
        }, filterParams);
    },

    showValidateNowMessage: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'validateNowChannelConfirmationWindow',
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
                        title: Uni.I18n.translatePlural('deviceloadprofiles.channels.validateNow', record.get('name'), 'MDC', 'Validate data of channel {0}?'),
                        msg: ''
                    });
                } else {
                    var title = Uni.I18n.translatePlural('deviceloadprofiles.channels.validateNow.error', record.get('name'), 'MDC', 'Failed to validate data of channel {0}'),
                        message = Uni.I18n.translate('deviceloadprofiles.channels.noData', 'MDC', 'There is currently no data for this channel'),
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
                    itemId: 'validateChannelFromDate',
                    editable: false,
                    showToday: false,
                    value: me.dataValidationLastChecked,
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.validateNow.item1', 'MDC', 'The data of load profile will be validated starting from'),
                    labelWidth: 375,
                    labelPad: 0.5
                },
                {
                    xtype: 'panel',
                    itemId: 'validateChannelDateErrors',
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
        if (confWindow.down('#validateChannelFromDate').getValue() > me.dataValidationLastChecked) {
            confWindow.down('#validateChannelDateErrors').update(Uni.I18n.translate('deviceloadprofiles.activation.error', 'MDC', 'The date should be before or equal to the default date.'));
            confWindow.down('#validateChannelDateErrors').setVisible(true);
        } else {
            Ext.Ajax.request({
                url: '../../api/ddr/devices/' + me.mRID + '/loadprofiles/' + me.loadProfileId + '/channels/' + record.get('id') + '/validate',
                method: 'PUT',
                jsonData: {
                    lastChecked: confWindow.down('#validateChannelFromDate').getValue().getTime()
                },
                success: function () {
                    confWindow.removeAll(true);
                    confWindow.destroy();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('deviceloadprofiles.channels.activation.completed', record.get('name'), 'MDC', 'Data validation on channel {0} was completed successfully'));
                    Ext.ComponentQuery.query('#deviceLoadProfileChannelsGrid')[0].fireEvent('select', Ext.ComponentQuery.query('#deviceLoadProfileChannelsGrid')[0].getSelectionModel(), record);
                }
            });
        }
    }
});