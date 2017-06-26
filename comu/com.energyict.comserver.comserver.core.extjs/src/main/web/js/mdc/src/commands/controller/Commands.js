/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.commands.controller.Commands', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.commands.view.CommandsOverview',
        'Mdc.commands.view.AddCommand'
    ],

    requires: [
    ],

    stores: [
        'Mdc.commands.store.Commands'
    ],

    refs: [
        {
            ref: 'commandPreview',
            selector: 'command-preview'
        },
        {
            ref: 'commandPreviewForm',
            selector: 'command-preview command-preview-form'
        },
        {
            ref: 'previewActionsBtn',
            selector: '#mdc-command-preview-actions-button'
        },
        {
            ref: 'commandsGrid',
            selector: 'commands-grid'
        }
    ],

    init: function () {
        this.control({
            '#mdc-empty-commands-grid-add-button': {
                click: this.navigateToAddCommand
            },
            '#mdc-add-command-cancel': {
                click: this.navigateToCommandsOverview
            },
            '#mdc-commands-grid': {
                select: this.loadCommandDetail
            }
        });
    },

    navigateToCommandsOverview: function () {
        this.getController('Uni.controller.history.Router').getRoute('workspace/commands').forward();
    },

    navigateToAddCommand: function () {
        this.getController('Uni.controller.history.Router').getRoute('workspace/commands/add').forward();
    },

    showCommandsOverview: function() {
        var me = this,
            widget = Ext.widget('commands-overview');

        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showAddCommand: function() {
        var me = this,
            widget = Ext.widget('add-command');

        me.getApplication().fireEvent('changecontentevent', widget);
    },

    loadCommandDetail: function (selectionModel, record) {
        var me = this,
            preview = me.getCommandPreview(),
            previewForm = me.getCommandPreviewForm(),
            previewActionsButton = me.getPreviewActionsBtn(),
            previewActionsMenu = preview.down('menu');

        Ext.suspendLayouts();
        previewForm.loadRecord(record);
        preview.setTitle(Ext.String.htmlEncode(record.get('messageSpecification').name));
        Ext.resumeLayouts(true);
        if (previewActionsMenu) {
            previewActionsMenu.record = record;
        }

        var status = record.get('status').value;
        previewActionsButton.setVisible( (status === 'WAITING' || status === 'PENDING') );
    }

});
