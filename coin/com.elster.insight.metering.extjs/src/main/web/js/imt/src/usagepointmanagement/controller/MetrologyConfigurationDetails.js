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
        'Imt.usagepointmanagement.model.UsagePoint'
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

    init: function () {
        this.control({
            'usage-point-metrology-configuration-details purposes-grid': {
                select: this.showPreview
            }
        });
    },

    showUsagePointMetrologyConfiguration: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');

        usagePointsController.loadUsagePoint(mRID, {
            success: function (types, usagePoint) {
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

        Ext.suspendLayouts();
        me.getPage().down('purposes-preview').setTitle(Ext.String.htmlEncode(record.get('name')));
        me.getPage().down('#purposes-preview-container').removeAll(true);
        me.getPage().down('#purposes-preview-container').add(Ext.widget('displayfield', {
                fieldLabel: ' ',
                value: ' '
            }
        ));
        Ext.Array.each(record.get('meterRoles'), function (meterRole) {
            me.getPage().down('#purposes-preview-container').add(Ext.widget('displayfield', {
                    fieldLabel: meterRole.name,
                    itemId: meterRole.mRID,
                    value: meterRole.mRID || '-'
                }
            ));
        });
        Ext.resumeLayouts(true);
    }
});