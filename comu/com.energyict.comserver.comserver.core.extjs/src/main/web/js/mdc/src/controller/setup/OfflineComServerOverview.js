/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.OfflineComServerOverview', {
    extend: 'Mdc.controller.setup.OfflineComServersView',

    models: [
        'Mdc.model.ComServer'
    ],

    views: [
        'Mdc.view.setup.comserver.OfflineComServerOverview'
    ],

    stores: [
        'Mdc.store.LogLevels',
        'Mdc.store.TimeUnitsWithoutMilliseconds',
        'ComServers',
        'OfflineComServers'
    ],

    refs: [
        {
            ref: 'offlineComServerOverview',
            selector: 'offlineComServerOverview'
        },
        {
            selector: '#offlineComServerOverviewForm',
            ref: 'offlineComServerOverviewForm'
        },
        {
            ref: 'offlineComServerGrid',
            selector: 'offlineComServersSetup offlineComServersGrid'
        }
    ],

    init: function () {
        this.control({
            '#offlineComServerOverviewMenu': {
                show: this.configureMenu,
                click: this.chooseAction
            }
        })
    },

    editComServer: function (record) {
        var router = this.getController('Uni.controller.history.Router'),
            id = record.getId();

        router.getRoute('administration/offlinecomservers/detail/edit').forward({id: id});
    },

    showOverview: function (id) {
        var me = this,
            model = this.getModel('Mdc.model.ComServer'),
            comServerStore = this.getStore('Mdc.store.ComServers'),
            logLevelsStore = me.getStore('Mdc.store.LogLevels'),
            timeUnitsStore = me.getStore('Mdc.store.TimeUnitsWithoutMilliseconds'),
            widget = Ext.widget('offlineComServerOverview', {
                serverId: id
            }),
            counter = 0,
            callback;

        widget.setLoading(true);
        callback = function() {
            counter += 1;
            if (counter === 2) {
                model.load(id, {
                    success: function (record) {
                        var form = widget.down('form'),
                            onlineComServerName = comServerStore.findRecord('id', record.get('onlineComServerId')).get('name'),
                            serverLogLevel = logLevelsStore.findRecord('logLevel', record.get('serverLogLevel')).get('localizedValue'),
                            communicationLogLevel = logLevelsStore.findRecord('logLevel', record.get('communicationLogLevel')).get('localizedValue'),
                            changesInterPollDelayUnit = timeUnitsStore.findRecord('timeUnit', record.get('changesInterPollDelay').timeUnit).get('localizedValue'),
                            schedulingInterPollDelayUnit = timeUnitsStore.findRecord('timeUnit', record.get('schedulingInterPollDelay').timeUnit).get('localizedValue'),
                            changesInterPollDelay = {count: record.get('changesInterPollDelay').count, timeUnit: changesInterPollDelayUnit},
                            schedulingInterPollDelay = {count: record.get('schedulingInterPollDelay').count , timeUnit: schedulingInterPollDelayUnit};
                        record.set('onlineComServerName', onlineComServerName);
                        record.set('serverLogLevel', serverLogLevel);
                        record.set('communicationLogLevel', communicationLogLevel);
                        record.set('changesInterPollDelay', changesInterPollDelay);
                        record.set('schedulingInterPollDelay', schedulingInterPollDelay);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        form.loadRecord(record);
                        form.up('container').down('container').down('button').menu.record = record;
                        widget.down('comserversidemenu').setHeader(record.get('name'));
                        me.getApplication().fireEvent('comServerOverviewLoad', record);
                    },
                    callback: function () {
                        widget.setLoading(false);
                    }
                });
            }
        };
        comServerStore.load(callback);
        logLevelsStore.load(callback);
        timeUnitsStore.load(callback);
    },

    deleteComserver: function (record) {
        var me = this,
            page = me.getComServerOverview();
        page.setLoading('Removing...');
        record.destroy({
            callback: function (model, operation) {
                page.setLoading(false);
                if (operation.response.status == 204) {
                    var router = me.getController('Uni.controller.history.Router');
                    router.getRoute('administration/offlinecomservers').forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comServer.deleteSuccess.msg', 'MDC', 'Communication server removed'));
                }
            }
        });
    }
});
