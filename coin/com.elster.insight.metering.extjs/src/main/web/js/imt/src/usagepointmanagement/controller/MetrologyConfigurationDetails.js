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
    usagePoint: null,

    init: function () {
        this.control({
            'usage-point-metrology-configuration-details purposes-grid': {
                select: this.showPreview
            }
        });
    },

    showUsagePointMetrologyConfiguration: function (mRID) {
        var me = this,
            resultSet,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');

        usagePointsController.loadUsagePoint(mRID, {
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
        var me = this,
            model = Ext.create('Imt.metrologyconfiguration.model.Formula'),
            reader = model.getProxy().getReader(),
            metrologyContracts = me.usagePoint.get('metrologyConfiguration').metrologyContracts,
            resultSet;

        Ext.suspendLayouts();
        me.getPage().down('purposes-preview').setTitle(Ext.String.htmlEncode(record.get('name')));
        me.getPage().down('#purposes-preview-container').removeAll(true);
        me.getPage().down('#purposes-preview-container').add(Ext.widget('displayfield', {
                fieldLabel: ' ',
                value: ' '
            }
        ));
        Ext.Array.each(record.get('meterRoles'), function (meterRole) {
            var deviceLink;
            if (meterRole.mRID) {
                if (meterRole.url) {
                    deviceLink = Ext.String.format('<a href="{0}" target="_blank">{1}</a>', meterRole.url, Ext.String.htmlEncode(meterRole.mRID));
                } else {
                    deviceLink = Ext.String.htmlEncode(meterRole.mRID);
                }
            } else {
                deviceLink = '-';
            }
            me.getPage().down('#purposes-preview-container').add(Ext.widget('displayfield', {
                    htmlEncode: false,
                    fieldLabel: meterRole.name,
                    itemId: meterRole.mRID,
                    value: deviceLink
                }
            ));
        });

        Ext.Array.each(metrologyContracts, function (metrologyContract) {
            if(metrologyContract.name == record.get('name')){
                Ext.Array.each(metrologyContract.readingTypeDeliverables, function (readingTypeDeliverable) {
                    resultSet = reader.readRecords(readingTypeDeliverable.formula); // Making record with associated data
                    me.getPage().down('purposes-preview').addFormulaComponents(resultSet.records[0], me.usagePoint.customPropertySets());
                });
            }
        });
        Ext.resumeLayouts(true);
    }
});