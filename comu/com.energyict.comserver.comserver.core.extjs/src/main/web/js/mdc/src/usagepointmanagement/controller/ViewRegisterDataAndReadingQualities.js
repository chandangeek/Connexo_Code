/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.controller.ViewRegisterDataAndReadingQualities', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.usagepointmanagement.model.UsagePoint',
        'Mdc.usagepointmanagement.model.Register',
        'Mdc.usagepointmanagement.model.RegisterReading'
    ],

    stores: [
        'Mdc.usagepointmanagement.store.Registers',
        'Mdc.usagepointmanagement.store.RegisterData',
        'Mdc.store.LoadProfileDataDurations',
        'Uni.store.DataIntervalAndZoomLevels'
    ],

    views: [
        'Mdc.usagepointmanagement.view.ViewRegisterDataAndReadingQualities'
    ],

    refs: [
        {
            ref: 'preview',
            selector: '#view-register-data-and-reading-qualities #register-data-preview'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#view-register-data-and-reading-qualities #register-data-grid': {
                select: me.showPreview
            }
        });
    },

    showOverview: function (usagePointId, registerId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            registerModel = me.getModel('Mdc.usagepointmanagement.model.Register'),
            RegisterReading = me.getModel('Mdc.usagepointmanagement.model.RegisterReading'),
            registersStore = me.getStore('Mdc.usagepointmanagement.store.Registers'),
            registerDataStore = me.getStore('Mdc.usagepointmanagement.store.RegisterData'),
            dependenciesCounter = 3,
            onDependencyLoad = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    pageMainContent.setLoading(false);
                    Ext.suspendLayouts();
                    app.fireEvent('usagePointLoaded', usagePoint);
                    app.fireEvent('usagePointRegisterLoaded', register);
                    registerDataStore.getProxy().setParams(usagePointId, registerId);
                    app.fireEvent('changecontentevent', Ext.widget('view-register-data-and-reading-qualities', {
                        itemId: 'view-register-data-and-reading-qualities',
                        router: router,
                        register: register,
                        usagePointId: usagePointId,
                        filter: filter
                    }));
                    registerDataStore.load();
                    Ext.resumeLayouts(true);
                }
            },
            usagePoint,
            register,
            filter;

        pageMainContent.setLoading();

        registersStore.getProxy().setExtraParam('usagePointId', usagePointId);
        registersStore.suspendEvent('beforeload');
        registersStore.load(function () {
            registersStore.resumeEvent('beforeload');
            onDependencyLoad();
        });

        me.getModel('Mdc.usagepointmanagement.model.UsagePoint').load(usagePointId, {
            success: function (record) {
                usagePoint = record;
                onDependencyLoad();
            }
        });

        registerModel.getProxy().setExtraParam('usagePointId', usagePointId);
        registerModel.load(registerId, {
            success: function (record) {
                me.setDataFilter();
                onDependencyLoad();
            }
        });

        RegisterReading.getProxy().setExtraParam('usagePointId', usagePointId);
        RegisterReading.getProxy().setExtraParam('registerId', registerId);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview();

        preview.loadRecord(record);
    },

    setDataFilter: function (register) {
        return {
                defaultFromDate: moment().startOf('day').subtract(1, 'years').toDate(),
                defaultDuration: '1years'
            };
    }
});