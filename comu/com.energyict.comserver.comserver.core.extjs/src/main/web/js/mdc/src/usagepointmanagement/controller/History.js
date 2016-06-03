Ext.define('Mdc.usagepointmanagement.controller.History', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.usagepointmanagement.model.UsagePoint',
        'Ext.container.Container'
    ],
    stores: [
        'Mdc.usagepointmanagement.store.MetrologyConfigurationVersions'
    ],
    views: [
        'Mdc.usagepointmanagement.view.history.Setup'
    ],
    refs: [
        {ref: 'metrologyConfigurationTab', selector: 'metrology-configuration-history-tab'},
        {ref: 'metrologyConfigurationActionMenu', selector: 'metrology-configuration-versions-action-menu'}
    ],

    init: function () {
        this.control({
            'metrology-configuration-history-tab metrology-configuration-history-grid': {
                select: this.selectVersion
            }
        });
    },

    showMetrologyConfigurationHistory: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePointModel = me.getModel('Mdc.usagepointmanagement.model.UsagePoint'),
            vesrsions = me.getStore('Mdc.usagepointmanagement.store.MetrologyConfigurationVersions'),

            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        pageMainContent.setLoading(true);

        usagePointModel.load(id, {
            success: function (record) {
                me.getApplication().fireEvent('usagePointLoaded', record);


                vesrsions.getProxy().setUrl(record.get('mRID'));
                switch (router.queryParams.historyTab) {
                    default:
                    {
                        var widget = Ext.widget('usage-point-history-setup', {router: router, mRID: record.get('mRID')});
                        var grid = widget.down('metrology-configuration-history-grid');

                        vesrsions.load({
                            callback: function () {
                                me.getApplication().fireEvent('changecontentevent', widget);
                                grid.setVersionCount(vesrsions.getCount())
                                pageMainContent.setLoading(false);
                            }
                        });
                    }
                        break;
                    case 'meterActivation':
                    {
                        //TODO: should be changed in another story
                        var widget = Ext.widget('usage-point-history-setup', {router: router, mRID: record.get('mRID')});
                        var grid = widget.down('metrology-configuration-history-grid');

                        vesrsions.load({
                            callback: function () {
                                me.getApplication().fireEvent('changecontentevent', widget);
                                grid.setVersionCount(vesrsions.getCount())
                                pageMainContent.setLoading(false);
                            }
                        });
                    }
                        break;

                }
            },
            failure: function () {
                pageMainContent.setLoading(false);
            }
        });
    },

    selectVersion: function (selectionModel, record) {
        var me = this,
            page = me.getMetrologyConfigurationTab(),
            actionMenu = me.getMetrologyConfigurationActionMenu(),
            preview = page.down('metrology-configuration-history-preview');

        Ext.suspendLayouts();
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));

        if (actionMenu) {
            actionMenu.setMenuItems(record);
            preview.down('metrology-configuration-versions-action-menu').setMenuItems(record);
        }
        
        preview.fillReadings(record);
        Ext.resumeLayouts(true);
    }
});

