Ext.define('Imt.purpose.controller.Purpose', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router',
        'Imt.purpose.view.Outputs',
        'Imt.purpose.store.Outputs',
        'Imt.purpose.view.OutputChannelMain',
        'Uni.store.DataIntervalAndZoomLevels'
    ],

    stores: [
        'Imt.purpose.store.Outputs',
        'Imt.purpose.store.Readings',
        'Uni.store.DataIntervalAndZoomLevels'
    ],

    models: [
        'Imt.purpose.model.Output',
        'Imt.purpose.model.Reading',
        'Imt.usagepointmanagement.model.ValidationInfo',
        'Imt.usagepointmanagement.model.SuspectReason'
    ],

    views: [
        'Imt.purpose.view.Outputs',
        'Imt.purpose.view.OutputChannelMain',
        'Imt.purpose.view.ValidationStatusForm'
    ],

    loadOutputs: function (mRID, purposeId, callback) {
        var me = this,
            outputsStore = me.getStore('Imt.purpose.store.Outputs');

        outputsStore.getProxy().extraParams = {mRID: mRID, purposeId: purposeId};
        outputsStore.load(callback);
    },

    showOutputs: function (mRID, purposeId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];


        mainView.setLoading();
        usagePointsController.loadUsagePoint(mRID, {
            success: function (types, usagePoint, purposes) {
                var purpose = _.find(purposes, function (p) {
                        return p.getId() == purposeId
                    }),
                    widget = Ext.widget('purpose-outputs', {
                        itemId: 'purpose-outputs',
                        router: router,
                        usagePoint: usagePoint,
                        purposes: purposes,
                        purpose: purpose
                    });
                widget.down('#purpose-details-form').loadRecord(purpose);
                debugger;
                widget.down('#purpose-details-form #output-validation-status-form').loadRecord(purpose.getValidationInfo());
                app.fireEvent('changecontentevent', widget);
                mainView.setLoading(false);
                me.loadOutputs(mRID, purposeId);
            },
            failure: function () {
                mainView.setLoading(false);
            }
        });
    },

    makeLinkToOutputs: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('general.outputs', 'IMT', 'Outputs').toLowerCase() + '</a>';
        return Ext.String.format(link, router.getRoute('usagepoints/view/purpose').buildUrl());
    },

    showOutputDefaultTab: function(mRID, purposeId, outputId, tab) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            prevNextListLink = me.makeLinkToOutputs(router);

        if (!tab) {
            router.getRoute('usagepoints/view/purpose/output').forward({tab: 'readings'});
        } else {
            mainView.setLoading();
            usagePointsController.loadUsagePoint(mRID, {
                success: function (types, usagePoint, purposes) {
                    me.loadOutputs(mRID, purposeId, function (outputs) {
                        app.fireEvent('outputs-loaded', outputs);
                        var output = _.find(outputs, function (o) {
                            return o.getId() == outputId
                        });

                        //me.getModel('Imt.purpose.model.Output').load(outputId, {
                        //    success: function (output) {
                        var purpose = _.find(purposes, function (p) {
                            return p.getId() == purposeId
                        });
                        app.fireEvent('output-loaded', output);
                        var widget = Ext.widget('output-channel-main', {
                            itemId: 'output-channel-main',
                            router: router,
                            usagePoint: usagePoint,
                            purposes: purposes,
                            purpose: purpose,
                            outputs: outputs,
                            output: output,
                            interval: me.getInterval(output),
                            prevNextListLink: prevNextListLink,
                            controller: me,
                            tab: tab
                        });
                        app.fireEvent('changecontentevent', widget);
                        mainView.setLoading(false);
                        widget.down('output-specifications-form').loadRecord(output);

                        //},
                        //failure: function () {
                        //    mainView.setLoading(false);
                        //}
                        //});
                    });
                },
                failure: function () {
                    mainView.setLoading(false);
                }
            });
        }
    },

    getInterval: function(output) {
        var me = this,
            intervalStore = me.getStore('Uni.store.DataIntervalAndZoomLevels');

        return intervalStore.getIntervalRecord(output.get('interval'));
    },

    showSpecificationsTab: function(panel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        Uni.util.History.suspendEventsForNextCall();
        Uni.util.History.setParsePath(false);
        router.arguments.tab = 'specifications';
        router.getRoute('usagepoints/view/purpose/output').forward();
    },

    showReadingsTab: function(panel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            output = panel.output,
            readingsStore = me.getStore('Imt.purpose.store.Readings');


        Uni.util.History.suspendEventsForNextCall();
        Uni.util.History.setParsePath(false);
        router.arguments.tab = 'readings';
        router.getRoute('usagepoints/view/purpose/output').forward();

        readingsStore.getProxy().extraParams = {
            mRID: panel.usagePoint.get('mRID'),
            purposeId: panel.purpose.getId(),
            outputId: panel.output.getId()
        };

        readingsStore.load();
    }
});