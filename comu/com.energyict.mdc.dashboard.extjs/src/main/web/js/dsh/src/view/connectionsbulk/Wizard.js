Ext.define('Dsh.view.connectionsbulk.Wizard', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Dsh.view.connectionsbulk.Step1',
        'Dsh.view.connectionsbulk.Step2',
        'Dsh.view.connectionsbulk.Step3',
        'Dsh.view.connectionsbulk.Step4',
        'Dsh.view.connectionsbulk.Step5'
    ],
    alias: 'widget.connections-bulk-wizard',
    layout: 'card',
    defaults: {
        ui: 'large'
    },
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'connections-bulk-step1',
                itemId: 'cnbw-step1',
                title: Ext.String.format(Uni.I18n.translate('general.bulkStepTitle', 'DSH', 'Bulk action - Step {0} of {1}:'), 1, 5)
                + ' ' + Uni.I18n.translate('connection.bulk.selectConnections', 'DSH', 'Select connections'),
                router: me.router
            },
            {
                xtype: 'connections-bulk-step2',
                itemId: 'cnbw-step2',
                title: Ext.String.format(Uni.I18n.translate('general.bulkStepTitle', 'DSH', 'Bulk action - Step {0} of {1}:'), 2, 5)
                + ' ' + Uni.I18n.translate('general.selectAction', 'DSH', 'Select action')
            },
            {
                xtype: 'connections-bulk-step3',
                itemId: 'cnbw-step3',
                title: Ext.String.format(Uni.I18n.translate('general.bulkStepTitle', 'DSH', 'Bulk action - Step {0} of {1}:'), 3, 5)
                + ' ' + Uni.I18n.translate('general.actionDetails', 'DSH', 'Action details')
            },
            {
                xtype: 'connections-bulk-step4',
                itemId: 'cnbw-step4',
                title: Ext.String.format(Uni.I18n.translate('general.bulkStepTitle', 'DSH', 'Bulk action - Step {0} of {1}:'), 4, 5)
                + ' ' + Uni.I18n.translate('general.confirmation', 'DSH', 'Confirmation')
            },
            {
                xtype: 'connections-bulk-step5',
                itemId: 'cnbw-step5',
                title: Ext.String.format(Uni.I18n.translate('general.bulkStepTitle', 'DSH', 'Bulk action - Step {0} of {1}:'), 5, 5)
                + ' ' + Uni.I18n.translate('general.status', 'DSH', 'Status'),
                router: me.router
            }
        ];

        me.bbar = {
            itemId: 'cnbw-buttons',
            items: [
                {
                    text: Uni.I18n.translate('general.back', 'DSH', 'Back'),
                    action: 'step-back',
                    itemId: 'cnbw-step-back',
                    disabled: true
                },
                {
                    text: Uni.I18n.translate('general.next', 'DSH', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    itemId: 'cnbw-step-next'
                },
                {
                    text: Uni.I18n.translate('general.confirm', 'DSH', 'Confirm'),
                    ui: 'action',
                    action: 'confirm-action',
                    itemId: 'cnbw-confirm-action',
                    hidden: true
                },
                {
                    text: Uni.I18n.translate('general.finish', 'DSH', 'Finish'),
                    ui: 'action',
                    action: 'finish',
                    itemId: 'cnbw-finish',
                    hidden: true,
                    href: me.router.getRoute('workspace/connections/details').buildUrl()
                },
                {
                    text: Uni.I18n.translate('general.cancel', 'DSH', 'Cancel'),
                    ui: 'link',
                    action: 'cancel',
                    itemId: 'cnbw-finish-cancel',
                    href: me.router.getRoute('workspace/connections/details').buildUrl()
                }
            ]
        };

        me.callParent(arguments);
    }
});