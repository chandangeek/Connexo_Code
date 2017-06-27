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
                selectionchange: this.loadCommandDetail
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

    loadCommandDetail: function (selectionModel, selectedRecords) {
        var me = this,
            record = selectedRecords[0],
            preview = me.getCommandPreview(),
            previewForm = me.getCommandPreviewForm(),
            trackingField = previewForm.down('#mdc-command-preview-tracking-field'),
            previewActionsButton = me.getPreviewActionsBtn(),
            previewActionsMenu = preview.down('menu');

        if (Ext.isEmpty(record)) return;
        Ext.suspendLayouts();
        if (record.get('trackingCategory').id === 'trackingCategory.serviceCall') {
            trackingField.setFieldLabel(Uni.I18n.translate('general.serviceCall', 'MDC', 'Service call'));
            trackingField.renderer = function(val) {
                if (record.get('trackingCategory').activeLink != undefined && record.get('trackingCategory').activeLink) {
                    return '<a style="text-decoration: underline" href="' +
                        me.getController('Uni.controller.history.Router').getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: val.id})
                        + '">' + val.name + '</a>';
                } else {
                    return Ext.isEmpty(val.id) ? '-'  : Ext.String.htmlEncode(val.id);
                }
            }
        } else {
            trackingField.setFieldLabel(Uni.I18n.translate('general.trackingSource', 'MDC', 'Tracking source'));
            trackingField.renderer = function (val) {
                return !Ext.isEmpty(val) && !Ext.isEmpty(val.name) ? Ext.String.htmlEncode(val.name) : '-';
            }
        }
        previewForm.loadRecord(record);
        preview.setTitle(Ext.String.htmlEncode(record.get('messageSpecification').name));
        if (previewActionsMenu) {
            previewActionsMenu.record = record;
        }

        var status = record.get('status').value;
        previewActionsButton.setVisible( (status === 'WAITING' || status === 'PENDING') );
        Ext.resumeLayouts(true);
    }

});
