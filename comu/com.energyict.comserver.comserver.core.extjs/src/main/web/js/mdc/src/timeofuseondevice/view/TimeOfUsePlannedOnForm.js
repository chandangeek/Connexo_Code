/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuseondevice.view.TimeOfUsePlannedOnForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.device-tou-planned-on-form',
    layout: {
        type: 'column'
    },

    requires: [
        'Uni.util.FormEmptyMessage'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            defaults: {
                labelWidth: 250
            },
            items: [
                {
                    xtype: 'uni-form-empty-message',
                    itemId: 'willNotBePickedUpMessage',
                    hidden: true,
                    margin: '5 0 5 0',
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseCalendar', 'MDC', 'Time of use calendar'),
                    name: 'name'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('timeofuse.releaseDate', 'MDC', 'Release date (command)'),
                    name: 'releaseDateDisplayField'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('timeofuse.activationDate', 'MDC', 'Activation date'),
                    name: 'activationDateDisplayField'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                    name: 'status'
                }
            ]
        };
        me.callParent(arguments);
    },

    checkWillNotBePickedUp: function (willBePickedUpByPlannedComtask, willBePickedUpByComtask) {
        var me = this;
        if (!willBePickedUpByPlannedComtask || !willBePickedUpByComtask) {
            me.down('#willNotBePickedUpMessage').show();
            if (!willBePickedUpByPlannedComtask && willBePickedUpByComtask) {
                me.down('#willNotBePickedUpMessage').setText(Uni.I18n.translate('deviceCommand.willNotBePickedUpByPlannedComTask', 'MDC', 'This command is part of a communication task that is not planned and will not be picked up.'));
            } else {
                me.down('#willNotBePickedUpMessage').setText(Uni.I18n.translate('deviceCommand.willBePickedUpByComTask', 'MDC', 'This command is not part of a communication task on this device.'));
            }
        }
    }

});