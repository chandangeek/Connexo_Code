/*
 params = {
 type: ('error', 'question', 'notify', 'success', 'attention') ,
 msgBody: [
 {text: "some text", style: ('msgHeaderStyle', 'msgItemStyle')},
 {text: "", style: {}},
 {text: "", style: {}},
 {text: "", style: {}}
 ],
 btns: [
 {text: "button text", handler: function},
 {text: "button text", handler: function}
 ],
 closeBtn: (true/false),
 showTime: milliseconds
 }
 this.getApplication().fireEvent('isushowmsg', params); // add message to common message queue
 Ext.widget('message-panel', params) // create a message panel
 */


Ext.define('Idv.controller.MessageWindow', {
    extend: 'Ext.app.Controller',
    views: [
        'Idv.view.workspace.issues.MessagePanel',
        'Idv.view.workspace.issues.MessageWindow'
    ],
    init: function () {

        this.control({
            'message-panel': {
                afterrender: this.fillWindow
            }
        });
        this.getApplication().on('isushowmsg', this.showMsg);
    },

    randomDirection: function () {
        var direct = [
            Ext.Component.DIRECTION_TOP,
            Ext.Component.DIRECTION_BOTTOM,
            Ext.Component.DIRECTION_RIGHT,
            Ext.Component.DIRECTION_LEFT
        ];
        return direct[Math.floor(Math.random() * direct.length)];
    },


    showMsg: function (params) {
        var msgWindow;
        if (this.msgWindow == undefined) {
            msgWindow = Ext.widget('message-window');
            this.msgWindow = msgWindow;
        } else {
            msgWindow = this.msgWindow;
        }
        params.xtype = 'message-panel';
        msgWindow.add(params);
        msgWindow.center();
        msgWindow.setPosition(msgWindow.x, 50, false)
    },

    initMsgWindow: function (panel) {
        var cls = panel.types[panel.type],
            me = this;
        if (cls.colorCls) {
            panel.addCls(cls.colorCls);
        }
        panel.on('collapse', function (pan) {
            if (pan.isClosing) {
                pan.close();
            } else {
                pan.expand(true);
            }
        });

        panel.collapseClose = function () {
            panel.isClosing = true;
            panel.collapse(me.randomDirection(), true);
        };

        if (panel.showTime) {
            var runner = new Ext.util.TaskRunner(),
                task = runner.newTask({
                    interval: panel.showTime,
                    run: panel.collapseClose
                });
            task.start();
        }
    },

    fillMsgPanel: function (panel) {
        var msgPanel = Ext.ComponentQuery.query('panel[name=msgmessagepanel]', panel)[0],
            msgBody = [];
        Ext.Array.each(panel.msgBody, function (item) {
            item.xtype = 'component';
            item.itemId = 'msgmessagepanel';
            msgBody.push(item)
        });
        msgPanel.add(msgBody);
    },

    fillIconPanel: function (panel) {
        var iconPanel = Ext.ComponentQuery.query('panel[name=msgiconpanel]', panel)[0],
            cls = panel.types[panel.type].iconCls;
        iconPanel.add({
            xtype: 'component',
            cls: cls
        });
    },

    fillClosePanel: function (panel) {
        var iconPanel = Ext.ComponentQuery.query('panel[name=msgclosepanel]', panel)[0];
        if (panel.closeBtn) {
            iconPanel.add({
                xtype: 'button',
                name: 'msgwindowclosebtn',
                iconCls: 'isu-icon-cancel-circled2',
                cls: Ext.baseCSSPrefix + 'btn-plain-toolbar-medium',
                style: {
                    fontSize: '20px',
                    padding: '0px'
                },
                width: 28,
                height: 24,
                handler: function () {
                    panel.close()
                }
            });
        }
    },

    fillBottomPanel: function (panel) {
        var buttons = [],
            bottomPanel = Ext.ComponentQuery.query('panel[name=msgbottompanel]', panel)[0];
        Ext.Array.each(panel.btns, function (item) {
            item.xtype = 'button';
            item.handler = function(){
                item.hnd();
                panel.close();
            };
            buttons.push(item)
        });
        bottomPanel.add(buttons)
    },

    fillWindow: function (panel) {
        Ext.suspendLayouts();
        this.initMsgWindow(panel);
        this.fillMsgPanel(panel);
        this.fillIconPanel(panel);
        this.fillClosePanel(panel);
        this.fillBottomPanel(panel);
        Ext.resumeLayouts(true);
    }
});