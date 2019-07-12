/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.Overview', {
    extend: 'Ext.app.Controller',

    models: [
        'Isu.model.Group'
    ],

    stores: [
        'Isu.store.Clipboard',
        'Isu.store.IssueStatuses',
        'Isu.store.IssueReasons',
        'Isu.store.DueDate',
        'Isu.store.ManuallyRuleItems'
    ],

    views: [
        'Isu.view.overview.Overview'
    ],

    refs: [
        {
            ref: 'overview',
            selector: 'overview-issues-panel'
        },
        {
            ref: 'filterToolbar',
            selector: 'overview-issues-panel isu-view-issues-issuefilter'
        },
        {
            ref: 'noPanelFound',
            selector: 'overview-issues-panel #overview-no-issues-found-panel'
        },
        {
            ref: 'historyOverview',
            selector: 'history-issues-panel'
        },
        {
            ref: 'historyFilterToolbar',
            selector: 'history-issues-panel #view-issue-history-filter'
        },
        {
            ref: 'noHistoryItemsFoundPanel',
            selector: 'history-issues-panel #overview-no-history-issues-found-panel'
        },
        {
            ref: 'graphsPanel',
            selector: 'history-issues-panel #overview-graphs-panel'
        }
    ],

    sections: ['issueType', 'status', 'userAssignee', 'reason', 'workGroupAssignee'],
    historySections: ['crt-issues-per-reason', 'crt-issues-open-closed', 'crt-issues-per-priority'],
    historyFilter: ['reason', 'issueType'],
    widgetType: 'overview-of-issues',
    model: 'Isu.model.Group',

    init: function () {
        this.control({
            'overview-issues-panel button[action=applyAll]': {
                click: this.updateSections
            },
            'overview-issues-panel button[action=clearAll]': {
                click: this.clearAllFilters
            },
            'history-issues-panel button[action=applyAll]': {
                click: this.updateHistorySections
            },
            'history-issues-panel button[action=clearAll]': {
                click: this.clearAllHistoryFilters
            }

        });
    },

    showIssuesOverview: function () {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        if (_.isEmpty(queryString)) {
            queryString.status = ['status.open', 'status.in.progress'];
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
        } else {
            me.getApplication().fireEvent('changecontentevent', Ext.widget(me.widgetType, {
                router: me.getController('Uni.controller.history.Router')
            }));
            me.getOverview().down('button[action=clearAll]').setDisabled(false);
            me.updateSections();
            me.updateHistorySections();
        }
    },

    updateSections: function (btn) {
        var me = this;

        Ext.Array.each(me.sections, function (section) {
            var sectionPanel = me.getOverview().down('#' + section);
            if (!sectionPanel.store) {
                sectionPanel.store = new Ext.data.Store({
                    model: me.model
                });
                sectionPanel.store.getProxy().pageParam = false;
                sectionPanel.store.getProxy().startParam = false;
                sectionPanel.store.getProxy().limitParam = false;
            }
            sectionPanel.store.load({
                params: me.getGroupProxyParams(section),
                callback: function () {
                    me.getNoPanelFound().setVisible(!this.getCount());
                    me.getOverview().down('#sections-panel').setVisible(this.getCount());
                    Ext.suspendLayouts();
                    sectionPanel.fillSection(this, section);
                    Ext.resumeLayouts(true);
                }
            });
        });

        if (btn) {
            me.getOverview().down('button[action=clearAll]').enable();
        }
    },

    getGroupProxyParams: function (section) {
        var me = this,
            filterToolbar = me.getFilterToolbar(),
            filter = filterToolbar.getFilterParams(false, !filterToolbar.filterObjectEnabled),
            params = {
                filter: [
                    {
                        property: 'field',
                        value: section
                    }
                ]
            };

        Ext.iterate(filter, function (key, value) {
            if (!Ext.isEmpty(value)) {
                params.filter.push({
                    property: key,
                    value: value
                });
            }
        });

        params.filter = Ext.encode(params.filter);

        return params;
    },

    updateHistorySections: function (btn) {
        var me = this,
            historyOverview = me.getHistoryOverview(),
            noHistoryItemsFoundPanel = me.getNoHistoryItemsFoundPanel(),
            graphsPanel = me.getGraphsPanel();

        noHistoryItemsFoundPanel.setVisible(false);
        Ext.Array.each(me.historySections, function (historySection) {
            var historySection = historyOverview.down('#' + historySection);
            historySection.destroyControls();
        });

        graphsPanel.setVisible(true);

        Ext.Array.each(me.historySections, function (historySection) {
            var historySection = historyOverview.down('#' + historySection);
            historySection.setLoading();

            if (historySection.traslationStore) {
                var translationReasons = [];
                var issueStatusesStore = me.getStore(historySection.traslationStore);
                issueStatusesStore.load(function (records) {
                    Ext.Array.each(records, function (record) {
                        translationReasons.push({
                            reason: record.get('id'),
                            translation: record.get('name')
                        });
                    });
                    historySection.translationFields = translationReasons;
                    historySection.setTranslationReasons(translationReasons);
                });

                Ext.Ajax.request({
                    params: me.getHistoryGroupProxyParams(historySection),
                    url: historySection.url,
                    method: 'GET',
                    success: function (response) {
                        var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;

                        graphsPanel.setVisible(decoded.fields.length != 0);
                        noHistoryItemsFoundPanel.setVisible(decoded.fields.length == 0);
                        if (decoded.fields.length != 0) {
                            historySection.refresh(decoded);
                        }
                        historySection.setLoading(false);
                    }
                });
            }
            else {
                Ext.Ajax.request({
                    params: me.getHistoryGroupProxyParams(historySection),
                    url: historySection.url,
                    method: 'GET',
                    success: function (response) {
                        var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;

                        graphsPanel.setVisible(decoded.fields.length != 0);
                        noHistoryItemsFoundPanel.setVisible(decoded.fields.length == 0);
                        if (decoded.fields.length != 0) {
                            historySection.refresh(decoded);
                        }
                        historySection.setLoading(false);
                    }
                });
            }
        });

        if (btn) {
            historyOverview.down('button[action=clearAll]').enable();
        }
    },

    getHistoryGroupProxyParams: function (historySection) {
        var me = this,
            filterToolbar = me.getHistoryFilterToolbar(),
            filter = filterToolbar.getFilterParams(false, !filterToolbar.filterObjectEnabled),
            params = {
                filter: [
                    {
                        property: 'field',
                        value: historySection.field
                    },
                    {
                        property: 'application',
                        value: Uni.util.Application.getAppName() == 'MdmApp' ? 'INS' :
                            Uni.util.Application.getAppName() == 'MultiSense' ? 'MultiSense' : ''
                    }
                ]
            };

        Ext.iterate(filter, function (key, value) {
            if (!Ext.isEmpty(value)) {
                params.filter.push({
                    property: key,
                    value: value
                });
            }
        });

        params.filter = Ext.encode(params.filter);

        return params;
    },

    clearAllFilters: function (btn) {
        var me = this;

        me.getFilterToolbar().filters.each(function (filter) {
            filter.resetValue();
        }, me);

        btn.disable();
        me.updateSections();
    },

    clearAllHistoryFilters: function (btn) {
        var me = this;

        me.getHistoryFilterToolbar().filters.each(function (filter) {
            if (me.historyFilter.indexOf(filter) > 0) {
                filter.resetValue();
            }
        }, me);

        btn.disable();
        me.updateHistorySections();
    }

});
