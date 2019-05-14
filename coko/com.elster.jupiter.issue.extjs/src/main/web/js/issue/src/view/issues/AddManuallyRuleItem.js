/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.AddManuallyRuleItem', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.issues.ManuallyRuleItem'
    ],
    itemId: 'issue-manually-creation-rules-item-add',
    alias: 'widget.issue-manually-creation-rules-item-add',
    returnLink: null,
    action: null,

    initComponent: function () {
        var me = this;

        me.content = [{
                xtype: 'issue-manually-creation-rules-item',
                itemId: 'issue-manually-creation-rules-item',
                title: Uni.I18n.translate('workspace.newManuallyIssue', 'ISU', 'Create issue'),
                ui: 'large',
                returnLink: me.returnLink,
                action: me.action
            }
        ];

        me.callParent(arguments);
    }
});