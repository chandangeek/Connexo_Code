/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.creationrules.EditGroups', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-creation-rules-edit-groups',
    requires: [
        'Isu.view.creationrules.EditGroupsForm'
    ],
    returnLink: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'issues-creation-rules-edit-groups-form',
                itemId: 'issues-creation-rules-edit-groups-form',
                title: me.router.getRoute().getTitle(),
                ui: 'large',
                returnLink: me.returnLink
            }
        ];

        me.callParent(arguments);
    }
});