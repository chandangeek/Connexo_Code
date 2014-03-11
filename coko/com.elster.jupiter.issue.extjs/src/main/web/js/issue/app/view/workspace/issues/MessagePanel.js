Ext.define('Isu.view.workspace.issues.MessagePanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.message-panel',
    msgHeaderStyle: {
        fontSize: '16px',
        margin: '10 0 0 0'
    },
    msgItemStyle: {
        fontSize: '12px',
        margin: '5 0 0 10'
    },
    autoShow: true,
    resizable: false,
    bodyBorder: false,
    shadow: false,
    animCollapse: true,
    border: false,
    header: false,
    cls: 'isu-msg-panel',
    margin: '2',
    defaults: {
        style: {
            opacity: 1
        }
    },
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
            cls: 'isu-msg-panel',
            defaults: {
                opacity: 1
            },
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                // ICON
                {
                    xtype: 'panel',
                    name: 'msgiconpanel',
                    layout: {
                        type: 'hbox',
                        align: 'middle'
                    },
                    cls: 'isu-msg-panel',
                    defaults: {
                        opacity: 1
                    }
                },
                // MESSAGE
                {
                    xtype: 'panel',
                    name: 'msgmessagepanel',
                    cls: 'isu-msg-panel',
                    defaults: {
                        opacity: 1
                    },
                    layout: {
                        type: 'vbox',
                        align: 'left'
                    }
                },
                // CLOSE BTN
                {
                    xtype: 'panel',
                    name: 'msgclosepanel',
                    cls: 'isu-msg-panel',
                    defaults: {
                        opacity: 1
                    },
                    layout: {
                        type: 'hbox'
                    }
                }
            ]
        },
        // BOTTOM BAR
        {
            xtype: 'panel',
            name: 'msgbottompanel',
            cls: 'isu-msg-panel',
            border: 0,
            defaults: {
                opacity: 1
            },
            padding: '0 0 0 40'
        }
    ]
})
;
