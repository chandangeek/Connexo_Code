/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.searchitems.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.searchitems-bulk-step4',
    bodyCls: 'isu-bulk-wizard-no-border',
    name: 'confirmPage',
    layout: 'vbox',
    title: Uni.I18n.translate('searchItems.bulk.step4title', 'MDC', 'Step 4: Confirmation'),
    ui: 'large',
    tbar: {
        xtype: 'panel',
        ui: 'medium',
        style: {
            padding: '0 0 0 3px'
        },
        title: '',
        itemId: 'searchitemsbulkactiontitle'
    },

    items: [
        {
            xtype: 'displayfield',
            itemId: 'displayTitle',
            htmlEncode: false
        },
        {
            xtype: 'displayfield',
            itemId: 'messageField'
        },
        {
            xtype: 'form',
            width: '100%',
            ui: 'large',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            itemId: 'strategyform',
            items: [
                {
                    xtype: 'displayfield',
                    value: Uni.I18n.translate('searchItems.bulk.chooseStrategy', 'MDC',
                        "It's not possible to add a new shared communication schedule to a device if it contains a communication task that is already scheduled with a shared communication schedule on that device. In that case, choose a strategy to deal with this.")
                },
                {
                    xtype: 'radiogroup',
                    fieldLabel: 'Strategy',
                    itemId: 'strategyRadioGroup',
                    labelWidth: 150,
                    required: true,
                    allowBlank: false,
                    columns: 1,
                    vertical: true,
                    items: [
                        {boxLabel: "Keep the old shared communication schedule and don't add the new one", name: 'rb', inputValue: 'keep'},
                        {boxLabel: 'Remove the old shared communication schedule and add the new one', name: 'rb', inputValue: 'remove'}
                    ]
                }
            ]
        }
    ],
    showMessage: function (message) {
        this.down('#messageField').setValue(Ext.String.htmlEncode(message.body));
        this.down('#displayTitle').setValue('<h3>' + Ext.String.htmlEncode(message.title) + '</h3>');
    },

    isRemove: function () {
        this.down('#strategyform').hide();
    },

    showStrategyForm: function () {
        this.down('#strategyform').show();
    },


    showChangeDeviceConfigConfirmation: function (title, text, solveLink, additionalText, type) {
        var bodyText, widget,
            solve = solveLink ? Uni.I18n.translate('searchItems.bulk.SolveTheConflictsBeforeYouRetry', 'MDC', '<br><a href="{0}">Solve the conflicts</a> before you retry.', solveLink) : '';
        bodyText = Ext.String.htmlEncode(text) + '<br>' + solve;
        if (additionalText) bodyText += '<br>' + additionalText;
        type = type ? type : 'confirmation';
        widget = {
            xtype: 'uni-notification-panel',
            margin: '0 0 0 -13',
            message: title,
            type: type,
            additionalItems: [
                {
                    xtype: 'container',
                    html: bodyText
                }
            ]
        };
        Ext.suspendLayouts();
        this.removeAll();
        this.add(widget);
        Ext.resumeLayouts(true);
    },

    showStartProcessConfirmation: function (title, text, additionalText, type) {
        var widget, bodyText = Ext.String.htmlEncode(text);
        console.log('title=',title);
        console.log('tetxt=',text);
        console.log('additionalText=',additionalText);
        if (additionalText) bodyText += '<br>' + additionalText;
        type = type ? type : 'confirmation';
        widget = {
            xtype: 'uni-notification-panel',
            margin: '0 0 0 -13',
            message: title,
            type: type,
            additionalItems: [
                {
                    xtype: 'container',
                    html: bodyText
                }
            ]
        };
        Ext.suspendLayouts();
        this.removeAll();
        this.add(widget);
        Ext.resumeLayouts(true);
    },

    showWarningZoneTypeAlreadyLinkedToDevice: function (devices) {
        var me = this,
            titleWarning,
            devicesPanel = {
                xtype: 'panel',
                itemId: 'warning-device-zones-panel',
                ui: 'tile',
                width: 600,
                items: [
                    {
                        xtype: 'uni-form-info-message',
                        style: 'border: 0px;',
                        margin: '7 0 17 0',
                        itemId: 'device-zones-error-msg',
                        iconCmp: {
                            xtype: 'component',
                            style: 'font-size: 28px; color: #eb5642; margin: 0px -22px 0px 0px;',
                            cls: 'icon-warning'
                        },
                    }
                ]
            },
            zonesContainer = {
                itemId: 'warning-device-zones',
                xtype: 'fieldcontainer'
            };

        me.add(devicesPanel);
        me.down('#warning-device-zones-panel').add(zonesContainer);
        if (!Ext.isEmpty(devices.deviceNames)) {

            titleWarning = Uni.I18n.translatePlural('searchItems.bulk.WarningDeviceLinkedToSameZoneType', devices.total, 'MDC',
                '{0} device already has another zone with the same zone type defined',
                '{0} device already has another zone with the same zone type defined',
                '{0} devices already have another zone with the same zone type defined', devices.total);

            me.down('#device-zones-error-msg').setText(titleWarning);
            Ext.Array.each(devices.deviceNames, function (device) {
                me.down('#warning-device-zones-panel').down('#warning-device-zones').add({
                        xtype: 'displayfield',
                        labelWidth: 120,
                        value: device,
                        margin: '0 0 0 15',
                        renderer: function (value) {
                            return '<a href="#/devices/' + encodeURIComponent(value) + '">' + Ext.String.htmlEncode(value) + '</a>';
                        },
                    });
            });
        }
    },


});
