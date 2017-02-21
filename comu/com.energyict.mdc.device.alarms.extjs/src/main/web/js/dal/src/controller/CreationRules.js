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
        var itemPanel = this.getItemPanel(),
            form = itemPanel.down('form'),
            menu = itemPanel.down('menu');

        Ext.suspendLayouts();
        form.loadRecord(record);
        itemPanel.setTitle(Ext.String.htmlEncode(record.get('title')));
        if (menu) {
            menu.record = record;
        }
        Ext.resumeLayouts(true);
    },

    chooseAction: function (menu, item) {
        var action = item.action;
        var id = menu.record.getId();
        var router = this.getController('Uni.controller.history.Router');

        switch (action) {
            case 'remove':
                this.showDeleteConfirmation(menu.record);
                break;
            case 'edit':
                router.getRoute('administration/alarmcreationrules/edit').forward({id: id});
                break;
        }
    },

    createRule: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/alarmcreationrules/add').forward();
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