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
        'Imt.purpose.model.Reading'
    ],

    views: [
        'Imt.purpose.view.Outputs',
        'Imt.purpose.view.OutputChannelMain'
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
                me.loadOutputs(mRID, purposeId, function() {
                    var purpose = _.find(purposes, function(p){return p.getId() == purposeId});
                    app.fireEvent('changecontentevent', Ext.widget('purpose-outputs', {
                        itemId: 'purpose-outputs',
                        router: router,
                        usagePoint: usagePoint,
                        purposes: purposes,
                        purpose: purpose
                    }));
                    mainView.setLoading(false);
                });
            },
            failure: function () {
                mainView.setLoading(false);
            }
        });
    },

    makeLinkToOutputs: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('general.channels', 'MDC', 'Channels').toLowerCase() + '</a>';
            //filter = this.getStore('Mdc.store.Clipboard').get('latest-device-channels-filter'),
            //queryParams = filter ? {filter: filter} : null;

        return Ext.String.format(link, router.getRoute('usagepoints/view/purpose').buildUrl());
    },

    showOutputDefaultTab: function(mRID, purposeId, outputId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            prevNextListLink = me.makeLinkToOutputs(router);

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
                                prevNextListLink: prevNextListLink,
                                controller: me
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
    },

    showSpecificationsTab: function(panel) {

    },

    showReadingsTab: function(panel) {
        var me = this,
            output = panel.output,
            intervalStore = me.getStore('Uni.store.DataIntervalAndZoomLevels'),
            readingsStore = me.getStore('Imt.purpose.store.Readings'),
            interval = intervalStore.getIntervalRecord(output.get('interval')),
            filter;


        filter = {};
        filter.intervalEnd = output.get('lastReading') || new Date();
        filter.intervalStart = interval.getIntervalStart(filter.intervalEnd);

        debugger;

        readingsStore.getProxy().extraParams = {
            mRID: panel.usagePoint.get('mRID'),
            purposeId: panel.purpose.getId(),
            outputId: panel.output.getId(),
            filter: filter
        };

        readingsStore.load();
    }

    //
    //    var me = this,
    //        mRID = params['mRID'],
    //        channelId = params['channelId'],
    //        issueId = params['issueId'],
    //        device = me.getModel('Mdc.model.Device'),
    //        viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
    //        channel = me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice'),
    //        router = me.getController('Uni.controller.history.Router'),
    //        prevNextstore = contentName === 'block' ? 'Mdc.store.ValidationBlocks' : 'Mdc.store.ChannelsOfLoadProfilesOfDevice',
    //        prevNextListLink = contentName === 'block' ? me.makeLinkToIssue(router, issueId) : me.makeLinkToChannels(router),
    //        indexLocation = contentName === 'block' ? 'queryParams' : 'arguments',
    //        routerIdArgument = contentName === 'block' ? 'validationBlock' : 'channelId',
    //        isFullTotalCount = contentName === 'block',
    //        activeTab = contentName === 'spec' ? 0 : 1,
    //        timeUnitsStore = Ext.getStore('Mdc.store.TimeUnits'),
    //        loadDevice = function() {
    //            device.load(mRID, {
    //                scope: me,
    //                success: onDeviceLoad
    //            });
    //        },
    //        onDeviceLoad = function(device) {
    //            me.getApplication().fireEvent('loadDevice', device);
    //            channel.getProxy().setUrl({
    //                mRID: mRID
    //            });
    //            channel.load(channelId, {
    //                success: function (channel) {
    //                    me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', channel);
    //                    var widget = Ext.widget('tabbedDeviceChannelsView', {
    //                        title: channel.get('readingType').fullAliasName,
    //                        router: router,
    //                        channel: channel,
    //                        device: device,
    //                        contentName: contentName,
    //                        indexLocation: indexLocation,
    //                        prevNextListLink: prevNextListLink,
    //                        activeTab: activeTab,
    //                        prevNextstore: prevNextstore,
    //                        routerIdArgument: routerIdArgument,
    //                        isFullTotalCount: isFullTotalCount,
    //                        filterDefault: activeTab === 1 ? me.setDataFilter(channel, contentName, router) : {}
    //                    });
    //
    //                    me.getApplication().fireEvent('changecontentevent', widget);
    //                    viewport.setLoading(false);
    //                    if (activeTab == 1) {
    //                        me.setupReadingsTab(device, channel, widget);
    //                    } else if (activeTab == 0) {
    //                        me.setupSpecificationsTab(device, channel, widget);
    //                    }
    //                }
    //            });
    //        };
    //
    //    viewport.setLoading(true);
    //    if (contentName === 'spec') {
    //        timeUnitsStore.load(function() {
    //            loadDevice();
    //        });
    //    } else {
    //        loadDevice();
    //    }
    //}
});