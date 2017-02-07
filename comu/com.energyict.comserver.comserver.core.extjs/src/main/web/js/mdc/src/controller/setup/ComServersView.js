/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.ComServersView', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.model.ComServer'
    ],

    views: [
        'Mdc.view.setup.comserver.ComServersSetup',
        'Mdc.store.LogLevels',
        'Mdc.store.TimeUnitsWithoutMilliseconds'
    ],

    stores: [
        'ComServers'
    ],

    refs: [
        {
            ref: 'comServersView',
            selector: 'comServersSetup'
        },
        {
            ref: 'comServerGrid',
            selector: 'comServersSetup comServersGrid'
        },
        {
            ref: 'comServerPreview',
            selector: 'comServersSetup comServerPreview'
        },
        {
            ref: 'previewActionMenu',
            selector: 'comServerPreview #comserverViewMenu'
        }
    ],

    init: function () {
        var me = this;
        this.control({
            'comServersGrid': {
                select: this.showComServerPreview
            },
            '#comserverViewMenu': {
                click: this.chooseAction,
                show: this.configureMenu
            }
        });
    },

    configureMenu: function (menu) {
        var activate = menu.down('#activate'),
            deactivate = menu.down('#deactivate'),
            active = menu.record.data.active;

        if (active) {
            deactivate.show();
            activate.hide();
        } else {
            activate.show();
            deactivate.hide();
        }

    },

    chooseAction: function (menu, item) {
        var me = this,
            grid = me.getComServerGrid(),
            activeChange = 'notChanged',
            record,
            form,
            gridView;


        if (grid) {
            gridView = grid.getView();
            record = gridView.getSelectionModel().getLastSelected();
            form = this.getComServerPreview().down('form');
        } else {
            record = menu.record;
            form = this.getComServerOverviewForm();
        }


        switch (item.action) {
            case 'edit':
                me.editComServer(record);
                break;
            case 'remove':
                me.showDeleteConfirmation(record);
                break;
            case 'activate':
                activeChange = true;
                break;
            case 'deactivate':
                activeChange = false;
                break;
            case 'monitor':
                me.openComserverMonitor(record);
                break;
        }

        if (activeChange != 'notChanged') {
            var recordId = record.get('id');

            record.set('active', activeChange);
            Ext.Ajax.request({
                url: '/api/mdc/comservers/' + recordId + '/status',
                method: 'PUT',
                jsonData: record.getRecordData(),
                isNotEdit: true,
                success: function (response) {
                    var data = Ext.decode(response.responseText);

                    record.set(data);
                    record.commit();
                    form.loadRecord(record);
                    if (grid) {
                        me.getPreviewActionMenu().record = record;
                    } else {
                        menu.record = record;
                    }
                    me.getApplication().fireEvent('acknowledge', activeChange
                        ? Uni.I18n.translate('general.comServer.msg.activated', 'MDC', 'Communication server activated')
                        : Uni.I18n.translate('general.comServer.msg.deactivated', 'MDC', 'Communication server deactivated')
                    );
                },
                failure: function(response, opts) {
                    var json = Ext.decode(response.responseText);

                    record.reject();
                    if (json && json.errors) {
                        var msg = '';
                        Ext.each(json.errors, function (error) {
                            msg += error["id"] + ': ' + error["msg"];
                        });
                        var title;
                        switch (item.action) {
                            case 'edit':
                                title = Uni.I18n.translate('comserver.changeState.edit.failed', 'MDC', 'Editing failed');
                                break;
                            case 'remove':
                                title = Uni.I18n.translate('comserver.changeState.remove.failed', 'MDC', 'Removal failed');
                                break;
                            case 'activate':
                                title = Uni.I18n.translate('comserver.changeState.activate.failed', 'MDC', 'Activation failed');
                                break;
                            case 'deactivate':
                                title = Uni.I18n.translate('comserver.changeState.deactivate.failed', 'MDC', 'Deactivation failed');
                                break;
                        }
                        me.getApplication().getController('Uni.controller.Error').showError(title, msg);
                    }
                }
            });
        }
    },

    showComServerPreview: function (selectionModel, record) {
        var me = this,
            itemPanel = this.getComServerPreview(),
            form = itemPanel.down('form'),
            model = me.getModel('Mdc.model.ComServer'),
            menu =  form.up('panel').down('menu'),
            id = record.getId();

        itemPanel.setLoading();

        model.load(id, {
            success: function (record) {
                if (!form.isDestroyed) {
                    me.fillPreviewForm(form, record);
                    if (menu) {
                        menu.record = record;
                    }
                    itemPanel.setLoading(false);
                    itemPanel.setTitle(record.get('name'));
                }
            }
        });
    },

    fillPreviewForm: function(form, record) {
        var loglevelsStore = this.getStore('Mdc.store.LogLevels'),
            timeUnitsStore = this.getStore('Mdc.store.TimeUnitsWithoutMilliseconds'),
            serverLogLevel = loglevelsStore.findRecord('logLevel', record.get('serverLogLevel')).get('localizedValue'),
            communicationLogLevel = loglevelsStore.findRecord('logLevel', record.get('communicationLogLevel')).get('localizedValue'),
            changesInterPollDelayUnit = timeUnitsStore.findRecord('timeUnit', record.get('changesInterPollDelay').timeUnit).get('localizedValue'),
            schedulingInterPollDelayUnit = timeUnitsStore.findRecord('timeUnit', record.get('schedulingInterPollDelay').timeUnit).get('localizedValue'),
            changesInterPollDelay = {count: record.get('changesInterPollDelay').count, timeUnit: changesInterPollDelayUnit},
            schedulingInterPollDelay = {count: record.get('schedulingInterPollDelay').count , timeUnit: schedulingInterPollDelayUnit};

        record.set('serverLogLevel', serverLogLevel);
        record.set('communicationLogLevel', communicationLogLevel);
        record.set('changesInterPollDelay', changesInterPollDelay);
        record.set('schedulingInterPollDelay', schedulingInterPollDelay);
        form.loadRecord(record);
    },

    editComServer: function (record) {
        var router = this.getController('Uni.controller.history.Router'),
            id = record.getId();

        router.getRoute('administration/comservers/edit').forward({id: id});
    },

    showDeleteConfirmation: function (record) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('comServer.deleteConfirmation.msg', 'MDC', 'This communication server will no longer be available.'),
            title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', [record.get('name')]),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.deleteComserver(record);
                        break;
                    case 'cancel':
                        break;
                }
            }
        });
    },

    openComserverMonitor: function (record) {
        var monitorUri = 'http://' + record.get('serverName') + ':' + record.get('statusPort') + '/apps/comservermonitor/index.html';
        window.open(monitorUri);
    },

    deleteComserver: function (record) {
        var me = this,
            page = me.getComServersView(),
            gridToolbarTop = me.getComServerGrid().down('pagingtoolbartop');

        page.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
        record.destroy({
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute().forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comServer.deleteSuccess.msg', 'MDC', 'Communication server removed'));
            },
            callback: function (model, operation) {
                page.setLoading(false);
            }
        });
    }
});
