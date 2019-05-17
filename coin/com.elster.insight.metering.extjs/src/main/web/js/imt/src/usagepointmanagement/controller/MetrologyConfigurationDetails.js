/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.controller.MetrologyConfigurationDetails', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    stores: [
        'Imt.usagepointmanagement.store.metrologyconfiguration.MeterRoles',
        'Imt.usagepointmanagement.store.metrologyconfiguration.Purposes',
        'Imt.usagepointmanagement.store.MeterRoles'
    ],

    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.usagepointmanagement.model.Purpose'
    ],

    views: [
        'Imt.usagepointmanagement.view.metrologyconfiguration.Details',
        'Imt.usagepointmanagement.view.metrologyconfiguration.UnlinkMeter'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'usage-point-metrology-configuration-details'
        },
        {
            ref: 'unlinkMeterForm',
            selector: '#unlinkMeter'
        }
    ],
    usagePoint: null,

    init: function () {
        this.control({
            'usage-point-metrology-configuration-details purposes-grid': {
                select: this.showPreview
            },
            '#usage-point-purpose-action-menu': {
                click: this.chooseAction
            },
            'usage-point-metrology-configuration-details #unlink-metrology-configuration-button': {
                click: this.unlinkMetrologyConfiguration
            },
            '#unlinkMeter #unlink-meter-button': {
                click: this.unlinkSaveButton   
            }
        });
    },

    showUsagePointMetrologyConfiguration: function (usagePointId) {
        var me = this,
            resultSet,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');

        viewport.setLoading();
        usagePointsController.loadUsagePoint(usagePointId, {
            success: function (types, usagePoint) {
                me.usagePoint = usagePoint;
                me.getApplication().fireEvent('changecontentevent', Ext.widget('usage-point-metrology-configuration-details', {
                    itemId: 'usage-point-metrology-configuration-details',
                    router: router,
                    usagePoint: usagePoint
                }));
                viewport.setLoading(false);
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this;
        me.getPage().down('purposes-preview').loadRecord(record, me.usagePoint);
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'triggerActivation':
                me.triggerActivation(menu.record);
                break;
        }
    },

    triggerActivation: function (record) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            usagePointPurpose = Ext.create('Imt.usagepointmanagement.model.Purpose', record.getData());

        mainView.setLoading();
        usagePointPurpose.triggerActivation(me.usagePoint, {
            isNotEdit: true,
            success: function (response) {
                var responseObject = Ext.decode(response.responseText);

                if (responseObject.active) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('purpose.activated', 'IMT', 'Purpose activated'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('purpose.deactivated', 'IMT', 'Purpose deactivated'));
                }
                me.getController('Uni.controller.history.Router').getRoute().forward();
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
    },

    unlinkMetrologyConfiguration: function (button) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        button.usagePoint.unlinkMetrologyConfiguration({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('metrologyConfiguration.unlinked', 'IMT', 'Metrology configuration unlinked'));
                me.getController('Uni.controller.history.Router').getRoute().forward();
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
    },

    unlinkMeter: function (usagePointname, meterRoleId, meterName) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');

        usagePointsController.loadUsagePoint(usagePointname, {
            success: function (types, usagePoint) {
                me.usagePoint = usagePoint;
                me.getApplication().fireEvent('changecontentevent', Ext.widget('unlink-meter', {
                    router: router,
                    usagePoint: usagePoint,
                    meterRoleId: meterRoleId,
                    meterName: meterName,
                }));
                me.getApplication().fireEvent('unlinkMeterLoaded', meterName)
            },
            failure: function () {
                viewport.setLoading(false);
            }
        });
    },

    unlinkSaveButton: function(btn) {
        var me = this;
            unlinkTime = this.getUnlinkMeterForm().down('#unlink-meter-date').down('#unlink-date-on').getValue().getTime();

        Ext.Ajax.request({
            url: '/'+ encodeURIComponent(btn.usagePointName) +'/meterroles/'+ encodeURIComponent(btn.meterRoleId) +'/unlink',
            method: 'POST',
            jsonData: {
                instant: unlinkTime
            },
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagePoint.acknowledge.calendarAdded', 'IMT', "Meter '{0}' will be unlinked on '{1}'.", [btn.meterName, new Date(unlinkTime).toLocaleDateString()]));
                me.getController('Uni.controller.history.Router').getRoute('usagepoints/view/calendars').forward();
            },
            failure: function (response) {
                var responseText = Ext.decode(response.responseText, true);
                if (responseText && Ext.isArray(responseText.errors)) {
                    me.getForm().form.markInvalid(responseText.errors);
                    me.getForm().down('#form-errors').show();
                    var fromTimeError = Ext.Array.findBy(responseText.errors, function (item) { return item.id == 'fromTime';});
                    if(fromTimeError) {
                        me.getForm().down('#error-label').setText(fromTimeError.msg);
                        me.getForm().down('#error-label').show();
                    }
                }
            }
        });
    }
});
