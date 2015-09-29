Ext.define('Mdc.view.setup.searchitems.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.searchitems-bulk-step4',
    bodyCls: 'isu-bulk-wizard-no-border',
    name: 'confirmPage',
    layout: 'hbox',
    title: Uni.I18n.translate('searchItems.bulk.step4title', 'MDC', 'Bulk action - Step 4 of 5: Confirmation'),
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
    showMessage: function (message) {
        var widget = {
            html: '<h3>' + Ext.String.htmlEncode(message.title) + '</h3><br>' + Ext.String.htmlEncode(message.body)
        };
        Ext.suspendLayouts();
        this.removeAll();
        this.add(widget);
        Ext.resumeLayouts();
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