/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        'Imt.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnUsagePoint',
        'Imt.usagepointmanagement.store.CalendarHistory',
        'Imt.usagepointhistory.store.LifeCycleAndState',
        'Imt.usagepointhistory.store.HistoricalMeters'
    ],
    views: [
        'Imt.usagepointhistory.view.Overview',
        'Imt.usagepointhistory.view.VersionsOverview',
        'Imt.usagepointhistory.view.CalendarsVersionsOverview',
        'Imt.usagepointhistory.view.lifecycleandstate.LifeCycleAndState',
        'Imt.usagepointhistory.view.meters.Meters'
    ],
    refs: [
        {
            ref: 'page',
            selector: '#usage-point-history'
        },
        {
            ref: 'metersPage',
            selector: '#usage-point-history usage-point-history-meters'
        }
    ],
    usagePointId: null,
    router: null,

    init: function () {
        var me = this;

        me.control({
            '#usage-point-history #usage-point-history-tab-panel': {
                beforetabchange: me.onBeforeHistoryTabChange
            },
            'life-cycle-and-state actioncolumn': {
                abortTransition: me.abortTransition
            }
        });
    },

    showHistory: function (usagePointId) {
        var me = this,
            app = me.getApplication(),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            customAttributesStore = me.getStore('Imt.customattributesonvaluesobjects.store.UsagePointCustomAttributeSets'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');

        viewport.setLoading();
        usagePointsController.loadUsagePoint(usagePointId, {
            success: function (types, usagePoint) {
                customAttributesStore.getProxy().setExtraParam('usagePointId', usagePointId);
                customAttributesStore.load(function () {
                    var widget, tabPanel;

                    widget = Ext.widget('usage-point-history', {
                        itemId: 'usage-point-history',
                        router: router,
                        usagePoint: usagePoint
                    });
                    app.fireEvent('changecontentevent', widget);
                    viewport.setLoading(false);
                    tabPanel = widget.down('#usage-point-history-tab-panel');
                    tabPanel.fireEvent('beforetabchange', tabPanel, tabPanel.getActiveTab(), undefined, undefined, true);
                });
            },
            failure: function () {
                viewport.setLoading(false);
            }
        });
    },

    onBeforeHistoryTabChange: function (tabPanel, newCard, oldCard, eOpts, isInit) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (!newCard) {
            return;
        }

        me.usagePointId = decodeURIComponent(router.arguments.usagePointId);
        me.newCard = newCard;
        if (oldCard) {
            oldCard.removeAll();
        }

        switch (newCard.itemId) {
            case 'calendar-tab':
                me.showCalendarTab();
                break;
            case 'meters-tab':
                me.showMetersTab();
                break;
            case 'up-life-cycle-tab':
                me.showLifeCycleTab();
                break;
            default:
                me.showCustomAttributeSetTab(isInit);
                break;
        }
    },

    abortTransition: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        record.getProxy().setParams(router.arguments.usagePointId);
        Ext.create('Uni.view.window.Confirmation', {confirmText: Uni.I18n.translate('general.abort', 'IMT', 'Abort')}).show({
            title: Uni.I18n.translate('usagePointHistory.abortTransitionTitle', 'IMT', "Abort state change from '{0}' to '{1}'?", [record.get('fromStateName'), record.get('toStateName')]),
            msg: Uni.I18n.translate('usagePointHistory.abortTransitionMsg', 'IMT', 'The scheduled state change will be aborted.'),
            config: {
                record: record
            },
            fn: function (action) {
                if (action == 'confirm') {
                    record.save({
                        success: function () {
                            router.getRoute().forward();
                        }
                    });
                }
            }
        });
    },

    showCalendarTab: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            calendarStore = me.getStore('Imt.usagepointmanagement.store.CalendarHistory'),
            url;

        if (router.queryParams.customAttributeSetId) {
            delete router.queryParams.customAttributeSetId;
            url = router.getRoute().buildUrl(router.arguments, router.queryParams);
            Uni.util.History.setParsePath(false);
            Uni.util.History.suspendEventsForNextCall();
            window.location.replace(url);
        }
        calendarStore.setName(me.usagePointId);
        me.newCard.add({
            xtype: 'calendars-versions-overview',
            store: calendarStore,
            type: 'usagePoint',
            ui: 'medium',
            padding: 0
        });
    },

    showMetersTab: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            metersStore = me.getStore('Imt.usagepointhistory.store.HistoricalMeters');

        metersStore.getProxy().setParams(me.usagePointId);
        metersStore.load(function() {
            me.newCard.add({
                xtype: 'usage-point-history-meters',
                itemId: 'usage-point-history-meters',
                router: router,
                margin: '10 0 0 0'
            });
            me.getMetersPage().setLoading(false);
        });
    },

    showLifeCycleTab: function () {
        var me = this,
            lifeCycleAndStateStore = me.getStore('Imt.usagepointhistory.store.LifeCycleAndState');

        lifeCycleAndStateStore.getProxy().setParams(me.usagePointId);
        me.newCard.add({
            xtype: 'life-cycle-and-state',
            itemId: 'life-cycle-and-state',
            margin: '10 0 0 0'
        });
        lifeCycleAndStateStore.load();
    },

    showCustomAttributeSetTab: function (isInit) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionsStore = me.getStore('Imt.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnUsagePoint'),
            attributeSetModel = Ext.ModelManager.getModel('Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'),
            customAttributeSetId = me.newCard.customAttributeSetId,
            cardView,
            onVersionsStoreLoad,
            url;

        if (customAttributeSetId != router.queryParams.customAttributeSetId) {
            router.queryParams.customAttributeSetId = customAttributeSetId;
            url = router.getRoute().buildUrl(router.arguments, router.queryParams);
            Uni.util.History.setParsePath(false);
            Uni.util.History.suspendEventsForNextCall();
            delete router.queryParams.selectCurrent;
            if (isInit) {
                window.location.replace(url);
            } else {
                window.location.href = url;
            }
        }
        versionsStore.getProxy().setParams(me.usagePointId, customAttributeSetId);
        cardView = me.newCard.add({
            xtype: 'custom-attribute-set-versions-overview',
            itemId: 'custom-attribute-set-versions-setup-id',
            title: Uni.I18n.translate('customattributesets.versions', 'IMT', 'Versions'),
            store: versionsStore,
            type: 'usagePoint',
            ui: 'medium',
            padding: 0,
            selectByDefault: !router.queryParams.selectCurrent
        });
        if (router.queryParams.selectCurrent) {
            onVersionsStoreLoad = function () {
                var currentVersion = versionsStore.find('isActive', true);

                if (cardView.rendered) {
                    cardView.down('custom-attribute-set-versions-grid').getSelectionModel().select(currentVersion > -1 ? currentVersion : 0);
                }
            };
            versionsStore.on('load', onVersionsStoreLoad, me);
            cardView.on('destroy', function () {
                versionsStore.un('load', onVersionsStoreLoad, me);
            });
        }
        attributeSetModel.getProxy().setExtraParam('usagePointId', me.usagePointId);
        attributeSetModel.load(customAttributeSetId, {
            success: function (record) {
                var isEditable, addBtn, addBtnTop, actionColumn, actionBtn;

                if (me.newCard.rendered) {
                    Ext.suspendLayouts();
                    isEditable = record.get('isEditable');
                    addBtn = me.newCard.down('#custom-attribute-set-add-version-btn');
                    addBtnTop = me.newCard.down('#custom-attribute-set-add-version-btn-top');
                    actionColumn = me.newCard.down('#custom-attribute-set-versions-grid-action-column');
                    actionBtn = me.newCard.down('#custom-attribute-set-versions-preview-action-button');

                    if (addBtn) {
                        addBtn.setVisible(isEditable);
                    }
                    if (addBtnTop) {
                        addBtnTop.setVisible(isEditable);
                    }
                    if (actionColumn) {
                        actionColumn.setVisible(isEditable);
                    }
                    if (actionBtn) {
                        actionBtn.setVisible(isEditable);
                    }
                    Ext.resumeLayouts(true);
                }
            }
        });
    }
});