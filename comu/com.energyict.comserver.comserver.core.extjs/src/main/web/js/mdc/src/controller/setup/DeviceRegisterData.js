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
            registersOfDeviceStore = me.getStore('RegisterConfigsOfDevice'),
            router = me.getController('Uni.controller.history.Router');
        contentPanel.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setExtraParam('mRID', encodeURIComponent(mRID));

                model.load(registerId, {
                    success: function (register) {
                        var type = register.get('type'),
                            dataStore = me.getStore(type.charAt(0).toUpperCase() + type.substring(1) + 'RegisterData');

                        if ((type === 'billing' || type === 'numerical') && !router.queryParams.interval) {
                            var intervalStart = (new Date()).setHours(0, 0, 0, 0);
                            router.getRoute().forward(null, Ext.apply(router.queryParams, {
                                interval: intervalStart + '-' + moment(intervalStart).add('years', 1).valueOf()
                            }));
                        } else {
                            var widget = Ext.widget('tabbedDeviceRegisterView', {
                                device: device,
                                router: router
                            });
                            widget.down('#registerTabPanel').setTitle(register.get('readingType').fullAliasName);
                            me.getApplication().fireEvent('loadRegisterConfiguration', register);
                            var func = function () {
                                me.getApplication().fireEvent('changecontentevent', widget);
                                tabController.showTab(1);
                                var dataReport = Ext.widget('deviceregisterreportsetup-' + type, {
                                    mRID: mRID,
                                    registerId: registerId
                                });
                                widget.down('#register-data').add(dataReport);
                                var valueColumn = widget.down('grid').down('[dataIndex=value]');
                                valueColumn.setText(Uni.I18n.translate('device.registerData.value', 'MDC', 'Value') + ' (' + register.get('lastReading')['unitOfMeasure'] + ')');

                                if (type === 'billing' || type === 'numerical') {
                                    me.getFilterPanel().bindStore(dataStore);
                                    var deltaValueColumn = widget.down('grid').down('[dataIndex=deltaValue]');
                                    deltaValueColumn.setText(Uni.I18n.translate('device.registerData.deltaValue', 'MDC', 'Delta value') + ' (' + register.get('lastReading')['unitOfMeasure'] + ')');
                                    deltaValueColumn.setVisible(register.get('isCumulative'));
                                }

                                dataStore.load();
                            };
                            if (registersOfDeviceStore.getTotalCount() === 0) {
                                registersOfDeviceStore.getProxy().url = registersOfDeviceStore.getProxy().url.replace('{mRID}', encodeURIComponent(mRID));
                                registersOfDeviceStore.load(function () {
                                    func();
                                });
                            } else {
                                func();
                            }
                        }
                    },
                    callback: function () {
                        contentPanel.setLoading(false);
                    }
                });
            }
        });
    }
})
;

