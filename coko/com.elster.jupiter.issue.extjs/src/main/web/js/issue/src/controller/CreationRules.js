Ext.define('Isu.controller.CreationRules', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.CreationRules'
    ],

    views: [
        'Isu.view.creationrules.Overview'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'issue-creation-rules-overview'
        },
        {
            ref: 'itemPanel',
            selector: 'issue-creation-rules-overview issue-creation-rules-item'
        },
        {
            ref: 'rulesGridPagingToolbarTop',
            selector: 'issue-creation-rules-overview issues-creation-rules-list pagingtoolbartop'
        }
    ],

    init: function () {
        this.control({
            'issue-creation-rules-overview issues-creation-rules-list': {
                select: this.showPreview
            },
            'issues-creation-rules-list uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'creation-rule-action-menu': {
                click: this.chooseAction
            },
            'issue-creation-rules-overview button[action=create]': {
                click: this.createRule
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('issue-creation-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    showPreview: function (selectionModel, record) {
        var itemPanel = this.getItemPanel(),
            form = itemPanel.down('form');

        itemPanel.setLoading(true);

        this.getModel('Isu.model.CreationRule').load(record.getId(), {
            success: function (record) {
                if (!form.isDestroyed) {
                    form.loadRecord(record);
                    form.up('panel').down('menu').record = record;
                    itemPanel.setLoading(false);
                    itemPanel.fireEvent('afterChange',itemPanel);
                    itemPanel.setTitle(record.data.title);
                }
            }
        });
    },

    chooseAction: function (menu, item) {
        var action = item.action;
        var id = menu.record.getId();
        var router = this.getController('Uni.controller.history.Router');

        switch (action) {
            case 'delete':
                this.showDeleteConfirmation(menu.record);
                break;
            case 'edit':
                router.getRoute('administration/creationrules/edit').forward({id: id});
                break;
        }
    },

    createRule: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/creationrules/add').forward();
    },

    showDeleteConfirmation: function (rule) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('administration.issueCreationRules.deleteConfirmation.msg', 'ISU', 'This issue creation rule will disappear from the list.<br>Issues will not be created automatically by this rule.'),
            title: Ext.String.format(Uni.I18n.translate('administration.issueCreationRules.deleteConfirmation.title', 'ISU', 'Delete rule "{0}"?'), rule.get('name')),
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
            store = this.getStore('Isu.store.CreationRules'),
            page = this.getPage();

        page.setLoading('Removing...');
        rule.destroy({
            params: {
                version: rule.get('version')
            },
            callback: function (model, operation) {
                page.setLoading(false);
                if (operation.response.status == 204) {
                    page.down('#creation-rules-list pagingtoolbartop').totalCount = 0;
                    store.loadPage(1);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('administration.issueCreationRules.deleteSuccess.msg', 'ISU', 'Issue creation rule deleted'));
                }
            }
        });
    }
});