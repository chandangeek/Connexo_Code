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
        'setup.deviceregisterconfiguration.ValidationPreview',
        'setup.deviceregisterconfiguration.TabbedDeviceRegisterView'
    ],

    models: [
        'Mdc.model.RegisterValidationPreview'
        ],
    stores: [
        'RegisterConfigsOfDevice'
    ],

    refs: [
        {ref: 'deviceRegisterConfigurationGrid', selector: '#deviceRegisterConfigurationGrid'},
        {ref: 'deviceRegisterConfigurationSetup', selector: '#deviceRegisterConfigurationSetup'},
        {ref: 'deviceRegisterConfigurationPreview', selector: '#deviceRegisterConfigurationPreview'},
        {ref: 'deviceRegisterConfigurationPreviewForm', selector: '#deviceRegisterConfigurationPreviewForm'},
        {ref: 'deviceRegisterConfigurationDetailForm', selector: '#deviceRegisterConfigurationDetailForm'},
        {ref: 'stepsMenu', selector: '#stepsMenu'},
        {ref: 'deviceRegistersOverview', selector: 'tabbedDeviceRegisterView'}
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
            route,
            filterParams = {};
        routeParams.registerId = menu.record.getId();

        switch (item.action) {
            case 'validate':
                me.showValidateNowMessage(menu.record);
                break;
            case 'viewSuspects':
                filterParams.suspect = 'suspect';
                route = 'devices/device/registers/registerdata';
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(routeParams, filterParams);
    },

    showDeviceRegisterConfigurationsView: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];
        me.mRID = mRID;
        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                var widget = Ext.widget('deviceRegisterConfigurationSetup', {device: device, router: me.getController('Uni.controller.history.Router')});
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                viewport.setLoading(false);
            }
        });
    },

    updateDeviceRegisterConfigurationsDetails: function (mRID, registerId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];
        me.mRID = mRID;
        me.registerId = registerId;

        viewport.setLoading();
        var model = me.getModel('Mdc.model.RegisterValidationPreview');
        model.getProxy().setUrl(mRID, registerId);
        model.load('', {
            success: function (record) {
                me.updateValidationData(record);
            }
        });
        viewport.setLoading(false);
    },

    updateValidationData: function(record)
    {
        var me = this,
            form = me.getDeviceRegisterConfigurationPreviewForm(),
            formRecord;
        if(form)
            formRecord = form.getRecord();
        else{
            form = me.getDeviceRegisterConfigurationDetailForm();
            formRecord = form.getRecord();
        }
        formRecord.set('validationInfo_dataValidated',
            record.get('dataValidated')? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                : Uni.I18n.translate('general.no', 'MDC', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>');
        formRecord.set('lastChecked_formatted',
            Uni.DateTime.formatDateTimeLong(new Date(record.get('lastChecked'))));

        form.loadRecord(formRecord);
    },

    onDeviceRegisterConfigurationGridSelect: function (rowmodel, record) {
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
        Ext.suspendLayouts();
        form.loadRecord(record);
        widget.setTitle(record.get('readingType').fullAliasName);
        previewContainer.removeAll();
        previewContainer.add(widget);
        widget.on('render', function () {
            widget.down('#deviceRegisterConfigurationActionMenu').record = record;
        }, me, {single: true});
        Ext.resumeLayouts(true);
    },

    showDeviceRegisterConfigurationDetailsView: function (mRID, registerId, tabController) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            registersOfDeviceStore = me.getStore('RegisterConfigsOfDevice');
        contentPanel.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setExtraParam('mRID', encodeURIComponent(mRID));
                model.load(registerId, {
                    success: function (register) {
                        var type = register.get('type');
                        var widget = Ext.widget('tabbedDeviceRegisterView', {device: device, router: me.getController('Uni.controller.history.Router')});
                        var func = function () {
                            me.getApplication().fireEvent('changecontentevent', widget);
                            widget.down('#registerTabPanel').setTitle(register.get('readingType').fullAliasName);
                            var config = Ext.widget('deviceRegisterConfigurationDetail-' + type, {mRID: encodeURIComponent(mRID), registerId: registerId, router: me.getController('Uni.controller.history.Router')});
                            var form = config.down('#deviceRegisterConfigurationDetailForm');
                            me.getApplication().fireEvent('loadRegisterConfiguration', register);
                            form.loadRecord(register);
                            if (!register.data.detailedValidationInfo.validationActive) {
                                config.down('#validateNowRegister').hide();
                            }
                            config.down('#deviceRegisterConfigurationActionMenu').record = register;
                            widget.down('#register-specifications').add(config);
                        };
                        if (registersOfDeviceStore.getTotalCount() === 0) {
                            registersOfDeviceStore.getProxy().url = registersOfDeviceStore.getProxy().url.replace('{mRID}', encodeURIComponent(mRID));
                            registersOfDeviceStore.load(function () {
                                func();
                            });
                        } else {
                            func();
                        }
                    },
                    callback: function () {
                        contentPanel.setLoading(false);
                        tabController.showTab(0);
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
            url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/registers/' + me.registerId + '/validationstatus',
            method: 'GET',
            success: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                if (res.hasValidation) {
                    if (res.lastChecked) {
                        me.dataValidationLastChecked = new Date(res.lastChecked);
                    } else {
                        me.dataValidationLastChecked = new Date();
                    }
                    confirmationWindow.insert(1, me.getValidationContent());
                    confirmationWindow.show({
                        title: Uni.I18n.translate('registerconfiguration.validation.validateNow', 'MDC', 'Validate data of register configuration {0}?', [record.get('name')]),
                        msg: ''
                    });
                } else {
                    var title = Uni.I18n.translate('registerconfiguration.validateNow.error', 'MDC', 'Failed to validate data of register configuration {0}', [record.get('name')]),
                        message = Uni.I18n.translate('registerconfiguration.validation.noData', 'MDC', 'There is currently no data for this register configuration'),
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
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        if (confWindow.down('#validateRegisterFromDate').getValue() > me.dataValidationLastChecked) {
            confWindow.down('#validateRegisterDateErrors').update(Uni.I18n.translate('deviceloadprofiles.activation.error', 'MDC', 'The date should be before or equal to the default date.'));
            confWindow.down('#validateRegisterDateErrors').setVisible(true);
        } else {
            confWindow.down('button').setDisabled(true);
            Ext.Ajax.request({
                url: '../../api/ddr/devices/' + encodeURIComponent(me.mRID) + '/registers/' + me.registerId + '/validate',
                method: 'PUT',
                isNotEdit: true,
                jsonData: Ext.merge({
                    lastChecked: confWindow.down('#validateRegisterFromDate').getValue().getTime()
                }, _.pick(record.getRecordData(), 'mRID', 'version', 'parent')),
                success: function () {
                    confWindow.removeAll(true);
                    confWindow.destroy();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('registerconfiguration.validation.completed', 'MDC', 'Data validation completed'));
                    router.getRoute().forward();
                },
                failure: function () {
                    confWindow.removeAll(true);
                    confWindow.destroy();
                }
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
                    fieldLabel: Uni.I18n.translate('registerconfiguration.validation.item1', 'MDC', 'The data of register configuration will be validated starting from'),
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

    // showValidationActivationErrors: function (errors) {
    //     if (Ext.ComponentQuery.query('#validateNowRegisterConfirmationWindow')[0]) {
    //         Ext.ComponentQuery.query('#validateNowRegisterConfirmationWindow')[0].down('#validateRegisterDateErrors').update(errors);
    //         Ext.ComponentQuery.query('#validateNowRegisterConfirmationWindow')[0].down('#validateRegisterDateErrors').setVisible(true);
    //     }
    //}
});

