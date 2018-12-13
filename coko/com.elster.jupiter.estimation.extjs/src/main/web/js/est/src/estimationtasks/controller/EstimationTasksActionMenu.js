/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.controller.EstimationTasksActionMenu', {
    extend: 'Ext.app.Controller',
    views: [
        'Uni.form.field.DateTime',
        'Est.estimationtasks.view.Details',
        'Est.estimationtasks.view.Overview',
        'Est.estimationtasks.view.History'
    ],
    stores: [
        'Est.estimationtasks.store.EstimationTasks'
    ],
    models: [
        'Est.estimationtasks.model.EstimationTask'
    ],
    refs: [
        {ref: 'page', selector: 'estimationtasks-overview'},
        {ref: 'detailsPage', selector: 'estimationtasks-details'},
        {ref: 'historyPage', selector: 'estimationtasks-history'},
        {ref: 'history', selector: 'estimationtasks-history'}
    ],

    init: function () {
        this.control({
            'estimationtasks-action-menu': {
                click: this.chooseAction
            },
            'estimationtasks-history-action-menu': {
                click: this.chooseAction
            }
        });
    },

    viewLogRoute: 'administration/estimationtasks/estimationtask/history/occurrence',
    chooseAction: function (menu, item) {
        var me = this, router = me.getController('Uni.controller.history.Router'), route;

        if (me.getHistory()) {
            router.arguments.occurrenceId = menu.record.getId();
        } else {
            router.arguments.taskId = menu.record.getId();
        }

        switch (item.action) {
            case 'removeEstimationTask':
                me.removeEstimationTask(menu.record);
                break;
            case 'runEstimationTask':
                me.runTask(menu.record);
                break;
            case 'viewEstimationTaskHistory':
                route = 'administration/estimationtasks/estimationtask/history';
                break;
            case 'viewLog':
                route = me.viewLogRoute;
                break;
            case 'editEstimationTask':
                route = 'administration/estimationtasks/estimationtask/edit';
                break;
        }

        if(route) router.getRoute(route).forward();

    },

    runTask: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('estimationtasks.general.run', 'EST', 'Run'),
                confirmation: function () {
                    me.submitRunTask(record, this);
                }
            });

        confirmationWindow.insert(1,
            {
                xtype: 'panel',
                itemId: 'date-errors',
                hidden: true,
                bodyStyle: {
                    color: '#eb5642',
                    padding: '0 0 15px 65px'
                },
                html: ''
            }
        );

        confirmationWindow.show({
            msg: Uni.I18n.translate('estimationtasks.general.runmsg', 'EST', 'The estimation task will be queued to run at the earliest possible time.'),
            title: Uni.I18n.translate('estimationtasks.general.runestimationtask', 'EST', "Run estimation task '{0}'?", [record.get('name')])
        });
    },

    submitRunTask: function (record, confWindow) {
        var me = this,
            id = record.get('id'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            router = me.getController('Uni.controller.history.Router');

        pageMainContent.setLoading(true);
        Ext.Ajax.request({
            url: '/api/est/estimation/tasks/' + id + '/trigger',
            method: 'PUT',
            jsonData: record.getRecordData(),
            isNotEdit: true,
            success: function () {
                confWindow.destroy();
                router.getRoute().forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationtasks.runQueued', 'EST', 'Estimation task run queued'));
            },
            failure: function (response) {
                var res = Ext.JSON.decode(response.responseText, true);
                if (response.status === 400) {
                    confWindow.update(res.errors[0].msg);
                    confWindow.setVisible(true);
                } else {
                    confWindow.destroy();
                }
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
    },

    removeEstimationTask: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');
        confirmationWindow.show({
            msg: Uni.I18n.translate('estimationtasks.general.remove.msg', 'EST', 'This estimation task will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'EST', "Remove '{0}'?",[ record.get('name')]),
            config: {},
            fn: function (state) {
                if (state === 'confirm') {
                    me.removeOperation(record);
                } else if (state === 'cancel') {
                    this.close();
                }
            }
        });
    },

    removeOperation: function (record) {
        var me = this,
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        pageMainContent.setLoading(true);
        record.destroy({
            success: function () {
                if (me.getPage()) {
                    var grid = me.getPage().down('estimationtasks-grid');
                    grid.down('pagingtoolbartop').totalCount = 0;
                    grid.down('pagingtoolbarbottom').resetPaging();
                    grid.getStore().load();
                } else {
                    me.getController('Uni.controller.history.Router').getRoute('administration/estimationtasks').forward();
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('estimationtasks.general.remove.confirm.msg', 'EST', 'Estimation task removed'));
            },
            failure: function (object, operation) {
                if (operation.response.status === 409) {
                    return
                }
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
    }
});