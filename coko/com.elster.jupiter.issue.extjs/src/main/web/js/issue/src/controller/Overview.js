Ext.define('Isu.controller.Overview', {
    extend: 'Ext.app.Controller',

    models: [
        'Isu.model.Group'
    ],

    stores: [
        'Isu.store.Clipboard',
        'Isu.store.IssueStatuses',
        'Isu.store.DueDate'
    ],

    views: [
        'Isu.view.overview.Overview'
    ],

    refs: [
        {
            ref: 'overview',
            selector: 'overview-of-issues'
        },
        {
            ref: 'filterToolbar',
            selector: 'overview-of-issues isu-view-issues-issuefilter'
        },
        {
            ref: 'noPanelFound',
            selector: 'overview-of-issues #overview-no-issues-found-panel'
        }
    ],

    sections: ['issueType', 'status', 'userAssignee', 'reason', 'workGroupAssignee'],

    widgetType: 'overview-of-issues',
    model: 'Isu.model.Group',

    init: function () {
        this.control({
            'overview-of-issues button[action=applyAll]': {
                click: this.updateSections
            },
            'overview-of-issues button[action=clearAll]': {
                click: this.clearAllFilters
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

    clearAllFilters: function (btn) {
        var me = this;

        me.getFilterToolbar().filters.each(function (filter) {
            filter.resetValue();
        }, me);

        btn.disable();
        me.updateSections();
    }
});
