Ext.define('Est.estimationrules.view.PreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    requires: [
        'Est.estimationrules.view.Grid',
        'Est.estimationrules.view.DetailForm',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    alias: 'widget.estimation-rules-preview-container',
    router: null,
    actionMenuItemId: null,
    editOrder: false,

    initComponent: function () {
        var me = this;

        me.grid = {
            xtype: 'estimation-rules-grid',
            itemId: 'estimation-rules-grid',
            router: me.router,
            actionMenuItemId: me.actionMenuItemId,
            editOrder: me.editOrder
        };

        me.emptyComponent = {
            xtype: 'no-items-found-panel',
            itemId: 'no-estimation-rules-found-panel',
            title: Uni.I18n.translate('estimationrules.empty.title', 'EST', 'No estimation rules found'),
            reasons: [
                Uni.I18n.translate('estimationrules.empty.list.item1', 'EST', 'No estimation rules have been defined yet.'),
                Uni.I18n.translate('estimationrules.empty.list.item2', 'EST', 'Estimation rules exist, but you do not have permission to view them.')
            ],
            stepItems: [
                {
                    text: Uni.I18n.translate('estimationrules.addEstimationRule', 'EST', 'Add estimation rule'),
                    action: 'addEstimationRule',
                    href: me.router.getRoute('administration/estimationrulesets/estimationruleset/rules/add').buildUrl(),
                    privileges: Est.privileges.EstimationConfiguration.administrate
                }
            ]
        };

        me.previewComponent = {
            xtype: 'estimation-rules-detail-form',
            itemId: 'estimation-rule-preview',
            actionMenuItemId: me.actionMenuItemId,
            frame: true,
            ui: 'default',
            title: ''
        };

        me.callParent(arguments);
    }
});