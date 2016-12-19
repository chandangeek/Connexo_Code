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
                        { boxLabel: "Keep the old shared communication schedule and don't add the new one", name: 'rb', inputValue: 'keep' },
                        { boxLabel: 'Remove the old shared communication schedule and add the new one', name: 'rb', inputValue: 'remove'}
                    ]
                }
            ]
        }
    ],
    showMessage: function (message) {
        this.down('#messageField').setValue(Ext.String.htmlEncode(message.body));
        this.down('#displayTitle').setValue('<h3>' + Ext.String.htmlEncode(message.title) + '</h3>');
    },

    isRemove: function() {
        this.down('#strategyform').hide();
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
    }

});