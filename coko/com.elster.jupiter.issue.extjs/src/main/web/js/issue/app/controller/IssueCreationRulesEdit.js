Ext.define('Isu.controller.IssueCreationRulesEdit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Isu.view.administration.datacollection.issuecreationrules.EditActionsMenu'
    ],

    stores: [
        'CreationRules'
    ],
    views: [
        'administration.datacollection.issuecreationrules.Edit'
    ],

    refs: [
        {
            ref: 'ruleForm',
            selector: 'issues-creation-rules-edit form'
        }
    ],

    mixins: [
        'Isu.util.IsuGrid'
    ],

    init: function () {
        this.control({
            'issues-creation-rules-edit breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'issues-creation-rules-edit issues-creation-rules-edit-actions-list actioncolumn': {
                click: this.showItemAction
            }
        });

        this.actionMenuXtype = 'issues-creation-rules-edit-actions-menu';
    },

    showOverview: function (id) {
        var widget = Ext.widget('issues-creation-rules-edit');
        this.getApplication().fireEvent('changecontentevent', widget);
        this.setRuleModel(id);
    },

    setBreadcrumb: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Administration',
                href: '#/issue-administration'
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Data collection',
                href: 'datacollection'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issue creation rules',
                href: 'issuecreationrules'
            }),
            breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Edit issue creation rules',
                href: 'edit'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2).setChild(breadcrumbChild3);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    setRuleModel: function (id) {
        var ruleModel = this.getModel('Isu.model.CreationRules');

        ruleModel.load(id, {
            success: function (record) {
                form.loadRecord(record);
            }
        });
    }
});
