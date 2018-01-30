/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceRegisterHistoryData', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.deviceregisterdata.HistorySetup'],

    models: [
        'Mdc.model.Register'
    ],

    stores: [
        'Mdc.store.RegisterHistoryData',
        'Mdc.store.NumericalRegisterHistoryData'
    ],

    refs: [
        {ref: 'previewDeviceRegistersHistory', selector: '#preview-device-registers-history'},
    ],

    init: function () {
        var me = this;

        me.control({
            '#device-registers-history-container #device-registers-history': {
                select: me.loadPreview
            }

        });
    },

    loadPreview: function (rowmodel, record) {
        var me = this;
        me.getPreviewDeviceRegistersHistory().updateContent(record);
    },

    onDataStoreLoad: function (widget, store, register, records) {
        var me = this,
            type = register.get('type'),
            collectedReadingType = register.get('readingType'),
            collectedUnit = collectedReadingType.names.unitOfMeasure,
            calculatedUnit = 'NY',
            isCumulative = register.get('isCumulative'),
            multiplier = register.get('multiplier'),
            hasCalculatedValue = false,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            calculatedValueColumn = widget.down('grid').down('[dataIndex=calculatedValue]'),
            valueColumn = widget.down('grid').down('[dataIndex=value]');

        Ext.Array.each(records, function (record) {
            hasCalculatedValue = hasCalculatedValue || !Ext.isEmpty(record.get('calculatedValue'));
            if (hasCalculatedValue) {
                calculatedUnit = record.get('calculatedUnit');
                return false; // Stop the iteration
            }
        }, me);

        if (valueColumn) {
            valueColumn.setText(Uni.I18n.translate('general.collected', 'MDC', 'Collected') + ' (' + collectedUnit + ')');
        }
        if (calculatedValueColumn) {
            if (hasCalculatedValue) {
                calculatedValueColumn.setText(Uni.I18n.translate('general.calculated', 'MDC', 'Calculated') + ' (' + calculatedUnit + ')');
            }
            calculatedValueColumn.setVisible(hasCalculatedValue);
        }
    },

    viewHistory: function (deviceId, registerId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {

                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setExtraParam('deviceId', deviceId);
                model.load(registerId, {
                    success: function (register) {

                        var type = register.get('type');
                        var widget = Ext.widget('device-register-history', {
                            device: device,
                            router: router,
                            register: register,
                            type: type,
                            showFilter: !router.queryParams.oneInterval,
                            filterDefault: {
                                defaultFromDate: new Date(Number(router.queryParams.endInterval.split('-')[0])),
                                defaultToDate: new Date(Number(router.queryParams.endInterval.split('-')[1]))
                            }
                        });
                        var store = widget.down('#device-registers-history').store;
                        widget.down('#device-register-history-filter') && (widget.down('#device-register-history-filter').store = store);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        store.getProxy().setUrl(deviceId, registerId);
                        if (!!router.queryParams.oneInterval) {
                            store.proxy.extraParams = {
                                filter: Ext.encode([
                                    {
                                        "property": "intervalStart",
                                        "value": Number(router.queryParams.endInterval.split('-')[0])
                                    },
                                    {
                                        "property": "intervalEnd",
                                        "value": Number(router.queryParams.endInterval.split('-')[1])
                                    }
                                ])
                            };
                        }
                        store.load({
                            callback: function (records) {
                                me.getApplication().fireEvent('loadDevice', device);
                                me.getApplication().fireEvent('loadRegisterConfiguration', register);

                                me.onDataStoreLoad(widget, store, register, records);

                                viewport.setLoading(false);
                            }
                        });
                    },
                    failure: function () {
                        viewport.setLoading(false);
                    }
                });
            },
            failure: function () {
                viewport.setLoading(false);
            }
        });
    }
})
;

