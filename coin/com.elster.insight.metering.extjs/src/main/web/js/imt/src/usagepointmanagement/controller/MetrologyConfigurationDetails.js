Ext.define('Imt.usagepointmanagement.controller.MetrologyConfigurationDetails', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    stores: [
        'Imt.usagepointmanagement.store.metrologyconfiguration.MeterRoles',
        'Imt.usagepointmanagement.store.metrologyconfiguration.Purposes'
    ],

    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.usagepointmanagement.model.Purpose'
    ],

    views: [
        'Imt.usagepointmanagement.view.metrologyconfiguration.Details'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'usage-point-metrology-configuration-details'
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
            }
        });
    },

    showUsagePointMetrologyConfiguration: function (usagePointId) {
        var me = this,
            resultSet,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');

        usagePointsController.loadUsagePoint(usagePointId, {
            success: function (types, usagePoint) {
                me.usagePoint = usagePoint;
                me.getApplication().fireEvent('changecontentevent', Ext.widget('usage-point-metrology-configuration-details', {
                    itemId: 'usage-point-metrology-configuration-details',
                    router: router,
                    usagePoint: usagePoint,
                    meterRolesAvailable: usagePoint.get('metrologyConfiguration_meterRoles')
                }));

            },
            failure: function () {
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
            usagePointPurpose = Ext.create('Imt.usagepointmanagement.model.Purpose', record.getData());

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
            }
        });
    }
});