Ext.define('Mdc.controller.setup.DeviceRegisterConfiguration', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.deviceregisterconfiguration.numerical.Preview',
        'setup.deviceregisterconfiguration.numerical.Detail',
        'setup.deviceregisterconfiguration.text.Preview',
        'setup.deviceregisterconfiguration.text.Detail',
        'setup.deviceregisterconfiguration.flags.Preview',
        'setup.deviceregisterconfiguration.flags.Detail',
        'setup.deviceregisterconfiguration.billing.Preview',
        'setup.deviceregisterconfiguration.billing.Detail',
        'setup.deviceregisterconfiguration.Setup',
        'setup.deviceregisterconfiguration.Grid',
        'setup.deviceregisterconfiguration.ValidationPreview'
    ],

    stores: [
        'RegisterConfigsOfDevice'
    ],

    refs: [
        {ref: 'deviceRegisterConfigurationGrid', selector: '#deviceRegisterConfigurationGrid'},
        {ref: 'deviceRegisterConfigurationSetup', selector: '#deviceRegisterConfigurationSetup'},
        {ref: 'deviceRegisterConfigurationPreview', selector: '#deviceRegisterConfigurationPreview'}
    ],

    init: function () {
        var me = this;
        me.control({
            '#deviceRegisterConfigurationGrid': {
                select: me.onDeviceRegisterConfigurationGridSelect
            },
            '#deviceRegisterConfigurationActionMenu': {
                click: this.chooseAction
            },
            '#registerActionMenu': {
                click: this.chooseAction
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
            route;
        routeParams.registerId = menu.record.getId();

        switch (item.action) {
            case 'viewdata':
                route = 'devices/device/registers/register/data';
                break;
            case 'validate':
                me.showValidateNowMessage(menu.record);
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(routeParams);
    },

    showDeviceRegisterConfigurationsView: function (mRID) {
        var me = this;
        me.mRID = mRID;
        var widget = Ext.widget('deviceRegisterConfigurationSetup', {mRID: mRID, router: me.getController('Uni.controller.history.Router')});
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getDeviceRegisterConfigurationGrid().getSelectionModel().select(0);
                widget.down('#stepsMenu').setTitle(device.get('mRID'));
            }
        });
    },

    onDeviceRegisterConfigurationGridSelect: function (rowmodel, record, index) {
        var me = this;
        me.previewRegisterConfiguration(record);
    },

    previewRegisterConfiguration: function (record) {
        var me = this,
            type = record.get('type'),
            widget = Ext.widget('deviceRegisterConfigurationPreview-' + type, {router: me.getController('Uni.controller.history.Router')}),
            form = widget.down('#deviceRegisterConfigurationPreviewForm'),
            previewContainer = me.getDeviceRegisterConfigurationSetup().down('#previewComponentContainer');
        me.registerId = record.get('id');
        me.registerName = record.get('name');
        form.loadRecord(record);
        widget.setTitle(record.get('name'));
        previewContainer.removeAll();
        previewContainer.add(widget);
        if (!record.data.detailedValidationInfo.validationActive) {
            Ext.ComponentQuery.query('#registerActionMenu #validateNowRegister')[0].hide();
            widget.down('#validateNowRegister').hide();
        }
        widget.down('#deviceRegisterConfigurationActionMenu').record = record;
    },

    showDeviceRegisterConfigurationDetailsView: function (mRID, registerId) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
        contentPanel.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setExtraParam('mRID', mRID);
                model.load(registerId, {
                    success: function (register) {
                        var type = register.get('type');
                        var widget = Ext.widget('deviceRegisterConfigurationDetail-' + type, {mRID: mRID, registerId: registerId, router: me.getController('Uni.controller.history.Router')});
                        me.getApplication().fireEvent('changecontentevent', widget);
                        var form = widget.down('#deviceRegisterConfigurationDetailForm');
                        me.getApplication().fireEvent('loadRegisterConfiguration', register);
                        form.loadRecord(register);
                        widget.down('#stepsMenu').setTitle(register.get('name'));
                        if (!register.data.detailedValidationInfo.validationActive) {
                            widget.down('#validateNowRegister').hide();
                        }
                        widget.down('#deviceRegisterConfigurationActionMenu').record = register;
                    },
                    callback: function () {
                        contentPanel.setLoading(false);
                    }
                });
            }
        });
    },

    showValidateNowMessage: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'validateNowRegisterConfirmationWindow',
                confirmText: Uni.I18n.translate('general.validate', 'MDC', 'Validate'),
                confirmation: function () {
                    me.activateDataValidation(record, this);
                }
            });
        Ext.Ajax.request({
            url: '../../api/ddr/devices/' + me.mRID + '/registers/' + me.registerId + '/validationstatus',
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
                        title: Uni.I18n.translatePlural('deviceregisterconfiguration.validation.validateNow', record.get('name'), 'MDC', 'Validate data of register {0}?'),
                        msg: ''
                    });
                } else {
                    var title = Uni.I18n.translatePlural('deviceregisterconfiguration.validateNow.error', record.get('name'), 'MDC', 'Failed to validate data of register {0}'),
                        message = Uni.I18n.translate('deviceregisterconfiguration.validation.noData', 'MDC', 'There is currently no data for this register'),
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

    activateDataValidation: function (record, confWindow) {
        var me = this;
        if (confWindow.down('#validateRegisterFromDate').getValue() > me.dataValidationLastChecked) {
            confWindow.down('#validateRegisterDateErrors').update(Uni.I18n.translate('deviceloadprofiles.activation.error', 'MDC', 'The date should be before or equal to the default date.'));
            confWindow.down('#validateRegisterDateErrors').setVisible(true);
        } else {
            Ext.Ajax.request({
                url: '../../api/ddr/devices/' + me.mRID + '/registers/' + me.registerId + '/validate',
                method: 'PUT',
                jsonData: {
                    lastChecked: confWindow.down('#validateRegisterFromDate').getValue().getTime()
                },
                success: function () {
                    confWindow.removeAll(true);
                    confWindow.destroy();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('deviceregisterconfiguration.validation.completed', me.registerName, 'MDC', 'Data validation on register {0} was completed successfully'));
                    if (Ext.ComponentQuery.query('#deviceRegisterConfigurationGrid')[0]) {
                        Ext.ComponentQuery.query('#deviceRegisterConfigurationGrid')[0].fireEvent('select', Ext.ComponentQuery.query('#deviceRegisterConfigurationGrid')[0].getSelectionModel(), record);
                    }
                }
                /*failure: function (response) {
                 if (confWindow) {
                 var res = Ext.JSON.decode(response.responseText);
                 me.showValidationActivationErrors(res.errors[0].msg);
                 }
                 },
                 callback: function () {
                 Ext.Ajax.resumeEvent('requestexception');
                 }*/
            });
        }
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
                    itemId: 'validateRegisterFromDate',
                    editable: false,
                    showToday: false,
                    value: me.dataValidationLastChecked,
                    fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.validation.item1', 'MDC', 'The data of register will be validated starting from'),
                    labelWidth: 375,
                    labelPad: 0.5
                },
                {
                    xtype: 'panel',
                    itemId: 'validateRegisterDateErrors',
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
    }

   /* showValidationActivationErrors: function (errors) {
        if (Ext.ComponentQuery.query('#validateNowRegisterConfirmationWindow')[0]) {
            Ext.ComponentQuery.query('#validateNowRegisterConfirmationWindow')[0].down('#validateRegisterDateErrors').update(errors);
            Ext.ComponentQuery.query('#validateNowRegisterConfirmationWindow')[0].down('#validateRegisterDateErrors').setVisible(true);
        }
    }*/
});

