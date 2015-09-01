Ext.define('Dsh.controller.ConnectionOverview', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.connection.Overview'
    ],
    stores: [
        'CommunicationServerInfos',
        'Dsh.store.CombineStore',
        'Dsh.store.ConnectionResultsStore'
    ],
    views: [ 'Dsh.view.ConnectionOverview' ],
    refs: [
        { ref: 'connectionOverview', selector: '#connection-overview' },
        { ref: 'header', selector: '#header-section' },
        { ref: 'summary', selector: '#summary' },
        { ref: 'communicationServers', selector: '#communication-servers' },
        { ref: 'overview', selector: '#overview' },
        { ref: 'breakdown', selector: '#breakdown' },
        { ref: 'kpi', selector: '#connection-overview read-outs-over-time' },
        { ref: 'quickLinks', selector: '#connection-overview #quick-links' },
        { ref: 'heatmap', selector: '#connection-overview #heatmap' }
    ],

    init: function () {
        this.control({
            '#connection-overview #refresh-btn': {
                click: this.loadData
            },
            '#connection-overview #device-group': {
                change: this.updateQuickLinks
            }
        });
    },

    showOverview: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('connection-overview', {router: router}));
        this.loadData();
    },

    loadData: function () {
        var me = this,
            model = me.getModel('Dsh.model.connection.Overview'),
            router = this.getController('Uni.controller.history.Router');

        model.setFilter(router.filter);
        me.getConnectionOverview().setLoading();
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
                    me.getHeader().down('#last-updated-field').setValue(
                        Uni.I18n.translate('general.lastUpdatedAt', 'DSH', 'Last updated at {0}', [Uni.DateTime.formatTimeShort(new Date())])
                    );
                },
                callback: function () {
                    me.getConnectionOverview().setLoading(false);
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
                proxy.setExtraParam('subCategory', 'Device Connections');
                reportsStore.load(function (records) {
                    var quickLinks = Ext.isArray(me.getQuickLinks().data) ? me.getQuickLinks().data : [];
                    Ext.each(records, function (record) {
                        var reportName = record.get('name');
                        var reportUUID = record.get('reportUUID');
                        quickLinks.push({
                            link: reportName,
                            href: '#/administration/generatereport?reportUUID=' + reportUUID +'&subCategory=Device%Connections' + (filter ? '&filter=' + filter : ''),
                            target: '_blank'
                        });
                    });

                    var quicklinksTplPanel = me.getQuickLinks().down('#quicklinksTplPanel');
                    quicklinksTplPanel.update(quickLinks);
                });
            }
        }
    }
});