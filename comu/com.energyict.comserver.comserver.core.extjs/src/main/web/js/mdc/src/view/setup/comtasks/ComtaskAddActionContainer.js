/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskAddActionContainer', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comtaskAddActionContainer',
    itemId: 'mdc-comtask-addActions-view',
    requires: [
        'Mdc.view.setup.comtasks.ComtaskAddActionForm'
    ],

    router: null,
    communicationTask: null,
    cancelRoute : null,
    btnAction: null,
    btnText: null,

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'comtaskAddActionForm',
                itemId: 'mdc-comtask-addAction-form',
                router: me.router,
                cancelRoute: me.cancelRoute,
                btnAction: me.btnAction,
                btnText: me.btnText,
                hideInfoMsg: true
            }
        ];
        me.callParent(arguments);
    }
});