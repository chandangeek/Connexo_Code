Ext.define('Mdc.controller.setup.DeviceChannelOverview', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.controller.setup.DeviceChannelData'
    ],

    views: [
        'Mdc.view.setup.devicechannels.Overview',
        'Mdc.view.setup.devicechannels.TabbedDeviceChannelsView'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice'
    ],

    refs: [
        {ref: 'calculatedReadingType', selector: '#calculatedReadingType'},
        {ref: 'readingType', selector: '#readingType'}
    ],

    showOverview: function (mRID, channelId, tabController) {
        var me = this,
            channelModel = me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice'),
            router = me.getController('Uni.controller.history.Router'),
            widget,
            tabWidget,
            defer = {
                param: null,
                callback: null,
                resolve: function (arg) {
                    arg && this.callback.apply(this, this.param)
                },
                setCallback: function (fn) {
                    this.callback = fn;
                    this.resolve(this.param)
                },
                setParam: function () {
                    this.param = arguments;
                    this.resolve(this.callback)
                }
            };

        defer.setCallback(function (device) {

            tabWidget = Ext.widget('tabbedDeviceChannelsView', {
                router: router,
                device: device,
                channelsListLink: me.getController('Mdc.controller.setup.DeviceChannelData').makeLinkToList(router)
            });

            widget = Ext.widget('deviceLoadProfileChannelOverview', {
                router: me.getController('Uni.controller.history.Router'),
                device: device
            });

            tabWidget.down('#channel-specifications').add(widget);
            tabController.showTab(0);
            me.getApplication().fireEvent('changecontentevent', tabWidget);
            widget.setLoading(true);
            channelModel.getProxy().setUrl({
                mRID: mRID
            });
            channelModel.load(channelId, {
                success: function (record) {
                    if (!widget.isDestroyed) {
                        me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', record);
                        var readingType = record.get('readingType');
                        tabWidget.down('#channelTabPanel').setTitle(readingType.aliasName + (!Ext.isEmpty(readingType.names.unitOfMeasure) ? (' (' + readingType.names.unitOfMeasure + ')') : ''));
                        widget.down('#deviceLoadProfileChannelsOverviewForm').loadRecord(record);
                        widget.down('deviceLoadProfileChannelsActionMenu').record = record;

                        var calculatedReadingType = widget.down('#calculatedReadingType'),
                            readingTypeLabel = widget.down('#readingType').labelEl;

                        if (record.data.calculatedReadingType) {
                            readingTypeLabel.update(Uni.I18n.translate('deviceloadprofiles.channels.readingTypeForBulk', 'MDC', 'Collected reading type'));
                            calculatedReadingType.show();
                        } else {
                            readingTypeLabel.update(Uni.I18n.translate('deviceloadprofiles.channels.readingType', 'MDC', 'Reading type'));
                            calculatedReadingType.hide();
                        }
                    }
                },
                callback: function () {
                    widget.setLoading(false);
                }
            });
        });


        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                defer.setParam(record)
            }
        });

    }
});