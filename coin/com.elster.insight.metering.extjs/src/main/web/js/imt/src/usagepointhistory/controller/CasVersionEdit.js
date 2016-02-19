Ext.define('Imt.usagepointhistory.controller.CasVersionEdit', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint',
        'Imt.customattributesonvaluesobjects.model.AttributeSetVersionOnUsagePoint',
        'Imt.customattributesonvaluesobjects.model.AttributeSetVersionPeriod'
    ],
    stores: [
        'Imt.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'
    ],
    views: [
        'Imt.customattributesonvaluesobjects.view.CustomAttributeSetVersionForm'
    ],

    editCasVersion: function (mRID, customAttributeSetId, versionId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            attributeSetModel = me.getModel('Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'),
            versionModel = me.getModel('Imt.customattributesonvaluesobjects.model.AttributeSetVersionOnUsagePoint'),
            overlapStore = me.getStore('Imt.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'),
            widget,
            versionPeriod;

        versionModel.getProxy().setUrl(mRID, customAttributeSetId);
        overlapStore.getProxy()[Ext.isDefined(versionId) ? 'setUsagePointEditUrl' : 'setUsagePointUrl'](mRID, customAttributeSetId, versionId);
        widget = Ext.widget('custom-attribute-set-version-form', {
            router: router,
            overlapStore: overlapStore,
            versionModel: versionModel,
            type: 'usagePoint',
            pageType: Ext.isDefined(versionId) ? 'edit' : 'add'
        });
        app.fireEvent('changecontentevent', widget);
        widget.setLoading();

        me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
            success: function (record) {
                app.fireEvent('usagePointLoaded', record);
            }
        });
        attributeSetModel.getProxy().setUrl(mRID);

        if (versionId) {
            attributeSetModel.load(customAttributeSetId, {
                success: function (customattributeset) {
                    app.fireEvent('loadCasOnUsagePoint', customattributeset);
                }
            });
            versionModel.load(versionId, {
                success: function (record) {
                    Ext.suspendLayouts();
                    app.fireEvent('loadCasVersionOnUsagePoint', record);
                    widget.down('form').setTitle(router.getRoute().getTitle());
                    widget.loadRecord(record, record, record.get('startTime'), record.get('endTime'));
                    widget.setLoading(false);
                    Ext.resumeLayouts(true);
                }
            });
        } else {
            versionPeriod = me.getModel('Imt.customattributesonvaluesobjects.model.AttributeSetVersionPeriod');
            versionPeriod.getProxy().setUsagePointUrl(mRID, customAttributeSetId);
            attributeSetModel.load(customAttributeSetId, {
                success: function (customattributeset) {
                    app.fireEvent('loadCasOnUsagePoint', customattributeset);
                    versionPeriod.load('currentinterval', {
                        success: function (interval) {
                            Ext.suspendLayouts();
                            widget.loadRecord(customattributeset, Ext.create(versionModel), interval.get('start'), interval.get('end'));
                            app.fireEvent('loadCasOnUsagePointAdd', customattributeset);
                            widget.down('form').setTitle(router.getRoute().getTitle());
                            widget.setLoading(false);
                            Ext.resumeLayouts(true);
                        }
                    });
                }
            });
        }
    },

    cloneCustomAttributeVersion: function (mRID, customAttributeSetId, versionId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            attributeSetModel = me.getModel('Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'),
            versionModel = me.getModel('Imt.customattributesonvaluesobjects.model.AttributeSetVersionOnUsagePoint'),
            versionPeriod = me.getModel('Imt.customattributesonvaluesobjects.model.AttributeSetVersionPeriod'),
            overlapStore = me.getStore('Imt.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'),
            widget;

        versionModel.getProxy().setUrl(mRID, customAttributeSetId);
        versionPeriod.getProxy().setUsagePointUrl(mRID, customAttributeSetId);
        overlapStore.getProxy().setUsagePointUrl(mRID, customAttributeSetId);
        widget = Ext.widget('custom-attribute-set-version-form', {
            router: router,
            overlapStore: overlapStore,
            versionModel: versionModel,
            type: 'usagePoint',
            pageType: 'clone'
        });
        me.getApplication().fireEvent('changecontentevent', widget);

        widget.setLoading();
        me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
            success: function (record) {
                app.fireEvent('usagePointLoaded', record);
            }
        });
        attributeSetModel.getProxy().setUrl(mRID);
        attributeSetModel.load(customAttributeSetId, {
            success: function (customattributeset) {
                app.fireEvent('loadCasOnUsagePoint', customattributeset);
            }
        });

        versionModel.load(versionId, {
            success: function (record) {
                versionPeriod.load('currentinterval', {
                    success: function (interval) {
                        Ext.suspendLayouts();
                        me.getApplication().fireEvent('loadCustomAttributeSetVersionOnUsagePointClone', record);
                        widget.down('form').setTitle(router.getRoute().getTitle());
                        widget.loadRecord(record, Ext.create(versionModel), interval.get('start'), interval.get('end'));
                        widget.setLoading(false);
                        Ext.resumeLayouts(true);
                    }
                });

            }
        });
    }
});