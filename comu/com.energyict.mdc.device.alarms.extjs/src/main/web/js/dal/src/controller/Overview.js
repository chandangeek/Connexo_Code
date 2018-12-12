Ext.define('Dal.controller.Overview', {
    extend: 'Isu.controller.Overview',
    requires: [
        'Uni.view.widget.HistoryGraph'
    ],
    models: [
        'Dal.model.Group'
    ],

    stores: [
        'Dal.store.AlarmStatuses',
        'Dal.store.DueDate'
    ],

    views: [
        'Dal.view.overview.Overview',
        'Uni.view.widget.HistoryGraph'
    ],

    sections: ['status', 'userAssignee', 'reason', 'workGroupAssignee'],
    historySections: ['crt-alarms-per-reason', 'crt-alarms-open-closed', 'crt-alarms-per-priority'],
    historyFilter: ['reason'],
    widgetType: 'overview-of-alarms',
    model: 'Dal.model.Group',

    constructor: function () {
        var me = this;

        me.refs = [
            {
                ref: 'overview',
                selector: 'overview-panel'
            },
            {
                ref: 'filterToolbar',
                selector: 'overview-panel view-alarms-filter'
            },
            {
                ref: 'noPanelFound',
                selector: 'overview-panel #overview-no-alarms-found-panel'
            },
            {
                ref: 'historyOverview',
                selector: 'history-panel'
            },
            {
                ref: 'historyFilterToolbar',
                selector: 'history-panel #view-history-filter'
            },
            {
                ref: 'noHistoryAlarmsFoundPanel',
                selector: 'history-panel #overview-no-history-alarms-found-panel'
            },
            {
                ref: 'graphsPanel',
                selector: 'history-panel #overview-graphs-panel'
            }
        ];

        me.callParent(arguments);
    },

    init: function () {
        this.control({
            'overview-panel button[action=applyAll]': {
                click: this.updateSections
            },
            'overview-panel button[action=clearAll]': {
                click: this.clearAllFilters
            },
            'history-panel button[action=applyAll]': {
                click: this.updateHistorySections
            },
            'history-panel button[action=clearAll]': {
                click: this.clearAllHistoryFilters
            }
        });
    },

    showAlarmOverview: function (history) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        if (_.isEmpty(queryString)) {
            queryString.status = ['status.open', 'status.in.progress'];
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
        } else {
            var widget = Ext.widget(me.widgetType, {
                router: me.getController('Uni.controller.history.Router')
            });

            me.getApplication().fireEvent('changecontentevent', widget);

            me.getOverview().down('button[action=clearAll]').setDisabled(false);
            me.getHistoryOverview().down('button[action=clearAll]').setDisabled(true);
            me.updateSections();
            me.updateHistorySections();
        }
    },

    updateHistorySections: function (btn) {
        var me = this,
            historyOverview = me.getHistoryOverview(),
            noHistoryAlarmsFoundPanel = me.getNoHistoryAlarmsFoundPanel(),
            graphsPanel = me.getGraphsPanel();

        noHistoryAlarmsFoundPanel.setVisible(false);
        Ext.Array.each(me.historySections, function (historySection) {
            var historySection = historyOverview.down('#' + historySection);
            historySection.destroyControls();
        });

        graphsPanel.setVisible(true);
        Ext.Array.each(me.historySections, function (historySection) {
            var historySection = historyOverview.down('#' + historySection);

            historySection.setLoading();

            Ext.Ajax.request({
                params: me.getHistoryGroupProxyParams(historySection),
                url: historySection.url,
                method: 'GET',
                success: function (response) {
                    var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;

                    graphsPanel.setVisible(decoded.fields.length != 0);
                    noHistoryAlarmsFoundPanel.setVisible(decoded.fields.length == 0);
                    if (decoded.fields.length != 0) {
                        historySection.refresh(decoded);
                    }
                    historySection.setLoading(false);
                }
            });
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
            if (me.historyFilter.indexOf(filter) == -1) {
                filter.resetValue();
            }
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
    }
});
