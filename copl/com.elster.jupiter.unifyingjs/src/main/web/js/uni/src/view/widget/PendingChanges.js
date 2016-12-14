Ext.define('Uni.view.widget.PendingChanges', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.pendingChangesPanel',

    requires: [
        'Uni.view.grid.PendingChangesGrid',
        'Uni.util.FormInfoMessage'
    ],
    store: 'Uni.store.PendingChanges',

    title: Uni.I18n.translate('general.pendingChanges', 'UNI', 'Pending changes'),
    ui: 'large',
    acceptRejectButtonsVisible: true,
    acceptButtonDisabled: false,

    initComponent: function() {
        var me = this;
        me.items = [
            {
                xtype: 'uni-form-info-message',
                itemId: 'uni-pendingChangesPnl-info-msg',
                text: Uni.I18n.translate('general.noPendingChanges', 'UNI', 'This command limitation rule has no pending changes.'),
                hidden: true
            },
            {
                xtype: 'pendingChangesGrid',
                itemId: 'uni-pendingChangesPnl-changes-grid',
                store: me.store
            },
            {
                xtype: 'container',
                itemId: 'uni-pendingChangesPnl-button-container',
                hidden: !me.acceptRejectButtonsVisible,
                defaults: {
                    xtype: 'button'
                },
                items: [
                    {
                        text: Uni.I18n.translate('general.accept', 'UNI', 'Accept'),
                        itemId: 'uni-pendingChangesPnl-accept',
                        disabled: me.acceptButtonDisabled
                    },
                    {
                        text: Uni.I18n.translate('general.reject', 'UNI', 'Reject'),
                        itemId: 'uni-pendingChangesPnl-reject'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    },

    listeners: {
        afterrender: function(panel) {
            var grid = panel.down('#uni-pendingChangesPnl-changes-grid'),
                message = panel.down('#uni-pendingChangesPnl-info-msg'),
                buttonContainer = panel.down('#uni-pendingChangesPnl-button-container'),
                changesCount = panel.store.getCount();

            grid.setVisible(changesCount > 0);
            if (changesCount === 0) {
                message.setText(Uni.I18n.translate('general.noPendingChanges', 'UNI', 'This command limitation rule has no pending changes.'));
                message.show();
            } else {
                if (panel.acceptButtonDisabled) {
                    message.setText(Uni.I18n.translate('general.waitingForOtherApprovals', 'UNI', 'Waiting for other users to approve.'));
                    message.show();
                } else {
                    message.hide();
                }
            }
            buttonContainer.setVisible(panel.acceptRejectButtonsVisible && changesCount > 0);
        }
    }
});