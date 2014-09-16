Ext.define('Isu.view.workspace.issues.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-preview',
    requires: [
        'Isu.view.workspace.issues.ActionMenu',
        'Isu.view.workspace.issues.DataCollectionPreviewForm',
        'Isu.view.workspace.issues.DataValidationPreviewForm'
    ],
    title: '',
    frame: true,
    issueType: null,
    router: null,

    initComponent: function () {
        var me =this,
            previewForm;

        switch (me.issueType) {
            case 'datacollection':
                previewForm = 'datacollection-issue-form';
                break;
            case 'datavalidation':
                previewForm = 'datavalidation-issue-form';
                break;
        }

        me.items = {
            xtype: previewForm,
            itemId: 'issues-preview-form',
            showFilters: true,
            router: me.router,
            bbar: {
                layout: {
                    type: 'vbox',
                    align: 'right'
                },
                items: {
                    text: Uni.I18n.translate('general.title.viewDetails', 'ISU', 'View details'),
                    itemId: 'view-details-link',
                    ui: 'link',
                    href: location.href
                }
            }
        };

        me.tools = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'issue-action-menu',
                    itemId: 'issue-action-menu',
                    issueType: me.issueType
                }
            }
        ];

        me.callParent(arguments);
    }
});