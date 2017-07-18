/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.parameters.Profiles', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.view.setup.comtasks.parameters.TimeCombo',
        'Mdc.view.setup.comtasks.parameters.ComboWithToolbar'
    ],
    alias: 'widget.communication-tasks-profilescombo',
    name: 'profiles',
    items: [
        {
            xtype: 'combo-with-toolbar',
            fieldLabel: Uni.I18n.translate('comtask.load.profile.type','MDC','Load profile type'),
            labelWidth: 300,
            width: 570,
            itemId: 'checkProfileTypes',
            store: 'Mdc.store.LoadProfileTypes'
        },
        {
            xtype: 'radiogroup',
            itemId: 'radioIntervals',
            fieldLabel: Uni.I18n.translate('comtask.mark.intervals.as.bad.time','MDC','Mark intervals as bad time'),
            afterLabelTextTpl: '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px; margin-left:7px;"></span>',
            labelWidth: 300,
            width: 500,
            defaults: {
                name: 'intervals',
                margin: '0 10 0 0'
            },
            items: [
                {boxLabel: Uni.I18n.translate('general.yes','MDC','Yes'), inputValue: 'true', id: 'radioYes'},
                {boxLabel: Uni.I18n.translate('general.no','MDC','No'), inputValue: 'false', checked: true, id: 'radioNo'}
            ],
            listeners: {
                change: function () {
                    var radioYes = Ext.getCmp('radioYes');
                    if (radioYes.getValue()) {
                        this.up().down('#mdc-minClockDiffBeforeBadTime').setDisabled(false);
                    } else {
                        this.up().down('#mdc-minClockDiffBeforeBadTime').setDisabled(true);
                    }
                }
            }
        },
        {
            xtype: 'fieldcontainer',
            layout: 'hbox',
            itemId: 'mdc-minClockDiffBeforeBadTime',
            msgTarget: 'under',
            fieldLabel: Uni.I18n.translate('comtask.minimum.clock.difference','MDC','Minimum clock difference'),
            labelWidth: 300,
            width: 500,
            disabled: true,
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'disContNum',
                    name: 'minimumclockdifference',
                    maskRe: /[0-9]+/,
                    margin: '0 10 0 0',
                    flex: 1,
                    value: 60
                },
                {
                    xtype: 'communication-tasks-parameters-timecombo',
                    itemId: 'disContTime',
                    value: 'seconds',
                    flex: 3
                }
            ]
        },
        {
            xtype: 'radiogroup',
            itemId: 'radioEvents',
            fieldLabel: Uni.I18n.translate('comtask.meter.events.from.reading.qualities', 'MDC', 'Meter events from reading qualities'),
            afterLabelTextTpl: '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px; margin-left:7px;"></span>',
            labelWidth: 300,
            width: 500,
            defaults: {
                name: 'events',
                margin: '0 10 0 0'
            },
            items: [
                {boxLabel: Uni.I18n.translate('general.yes','MDC','Yes'), inputValue: 'true'},
                {boxLabel: Uni.I18n.translate('general.no','MDC','No'), inputValue: 'false', checked: true}
            ]
        },
        {
            xtype: 'radiogroup',
            itemId: 'radioFail',
            fieldLabel: Uni.I18n.translate('comtask.fail.profile.configuration.doesnt.match','MDC',"Fail if profile configuration doesn't match"),
            afterLabelTextTpl: '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px; margin-left:7px;"></span>',
            labelWidth: 300,
            width: 500,
            defaults: {
                name: 'fail',
                margin: '0 10 0 0'
            },
            items: [
                {boxLabel: Uni.I18n.translate('general.yes','MDC','Yes'), inputValue: 'true'},
                {boxLabel: Uni.I18n.translate('general.no','MDC','No'), inputValue: 'false', checked: true}
            ]
        }
    ]
});

