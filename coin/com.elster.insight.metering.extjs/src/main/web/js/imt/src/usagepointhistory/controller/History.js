Ext.define('Imt.usagepointhistory.controller.History', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router',
        'Uni.util.History'
    ],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'
    ],
    stores: [
        'Imt.customattributesonvaluesobjects.store.UsagePointCustomAttributeSets',
        'Imt.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnUsagePoint'
    ],
    views: [
        'Imt.usagepointhistory.view.Overview',
        'Imt.usagepointhistory.view.VersionsOverview'
    ],
    refs: [
        {
            ref: 'page',
            selector: '#usage-point-history'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#usage-point-history #usage-point-history-tab-panel': {
                beforetabchange: me.onBeforeHistoryTabChange
            }
        });
    },

    showHistory: function (mRID) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            customAttributesStore = me.getStore('Imt.customattributesonvaluesobjects.store.UsagePointCustomAttributeSets'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            dependenciesCounter = 2,
            showPage = function () {
                var widget, tabPanel;

                dependenciesCounter--;
                if (!dependenciesCounter) {
                    widget =  Ext.widget('usage-point-history', {
                        itemId: 'usage-point-history',
                        router: router
                    });
                    app.fireEvent('changecontentevent',widget);
                    mainView.setLoading(false);
                    tabPanel = widget.down('#usage-point-history-tab-panel');
                    tabPanel.fireEvent('beforetabchange', tabPanel, tabPanel.getActiveTab());
                }
            };

        customAttributesStore.getProxy().setUrl(mRID);
        customAttributesStore.load(showPage);
        mainView.setLoading();
        me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
            success: function (record) {
                app.fireEvent('usagePointLoaded', record);
                showPage();
            }
        });
    },

    onBeforeHistoryTabChange: function (tabPanel, newCard, oldCard) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionsStore = me.getStore('Imt.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnUsagePoint'),
            attributeSetModel = Ext.ModelManager.getModel('Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'),
            mRID = router.arguments.mRID,
            customAttributeSetId = newCard.customAttributeSetId;

        if (customAttributeSetId != router.queryParams.customAttributeSetId) {
            Uni.util.History.setParsePath(false);
            Uni.util.History.suspendEventsForNextCall();
            router.queryParams.customAttributeSetId = customAttributeSetId;
            router.getRoute().forward(router.arguments, router.queryParams);
        }

        versionsStore.getProxy().setUrl(mRID, customAttributeSetId);

        Ext.suspendLayouts();
        if (oldCard) {
            oldCard.removeAll();
        }
        newCard.add({
            xtype: 'custom-attribute-set-versions-overview',
            itemId: 'custom-attribute-set-versions-overview',
            title: Uni.I18n.translate('customattributesets.versions', 'IMT', 'Versions'),
            store: versionsStore,
            type: 'usagePoint',
            ui: 'medium',
            padding: 0
        });
        Ext.resumeLayouts(true);

        attributeSetModel.getProxy().setUrl(mRID);
        attributeSetModel.load(customAttributeSetId, {
            success: function (record) {
                var addBtn, addBtnTop, actionColumn;

                if (newCard.rendered) {
                    Ext.suspendLayouts();
                    addBtn = newCard.down('#custom-attribute-set-add-version-btn');
                    addBtnTop = newCard.down('#custom-attribute-set-add-version-btn-top');
                    actionColumn = newCard.down('#custom-attribute-set-versions-grid-action-column');

                    if (addBtn) {
                        addBtn.setVisible(record.get('editable'));
                    }
                    if (addBtnTop) {
                        addBtnTop.setVisible(record.get('editable'));
                    }
                    if (actionColumn) {
                        actionColumn.setVisible(record.get('editable'));
                    }
                    Ext.resumeLayouts(true);
                }
            }
        });
    }
});