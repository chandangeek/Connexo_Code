/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceHistory', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.util.Common',
        'Mdc.controller.setup.IssueSetPriority',
        'Mdc.controller.setup.AlarmSetPriority',
        'Mdc.controller.setup.ApplyAlarmAction',
        'Mdc.controller.setup.ApplyIssueAction',
        'Mdc.controller.setup.IssueStartProcess',
        'Mdc.controller.setup.AlarmStartProcess',
        'Dal.controller.SetPriority',
        'Dal.controller.Detail'
    ],

    views: [
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.devicehistory.Setup',
        'Mdc.view.setup.devicehistory.LifeCycle',
        'Mdc.view.setup.devicehistory.Firmware',
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsOnDevice'
    ],

    stores: [
        'Mdc.store.DeviceLifeCycleStatesHistory',
        'Mdc.store.DeviceFirmwareHistory',
        'Mdc.customattributesonvaluesobjects.store.DeviceCustomAttributeSets',
        'Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnDevice',
        'Mdc.store.device.MeterActivations',
        'Isu.store.Issues',
        'Mdc.store.device.IssuesAlarms'
    ],

    models: [
        'Mdc.model.DeviceLifeCycleStatesHistory',
        'Mdc.model.DeviceFirmwareHistory',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice',
        'Mdc.model.Device'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'device-history-setup'
        },
        {
            ref: 'tabPanel',
            selector: 'device-history-setup tabpanel'
        }
    ],

    init: function () {
        this.control({
            '#device-history-issues-alarms-tab #issues-alarms-grid': {
                select: this.showIssueAndAlarmPreview
            }
        });
        this.getController('Mdc.controller.setup.ApplyAlarmAction');
    },

    showDeviceHistory: function (deviceId) {
        deviceId = encodeURIComponent(deviceId);
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            router = me.getController('Uni.controller.history.Router'),
            view,
            issuesAlarmsStore = me.getStore('Mdc.store.device.IssuesAlarms');

        Ext.Ajax.request({
            url: '/api/usr/currentuser',
            success: function (response) {
                var currentUser = Ext.decode(response.responseText, true);
                me.currentUserId = currentUser.id;
            }
        });

        issuesAlarmsStore.getProxy().setUrl(deviceId);
        deviceModel.load(deviceId, {
            success: function (device) {
                view = Ext.widget('device-history-setup', {
                    router: me.getController('Uni.controller.history.Router'),
                    device: device,
                    controller: me,
                    activeTab: (!_.isEmpty(router.queryParams) && router.queryParams.activeTab == 'meterActivations') ? 2 : 0
                });
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', view);
                issuesAlarmsStore.load();
                me.showDeviceLifeCycleHistory();
                me.showCustomAttributeSetsHistory(deviceId);
                me.showIssuesAndAlarms(deviceId);
            }
        });
    },

    showDeviceLifeCycleHistory: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            routerArguments = Uni.util.Common.decodeURIArguments(router.arguments),
            lifeCycleTab = me.getPage().down('#device-history-life-cycle-tab'),
            lifeCyclePanel = Ext.widget('device-history-life-cycle-panel'),
            lifeCycleDataView = lifeCyclePanel.down('#life-cycle-data-view'),
            lifeCycleHistoryStore = me.getStore('Mdc.store.DeviceLifeCycleStatesHistory'),
            firmwareTab = me.getPage().down('#device-history-firmware-tab'),
            firmwareHistoryPanel = Ext.widget('device-history-firmware-panel'),
            firmwareHistoryStore = me.getStore('Mdc.store.DeviceFirmwareHistory');

        me.getPage().setLoading();
        lifeCycleTab.add(lifeCyclePanel);
        lifeCycleDataView.bindStore(lifeCycleHistoryStore);
        Ext.apply(lifeCycleHistoryStore.getProxy().extraParams, routerArguments);
        lifeCycleHistoryStore.load(function (records) {
            lifeCycleHistoryStore.add(records.reverse());


            Ext.apply(firmwareHistoryStore.getProxy().extraParams, routerArguments);
            firmwareHistoryStore.load(function() {
                if (firmwareHistoryStore.getTotalCount()===0) {
                    firmwareTab.add({
                        xtype: 'form',
                        items: {
                            xtype: 'uni-form-empty-message',
                            text: Uni.I18n.translate('general.noFirmwareHistory', 'MDC', 'No firmware history available')
                        }
                    });
                } else {
                    firmwareTab.add(firmwareHistoryPanel);
                }
                me.getPage().setLoading(false);
            });
        });
    },

    showIssuesAndAlarms: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryParams = router.queryParams;

        if (queryParams.activeTab === 'issues') {
            me.getTabPanel().setActiveTab('device-history-issues-alarms-tab');
        }


    },

    showCustomAttributeSetsHistory: function (deviceId) {
        var me = this,
            customAttributesStore = me.getStore('Mdc.customattributesonvaluesobjects.store.DeviceCustomAttributeSets');

        customAttributesStore.getProxy().setExtraParam('deviceId', deviceId);

        customAttributesStore.load(function () {
            me.getPage().loadCustomAttributeSets(this);
            me.showCustomAttributeSet(deviceId);
        });
    },

    showMeterActivations: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = Ext.getStore('Mdc.store.device.MeterActivations');

        store.getProxy().setExtraParam('deviceId', decodeURIComponent(router.arguments.deviceId));
        store.load();
    },

    showCustomAttributeSet: function (deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionsStore = me.getStore('Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnDevice'),
            attributeSetModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice'),
            queryParams = router.queryParams,
            component;

        attributeSetModel.getProxy().setExtraParam('deviceId', deviceId);
        if (queryParams.customAttributeSetId) {
            versionsStore.getProxy().setParams(deviceId, queryParams.customAttributeSetId);
            component = me.getTabPanel().down('#custom-attribute-set-' + queryParams.customAttributeSetId);
            component.add({
                xtype: 'device-history-custom-attribute-sets-versions'
            });
            me.getTabPanel().setActiveTab(component);
            attributeSetModel.load(queryParams.customAttributeSetId, {
                success: function (customattributeset) {
                    component.down('#custom-attribute-set-add-version-btn').setVisible(customattributeset.get('editable'));
                    component.down('#custom-attribute-set-versions-grid-action-column').setVisible(customattributeset.get('editable'));
                    component.down('#custom-attribute-set-add-version-btn-top').setVisible(customattributeset.get('editable'));
                }
            });
        }

        me.getTabPanel().on('tabchange', function(tabpanel, tabItem) {
            if (tabItem.itemId !== 'device-history-firmware-tab'
                && tabItem.itemId !== 'device-history-life-cycle-tab'
                && tabItem.itemId !== 'device-history-meter-activations-tab' && tabItem.itemId !== 'device-history-issues-alarms-tab') {
                router.queryParams = {};
                if (tabItem.customAttributeSetId) {
                    router.queryParams.customAttributeSetId = tabItem.customAttributeSetId;
                }
                router.getRoute().forward(null, router.queryParams);
            }
        });
    },


    showActionOverview: function (deviceId, issueId, actionId) {
        var me = this,
            store = me.getStore('Mdc.store.device.IssuesAlarms'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            issueType = queryString.issueType;

        if (store.getCount()) {
            var issueActualType = store.getById(parseInt(issueId)).get('issueType').uid;
            if (issueActualType != issueType) {
                queryString.issueType = issueActualType;
                window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                issueType = issueActualType;
            }
        }

        if ((issueType === 'datacollection') || (issueType === 'datavalidation') || (issueType === 'devicelifecycle')) {
            if (actionId) {
                me.getController('Mdc.controller.setup.ApplyIssueAction').queryParams = {activeTab: 'issues'};
                me.getController('Mdc.controller.setup.ApplyIssueAction').showOverview(issueId, actionId);
            } else {
                me.getController('Mdc.controller.setup.ApplyIssueAction').queryParams = {activeTab: 'issues'};
                me.getController('Mdc.controller.setup.ApplyIssueAction').showOverview(deviceId, issueId);
            }
        }
        else if (issueType === 'devicealarm') {
            if (actionId) {
                me.getController('Mdc.controller.setup.ApplyAlarmAction').queryParams = {activeTab: 'issues'};
                me.getController('Mdc.controller.setup.ApplyAlarmAction').showOverview(issueId, actionId);
            } else {
                me.getController('Mdc.controller.setup.ApplyAlarmAction').queryParams = {activeTab: 'issues'};
                me.getController('Mdc.controller.setup.ApplyAlarmAction').showOverview(deviceId, issueId);
            }
        }
    },

    setPriority: function (deviceId, issueId) {
        var me = this,
            store = me.getStore('Mdc.store.device.IssuesAlarms');

        if (store.getCount()) {
            var issueActualType = store.getById(parseInt(issueId)).get('issueType').uid;
            if ((issueActualType === 'datacollection') || (issueActualType === 'datavalidation') || (issueActualType === 'devicelifecycle')) {
                me.getController('Mdc.controller.setup.IssueSetPriority').queryParams = {activeTab: 'issues'};
                me.getController('Mdc.controller.setup.IssueSetPriority').setPriority(issueId);
            }
            else if (issueActualType === 'devicealarm') {
                me.getController('Mdc.controller.setup.AlarmSetPriority').queryParams = {activeTab: 'issues'};
                me.getController('Mdc.controller.setup.AlarmSetPriority').setPriority(issueId);
            }
        }
    },

    startProcess: function (deviceId, issueId) {
        var me = this,
            store = me.getStore('Mdc.store.device.IssuesAlarms');

        if (store.getCount()) {
            var issueActualType = store.getById(parseInt(issueId)).get('issueType').uid;
            if ((issueActualType === 'datacollection') || (issueActualType === 'datavalidation')) {
                me.getController('Mdc.controller.setup.IssueStartProcess').queryParams = {activeTab: 'issues'};
                me.getController('Mdc.controller.setup.IssueStartProcess').showStartProcess(issueId);
            }
            else if (issueActualType === 'devicealarm') {
                me.getController('Mdc.controller.setup.AlarmStartProcess').queryParams = {activeTab: 'issues'};
                me.getController('Mdc.controller.setup.AlarmStartProcess').showStartProcess(issueId);
            }
        }
    },

    showIssueAndAlarmPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('issues-alarms-preview');

        Ext.suspendLayouts();
        preview.down('#issues-preview-actions-button').menu.record = record;
        preview.down('#issue-logbook').setVisible(record.get('issueType').uid == 'devicealarm');
        preview.record = record;
        preview.setTitle(record.get('issueId') + ' ' + record.get('reason'));
        preview.loadRecord(record);
        preview.currentUserId = me.currentUserId;
        Ext.resumeLayouts();
    }
});
