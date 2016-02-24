Ext.define('Imt.usagepointmanagement.controller.Processes', {
    extend: 'Dbp.startprocess.controller.StartProcess',

    requires: [
        'Uni.controller.history.Router',
        'Imt.usagepointmanagement.view.UsagePointEdit',
        'Imt.usagepointmanagement.view.Processes.StartProcess'
    ],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.metrologyconfiguration.model.MetrologyConfiguration'
    ],

    showUsagePointStartProcess: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Imt.usagepointmanagement.model.UsagePoint').load(mRID, {
            success: function (record) {
                var widget;

                me.getApplication().fireEvent('usagePointLoaded', record);
                viewport.setLoading(false);
                widget = Ext.widget('usage-point-processes-startprocess', {
                    mRID: mRID,
                    router: router,
                    properties: {
                        activeProcessesParams: {
                            type: 'usagePoint',
                            privileges: Ext.encode(me.getPrivileges())
                        },
                        startProcessParams: [
                            {
                                name: 'type',
                                value: 'usagePoint'
                            },
                            {
                                name: 'id',
                                value: 'usagePointId'
                            },
                            {
                                name: 'value',
                                value: mRID
                            }
                        ],
                        successLink: router.getRoute('usagepoints/view/processes').buildUrl({usagePointId: mRID}),
                        cancelLink: router.getRoute('usagepoints/view').buildUrl({usagePointId: mRID})
                    }
                });
                me.getApplication().fireEvent('changecontentevent', widget);

            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    }
});