Ext.define('Idv.view.workspace.issues.MessagePanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.message-panel',
    itemId: 'message-panel',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    types: {
        attention: {
            iconCls: 'isu-icon-attention isu-msg-attention-icon',
            colorCls: 'isu-msg-attention'
        },
        question: {
            iconCls: 'isu-icon-help isu-msg-question-icon',
            colorCls: 'isu-msg-question'
        },
        success: {
            iconCls: 'isu-icon-ok isu-msg-success-icon',
            colorCls: 'isu-msg-success'
        },
        error: {
            iconCls: 'isu-icon-attention isu-msg-error-icon',
            colorCls: 'isu-msg-error'
        },
        notify: {
            iconCls: '',
            colorCls: 'isu-msg-notify'
        }
    },

    items: [
        //MAIN BODY
        {
            xtype: 'panel',
            layout: {
                type: 'hbox',
                align: 'top'
            },
            items: [
                // ICON
                {
                    itemId: 'msgIcon',
                    xtype: 'panel',
                    name: 'msgiconpanel',
                    margin: '0 10 0 0'
                },
                // MESSAGE
                {
                    itemId : 'msgmessage',
                    xtype: 'panel',
                    name: 'msgmessagepanel'
                },
                // CLOSE BTN
                {
                    itemId : 'closeBTN',
                    xtype: 'panel',
                    name: 'msgclosepanel'
                }
            ]
        },
        // BOTTOM BAR
        {
            xtype: 'panel',
            name: 'msgbottompanel'
        }
    ]
})
;
