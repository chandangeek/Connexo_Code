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
        {ref: 'deviceRegisterConfigurationSetup', selector: '#deviceRegisterConfigurationSetup'}
    ],

    init: function () {
        var me = this;
        me.control({
            '#deviceRegisterConfigurationGrid': {
                select: me.onDeviceRegisterConfigurationGridSelect
            },
            '#gridPreviewActionMenu menu menuitem[action=viewdata]': {
                click: me.onGridPreviewActionMenuViewDataClick
            },
            '#gridPreviewActionMenu menu menuitem[action=validate]': {
                click: me.onGridPreviewActionMenuValidateClick
            },
            '#detailActionMenu menu menuitem[action=viewdata]': {
                click: me.onDetailActionMenuViewDataClick
            },
            '#detailActionMenu menu menuitem[action=validate]': {
                click: me.onDetailActionMenuValidateClick
            },
            '#deviceRegisterConfigurationGrid uni-actioncolumn': {
                viewdata: me.onGridPreviewActionMenuViewDataClick
            }
        });
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

        form.loadRecord(record);
        widget.setTitle(record.get('name'));

        previewContainer.removeAll();
        previewContainer.add(widget);
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
                        var form = widget.down('#deviceRegisterConfigurationDetailForm');
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getApplication().fireEvent('loadRegisterConfiguration', register);
                        form.loadRecord(register);
                        widget.down('#stepsMenu').setTitle(register.get('name'));
                    },
                    callback: function () {
                        contentPanel.setLoading(false);
                    }
                });
            }

        });
    },

    onGridPreviewActionMenuViewDataClick: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            grid = me.getDeviceRegisterConfigurationGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        router.getRoute('devices/device/registers/register/data').forward({registerId: lastSelected.getData().id});
    },

    onDetailActionMenuViewDataClick: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/registers/register/data').forward();
    },

    // TODO complete the functionality below
    onGridPreviewActionMenuValidateClick: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            grid = me.getDeviceRegisterConfigurationGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),

            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'validateNowRegisterConfirmationWindow',
                confirmText: Uni.I18n.translate('general.validate', 'MDC', 'Validate'),
                confirmation: function () {
                    //me.activateDataValidation(lastSelected, this);
                }
            });
        confirmationWindow.add(me.getValidationContent(lastSelected));
        confirmationWindow.show({
            title: Uni.I18n.translatePlural('deviceregisterconfiguration.validation.validateNow', me.mRID, 'MDC', 'Validate data of register {0}?'),
            msg: ''
        });
        confirmationWindow.on('close', function () {
            this.destroy();
        });

    },

    onDetailActionMenuValidateClick: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
    },

    getValidationContent: function (lastSelected) {
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
                    value: new Date(lastSelected.data.detailedValidationInfo.lastChecked),
                    fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.validation.item1', 'MDC', 'The data of this register will be validated starting from'),
                    labelWidth: 400,
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
                    padding: '0 0 -10 0',
                    fieldLabel: Uni.I18n.translate('deviceloadprofiles.validateNow.item2', 'MDC', 'Note: The date displayed by default is the last checked (the moment when the last interval was checked in the validation process).'),
                    labelWidth: 500
                }
            ]
        });
    },

    validateRegisterData: function(lastSelected) {
        var me = this;

        Ext.Ajax.request({
                url: '../../api/ddr/devices/' + me.mRID + '/registers/' + lastSelected.data.registerId + 'validate',
                method: 'PUT',
                timeout: 600000,
                success: function () {
                    me.destroyConfirmationWindow();
                    me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translatePlural('device.dataValidation.activation.validated', me.mRID, 'MDC', 'Data validation on device {0} was completed successfully'));
                },
                failure: function (response) {
                    if (confWindow) {
                        var res = Ext.JSON.decode(response.responseText);
                        confWindow.down('#validationProgress').removeAll(true);
                        me.showValidationActivationErrors(res.errors[0].msg);
                        me.confirmationWindowButtonsDisable(false);
                    }
                },
                callback: function () {
                    Ext.Ajax.resumeEvent('requestexception');
                }
            });
        }

});

