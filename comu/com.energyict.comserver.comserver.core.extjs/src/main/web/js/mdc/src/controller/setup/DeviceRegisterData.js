Ext.define('Mdc.controller.setup.DeviceRegisterData', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.deviceregisterdata.MainSetup',
        'setup.deviceregisterdata.MainGrid',
        'setup.deviceregisterdata.text.Setup',
        'setup.deviceregisterdata.text.Grid',
        'setup.deviceregisterdata.text.Preview',
        'setup.deviceregisterdata.numerical.Setup',
        'setup.deviceregisterdata.numerical.Grid',
        'setup.deviceregisterdata.numerical.Preview',
        'setup.deviceregisterdata.billing.Setup',
        'setup.deviceregisterdata.billing.Grid',
        'setup.deviceregisterdata.billing.Preview',
        'setup.deviceregisterdata.flags.Setup',
        'setup.deviceregisterdata.flags.Grid',
        'setup.deviceregisterdata.flags.Preview',
        'setup.deviceregisterdata.ValidationPreview',
        'setup.deviceregisterdata.RegisterTopFilter'
    ],

    models: [
    ],

    stores: [
        'RegisterData',
        'NumericalRegisterData',
        'BillingRegisterData',
        'TextRegisterData',
        'FlagsRegisterData',
        'RegisterConfigsOfDevice',
        'Mdc.store.RegisterDataDurations'
    ],

    refs: [
        { ref: 'page', selector: 'deviceRegisterDataPage' },
        { ref: 'deviceregisterreportpreview', selector: '#deviceregisterreportpreview' },
        {
            ref: 'filterPanel',
            selector: 'deviceRegisterDataPage mdc-registers-topfilter'
        },
        {
            ref: 'stepsMenu',
            selector: '#stepsMenu'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#deviceregisterreportsetup #deviceregisterreportgrid': {
                select: me.loadGridItemDetail
            },
            'deviceregisterdataactionmenu': {
                beforeshow: this.checkSuspect,
                click: this.chooseAction
            }
        });
    },

    loadGridItemDetail: function (rowmodel, record) {
        var me = this,
            previewPanel = me.getDeviceregisterreportpreview(),
            form = previewPanel.down('form');
        previewPanel.setTitle(Ext.util.Format.date(new Date(record.get('timeStamp')), 'M j, Y \\a\\t G:i'));
        if (previewPanel.down('displayfield[name=deltaValue]')) {
            previewPanel.down('displayfield[name=deltaValue]').setVisible(!Ext.isEmpty(record.get('deltaValue')));
        }
        form.loadRecord(record);
    },

    showDeviceRegisterDataView: function (mRID, registerId, tabController) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            registerModel = me.getModel('Mdc.model.Register'),
            router = me.getController('Uni.controller.history.Router'),
            dependenciesCount = 2,
            onDependenciesLoad = function () {
                var widget,
                    type,
                    dataStore;

                dependenciesCount--;
                if (!dependenciesCount) {
                    widget = Ext.widget('tabbedDeviceRegisterView', {
                        device: device,
                        router: router
                    });
                    type = register.get('type');
                    dataStore = me.getStore(type.charAt(0).toUpperCase() + type.substring(1) + 'RegisterData');

                    me.getApplication().fireEvent('changecontentevent', widget);

                    Ext.suspendLayouts();
                    widget.down('#registerTabPanel').setTitle(register.get('readingType').fullAliasName);

                    tabController.showTab(1);

                    widget.down('#register-data').add(Ext.widget('deviceregisterreportsetup-' + type, {
                        mRID: encodeURIComponent(mRID),
                        registerId: registerId
                    }));

                    widget.down('grid').down('[dataIndex=value]').setText(Uni.I18n.translate('device.registerData.value', 'MDC', 'Value') + ' (' + register.get('lastReading')['unitOfMeasure'] + ')');

                    me.getFilterPanel().bindStore(dataStore);

                    if (type === 'billing' || type === 'numerical') {
                        var deltaValueColumn = widget.down('grid').down('[dataIndex=deltaValue]');
                        deltaValueColumn.setText(Uni.I18n.translate('device.registerData.deltaValue', 'MDC', 'Delta value') + ' (' + register.get('lastReading')['unitOfMeasure'] + ')');
                        deltaValueColumn.setVisible(register.get('isCumulative'));
                    }
                    Ext.resumeLayouts(true);

                    contentPanel.setLoading(false);
                    dataStore.load();
                }
            },
            device,
            register;

        contentPanel.setLoading();
        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                device = record;
                me.getApplication().fireEvent('loadDevice', device);
                onDependenciesLoad();
            }
        });
        registerModel.getProxy().setExtraParam('mRID', encodeURIComponent(mRID));
        registerModel.load(registerId, {
            success: function (record) {
                register = record;
                me.getApplication().fireEvent('loadRegisterConfiguration', register);
                onDependenciesLoad();
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            grid = me.getPage().down('grid'),
            record = grid.getView().getSelectionModel().getLastSelected();

        switch (item.action) {
            case 'confirmValue':
                me.getPage().setLoading();
                record.getProxy().extraParams = ({mRID: router.arguments.mRID, registerId: router.arguments.registerId});
                record.set('isConfirmed', true);
                record.save({
                    callback: function (rec, operation, success) {
                        if (success) {
                            rec.set('validationResult', 'validationStatus.ok');
                            grid.getView().refreshNode(grid.getStore().indexOf(rec));
                            me.getPage().down('form').loadRecord(rec);
                        }
                        me.getPage().setLoading(false);
                    }
                });
                break;
        }
    },

    checkSuspect: function (menu) {
        var me = this,
            record = me.getPage().down('grid').getView().getSelectionModel().getLastSelected(),
            mainStatus = record.get('validationResult').split('.')[1] == 'suspect',
            bulkStatus = record.get('validationResult').split('.')[1] == 'suspect';

        menu.down('#confirm-value').setVisible(mainStatus || bulkStatus);
    }
})
;

