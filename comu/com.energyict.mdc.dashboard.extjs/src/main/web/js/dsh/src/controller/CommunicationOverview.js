Ext.define('Dsh.controller.CommunicationOverview', {
    extend: 'Ext.app.Controller',
    requires:[
        'Yfn.privileges.Yellowfin'
    ],
    models: [
        'Dsh.model.communication.Overview'
    ],
    stores: [
        'CommunicationServerInfos'
    ],
    views: [
        'Dsh.view.CommunicationOverview'
    ],
    refs: [
        { ref: 'communicationOverview', selector: '#communication-overview' },
        { ref: 'header', selector: '#header-section' },
        { ref: 'summary', selector: '#summary' },
        { ref: 'communicationServers', selector: '#communication-servers' },
        { ref: 'overview', selector: '#overview' },
        { ref: 'breakdown', selector: '#breakdown' },
        { ref: 'kpi', selector: '#communication-overview read-outs-over-time' },
        { ref: 'quickLinks', selector: '#communication-overview #quick-links' },
        { ref: 'heatmap', selector: '#communication-overview #heatmap' }
    ],

    init: function () {
        this.control({
            '#communication-overview #refresh-btn': {
                click: this.loadData
            },
            '#communication-overview #device-group': {
                change: this.updateQuickLinks
            }
        });
    },

    showOverview: function () {
        var me = this;
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('communication-overview', {router: router}));
        this.loadData();
    },

    loadData: function () {
        var me = this,
            model = me.getModel('Dsh.model.communication.Overview'),
            router = this.getController('Uni.controller.history.Router');

        model.setFilter(router.filter);
        me.getCommunicationOverview().setLoading();
        me.getCommunicationServers().reload();
        me.getHeatmap().reload();
        model.load(null, {
                success: function (record) {
                    me.getSummary().setRecord(record.getSummary());
                    me.getOverview().bindStore(record.overviews());
                    me.getBreakdown().bindStore(record.breakdowns());
                    if (record.raw.kpi) {
                        me.getKpi().setRecord(record.getKpi());
                    } else {
                        me.getKpi().setRecord(null); // when it is group without kpi defined
                    }
                    me.getHeader().down('#last-updated-field').setValue('Last updated at ' + Uni.DateTime.formatTimeShort(new Date()));


                },
                callback: function () {
                    me.getCommunicationOverview().setLoading(false);
                }
            }
        );
        },

    updateQuickLinks: function(){
        if (Yfn.privileges.Yellowfin.canView()) {
            var me = this;
            var deviceGroupField = me.getHeader().down('#device-group');
            var deviceGroupName = deviceGroupField.groupName;
            var filter = false;
            if (deviceGroupName && deviceGroupName.length) {
                filter = encodeURIComponent(Ext.JSON.encode({
                    'GROUPNAME': deviceGroupName
                }))
            }
            var reportsStore = Ext.getStore('ReportInfos');
            if (reportsStore) {
                var proxy = reportsStore.getProxy();
                proxy.setExtraParam('category', 'MDC');
                proxy.setExtraParam('subCategory', 'Device Communication');

                reportsStore.load(function (records) {
                    var quickLinks = Ext.isArray(me.getQuickLinks().data) ? me.getQuickLinks().data : [];
                    Ext.each(records, function (record) {
                        var reportName = record.get('name');
                        var reportUUID = record.get('reportUUID');

                        quickLinks.push({
                            link: reportName,
                            href: '#/administration/generatereport?reportUUID=' + reportUUID +'&subCategory=Device%20Communication' +(filter ? '&filter=' + filter : '')
                        });
                    });

                    var quicklinksTplPanel = me.getQuickLinks().down('#quicklinksTplPanel');
                    quicklinksTplPanel.update(quickLinks);
                });
            }
        }
    }
});