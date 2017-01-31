/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.Main', {
    extend: 'Ext.container.Container',
    requires: [
        'Ext.tab.Panel',
        'Ext.layout.container.Border'
    ],
    border: false,
    xtype: 'app-main',
    layout: {
        type: 'border'
    },

    config: {
        serverName : null,
        docTitleSuffix: null
    },

    items: [
        {
            region: 'north',
            xtype: 'serverDetails'
        }
    ],

    addEmptyTabPanel: function() {
        this.add(
            {
                region: 'center',
                xtype: 'tabpanel',
                itemId: 'mainTabPnl',
                deferredRender: false,
                tabBar: { plain: true },
                defaults : { bodyPadding: '0, 10, 0, 10', layout: 'fit' },
                items: []
            }
        );
    },

    addCenterPanel: function(xTypeOfPanel) {
        this.add(
            {
                region: 'center',
                xtype: xTypeOfPanel
            }
        );
        if (xTypeOfPanel === 'generalLoggingText') {
            this.setDocTitleSuffix("General Log");
        } else if (xTypeOfPanel === 'dataLoggingText') {
            this.setDocTitleSuffix("Data Storage Log");
        } else if (xTypeOfPanel === 'communicationLoggingText') {
            this.setDocTitleSuffix("Com Log");
        }
        var newTitle;
        if (this.getServerName()) {
            newTitle = this.getServerName() + "-" + this.getDocTitleSuffix();
        } else {
            newTitle = this.getDocTitleSuffix();
        }
        if (document.title !== newTitle) {
            document.title = newTitle;
        }
    },

    addSouthPanel: function(xTypeOfPanel, title) {
        this.add(
            {
                region: 'south',
                floatable: false,
                titleCollapse: true,
                split: true,
                xtype: xTypeOfPanel,
                collapsible: true,
                title: title,
                flex: 1
            }
        );
    },

    clearTabs: function() {
        this.down('#mainTabPnl').removeAll();
    },

    addTab: function(tabTitle, xTypeOfTabContent) {
        this.down('#mainTabPnl').add(
            {
                title: tabTitle,
                items: [
                    {
                        xtype: xTypeOfTabContent
                    }
                ]
            }
        );
    },

    setActiveTab: function(tabNumber) {
        this.down('#mainTabPnl').setActiveTab(tabNumber);
    },

    setServerName: function(name) {
        this.serverName = name;
        var newTitle;
        if (!this.getDocTitleSuffix()) {
            newTitle = this.getServerName() + "-Communication Server Monitor";
        } else {
            newTitle = this.getServerName() + "-" + this.getDocTitleSuffix();
        }
        if (document.title !== newTitle) {
            document.title = newTitle;
        }
    }
});
