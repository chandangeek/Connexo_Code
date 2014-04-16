Ext.define('Isu.controller.IssueCreationRules', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Isu.store.CreationRule'
    ],
    views: [
        'Isu.view.administration.datacollection.issuecreationrules.Overview',
        'Isu.view.ext.button.GridAction',
        'Isu.view.administration.datacollection.issuecreationrules.ActionMenu',
        'Isu.view.workspace.issues.MessagePanel'
    ],

    mixins: {
        isuGrid: 'Isu.util.IsuGrid'
    },

    refs: [
        {
            ref: 'itemPanel',
            selector: 'issue-creation-rules-overview issue-creation-rules-item'
        }
    ],

    init: function () {
        this.control({
            'issue-creation-rules-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'issue-creation-rules-overview issues-creation-rules-list gridview': {
                itemclick: this.loadGridItemDetail,
                refresh: this.onGridRefresh
            },
            'issue-creation-rules-overview issues-creation-rules-list actioncolumn': {
                click: this.showItemAction
            },
            'creation-rule-action-menu': {
                beforehide: this.hideItemAction,
                click: this.chooseAction
            },
            'issue-creation-rules-overview button[action=create]': {
                click: this.createRule
            }
        });

        this.actionMenuXtype = 'creation-rule-action-menu';
        this.gridItemModel = this.getModel('Isu.model.CreationRule');
    },

    showOverview: function () {
        var widget = Ext.widget('issue-creation-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    setBreadcrumb: function (breadcrumbs) {
        var me = this;
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Administration',
                href: me.getController('Isu.controller.history.Administration').tokenizeShowOverview()
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issue creation rules',
                href: 'issuecreationrules'
            });
        breadcrumbParent.setChild(breadcrumbChild1);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    onGridRefresh: function (grid) {
        this.setAssigneeTypeIconTooltip(grid);
        this.setDescriptionTooltip(grid);
        this.selectFirstGridRow(grid);
    },

    chooseAction: function (menu, item) {
        var action = item.action;

        switch (action) {
            case 'delete':
                this.deleteRule(menu);
                break;
            case 'edit':
                window.location.href = '#/issue-administration/issuecreationrules/' + menu.issueId + '/edit';
                break;
        }
    },

    createRule: function () {
        window.location.href = '#/issue-administration/issuecreationrules/create';
    },

    deleteRule: function (menu) {
        var self = this,
            store = self.getStore('Isu.store.CreationRule'),
            rule = store.getById(menu.issueId),
            confirmMessage = Ext.widget('messagebox', {
                buttons: [
                    {
                        text: 'Delete',
                        handler: function () {
                            rule.destroy({
                                params: {
                                    version: rule.data.version
                                },
                                callback: function (model, operation) {
                                    confirmMessage.close();
                                    store.loadPage(1);
                                    if (operation.response.status == 204) {
                                        self.getApplication().fireEvent('isushowmsg', {
                                            type: 'notify',
                                            msgBody: [
                                                {
                                                    style: 'msgHeaderStyle',
                                                    text: 'Issue creation rule deleted'
                                                }
                                            ],
                                            y: 10,
                                            showTime: 5000
                                        });
                                    }
                                }
                            });
                        }
                    },
                    {
                        text: 'Cancel',
                        cls: 'isu-btn-link',
                        handler: function () {
                            confirmMessage.close();
                        }
                    }
                ]
            });

        confirmMessage.show({
            title: 'Delete issue creation rule',
            msg: '<p><b>Delete rule "' + rule.data.name + '"?</b></p><p>This issue creation rule will disappear from the list.<br>Issues will not be created automatically by this rule.</p>',
            icon: Ext.MessageBox.WARNING,
            cls: 'isu-delete-message'
        });
    }
});
