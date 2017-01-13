Ext.define('Dal.controller.Overview', {
    extend: 'Ext.app.Controller',

    models: [
        'Dal.model.Group'
    ],

    stores: [
        'Dal.store.Clipboard',
        'Dal.store.AlarmStatuses',
        'Dal.store.DueDate'
    ],

    views: [
        'Dal.view.Overview'
    ],

    refs: [
        {
            ref: 'overview',
            selector: 'overview-of-alarms'
        },
        {
            ref: 'filterToolbar',
            selector: 'overview-of-alarms view-alarms-filter'
        }
    ],

    sections: ['status', 'userAssignee', 'reason', 'workgroupAssignee'],

    init: function () {
        this.control({
            'overview-of-alarms button[action=applyAll]': {
                click: this.updateSections
            },
            'overview-of-alarms button[action=clearAll]': {
                click: this.clearAllFilters
            }
        });
    },

    showAlarmsOverview: function () {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        if (_.isEmpty(queryString)) {
            queryString.status = ['status.open', 'status.in.progress'];
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
        } else {
            me.getApplication().fireEvent('changecontentevent', Ext.widget('overview-of-alarms', {
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
                    model: 'Dal.model.Group'
                });
                sectionPanel.store.getProxy().pageParam = false;
                sectionPanel.store.getProxy().startParam = false;
                sectionPanel.store.getProxy().limitParam = false;
            }
            sectionPanel.store.load({
                params: me.getGroupProxyParams(section),
                callback: function () {
                    me.getOverview().down('#overview-no-alarms-found-panel').setVisible(!this.getCount());
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
