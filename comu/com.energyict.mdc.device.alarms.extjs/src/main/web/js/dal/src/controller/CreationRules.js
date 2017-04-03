/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.controller.CreationRules', {
    extend: 'Ext.app.Controller',

    stores: [
        'Dal.store.CreationRules'
    ],

    views: [
        'Dal.view.creationrules.Overview'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'alarm-creation-rules-overview'
        },
        {
            ref: 'itemPanel',
            selector: 'alarm-creation-rules-overview alarm-creation-rules-item'
        },
        {
            ref: 'rulesGridPagingToolbarTop',
            selector: 'alarm-creation-rules-overview alarms-creation-rules-list pagingtoolbartop'
        }
    ],

    init: function () {
        this.control({
            'alarm-creation-rules-overview alarms-creation-rules-list': {
                select: this.showPreview
            },
            'alarm-creation-rule-action-menu': {
                click: this.chooseAction
            },
            'alarm-creation-rules-overview button[action=create]': {
                click: this.createRule
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('alarm-creation-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            itemPanel = this.getItemPanel(),
            form = itemPanel.down('form'),
            menu = itemPanel.down('menu');

        Ext.suspendLayouts();
        me.setupMenuItems(record);
        form.loadRecord(record);
        itemPanel.setTitle(Ext.String.htmlEncode(record.get('title')));
        if (menu) {
            menu.record = record;
        }
        Ext.resumeLayouts(true);
    },

    chooseAction: function (menu, item) {
        var me = this,
            action = item.action,
            id = menu.record.getId(),
            router = this.getController('Uni.controller.history.Router');

        switch (action) {
            case 'remove':
                this.showDeleteConfirmation(menu.record);
                break;
            case 'edit':
                router.getRoute('administration/alarmcreationrules/edit').forward({id: id});
                break;
            case 'activate':
                router.setState(router.getRoute()); // store the current url
                me.activateRule(menu.record);
                break;
        }
    },

    setupMenuItems: function (record) {
        var suspended = record.get('active'),
            menuText = suspended
                ? Uni.I18n.translate('administration.alarmCreationRules.deactivate', 'DAL', 'Deactivate')
                : Uni.I18n.translate('administration.alarmCreationRules.activate', 'DAL', 'Activate'),
            menuItems = Ext.ComponentQuery.query('menu menuitem[action=activate]');
        if (!Ext.isEmpty(menuItems)) {
            Ext.Array.each(menuItems, function (item) {
                item.setText(menuText);
            });
        }
    },

    createRule: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/alarmcreationrules/add').forward();
    },

    activateRule: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            suspended = record.data.active;

        var action = ((suspended == true) ? 'deactivate' : 'activate');

        Ext.Ajax.request({
            url: '/api/dal/creationrules/' + record.data.id + '/' + action,
            method: 'PUT',
            jsonData: Ext.encode(record.raw),
            success: function () {
                var messageText = suspended
                    ? Uni.I18n.translate('administration.alarmCreationRules.deactivateSuccessMsg', 'DAL', 'Alarm creation rule deactivated')
                    : Uni.I18n.translate('administration.alarmCreationRules.activateSuccessMsg', 'DAL', 'Alarm creation rule activated');
                me.getApplication().fireEvent('acknowledge', messageText);
                router.getState().forward(); // navigate to the previously stored url
            },
            failure: function (response) {
                if (response.status == 400) {
                    var errorText = Uni.I18n.translate('administration.alarmCreationRules.error.unknown', 'DAL', 'Unknown error occurred');
                    if (!Ext.isEmpty(response.statusText)) {
                        errorText = response.statusText;
                    }
                    if (!Ext.isEmpty(response.responseText)) {
                        var json = Ext.decode(response.responseText, true);
                        if (json && json.error) {
                            errorText = json.error;
                        }
                    }
                    var titleText = suspended
                        ? Uni.I18n.translate('administration.alarmCreationRules.deactivate.operation.failed', 'DAL', 'Deactivate operation failed')
                        : Uni.I18n.translate('administration.alarmCreationRules.activate.operation.failed', 'DAL', 'Activate operation failed');

                    me.getApplication().getController('Uni.controller.Error').showError(titleText, errorText);
                }
            }
        });
    },

    showDeleteConfirmation: function (rule) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('administration.alarmCreationRules.deleteConfirmation.message', 'DAL', 'This alarm creation rule will disappear from the list. Alarms will not be created automatically by this rule.'),
            title: Uni.I18n.translate('administration.alarmCreationRules.deleteConfirmation.title', 'DAL', "Remove rule '{0}'?", [rule.get('name')]),
            config: {
                me: me,
                rule: rule
            },
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.deleteRule(rule);
                        break;
                    case 'cancel':
                        break;
                }
            }
        });
    },

    deleteRule: function (rule) {
        var me = this,
            store = this.getStore('Dal.store.CreationRules'),
            page = this.getPage();

        page.setLoading('Removing...');
        rule.destroy({
            success: function () {
                page.down('#creation-rules-list pagingtoolbartop').totalCount = 0;
                page.down('#creation-rules-list pagingtoolbarbottom').resetPaging();
                store.loadPage(1);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('administration.alarmCreationRules.deleteSuccess.msg', 'DAL', 'Alarm creation rule removed'));
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    }
});