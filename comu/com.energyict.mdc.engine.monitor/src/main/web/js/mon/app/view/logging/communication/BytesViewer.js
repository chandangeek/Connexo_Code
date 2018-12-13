/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.logging.communication.BytesViewer', {
    extend: 'Ext.panel.Panel',
    xtype: 'bytesViewer',
    border: true,
    cls: [
        'logviewer',
        'logrecord-date',
        'logmessage',
        'logmessage-error',
        'bytes-read-write'
    ],
    config: {
        linesLogged: 0,
        maxLines: 10000,
        breakCode: '<br>',
        spaceCode: '\xA0', // non-braking space
        hexPnlBody: null,
        hexPnlChild: null,
        decPnlBody: null,
        decPnlChild: null,
        scrollExecRate: 500, // limit to one call every "n" msec
        scrollLastExec: null,
        scrollTask: null
    },

    layout: {
        type : 'hbox',
        align : 'stretch'
    },
    items: [
        {
            xtype: 'panel',
            itemId: 'hexPnl',
            tpl: '{data}',
            tplWriteMode: 'overwrite',
            autoScroll: true,
            bodyStyle: 'padding: 5px;',
            flex: 2
        },
        {
            xtype: 'panel',
            itemId: 'decimalPnl',
            tpl: '{data}',
            tplWriteMode: 'overwrite',
            autoScroll: true,
            bodyStyle: 'padding: 5px;',
            flex: 1
        }
    ],

    logMessage: function(message) {
        var breakCode = this.getBreakCode(),
            newContent_hexPart = '',
            newContent_decPart = '';

        if (this.getLinesLogged() >= this.getMaxLines()) {
            this.getHexPnlChild().removeChild(this.getHexPnlChild().childNodes[0]);
            this.getDecPnlChild().removeChild(this.getDecPnlChild().childNodes[0]);
        } else {
            this.setLinesLogged(this.getLinesLogged() + 1);
        }
        newContent_hexPart += message + breakCode;
        newContent_decPart += message + breakCode;

        var myDivHex = document.createElement('div');
        myDivHex.innerHTML = newContent_hexPart;
        this.getHexPnlChild().appendChild(myDivHex);

        var myDivDec = document.createElement('div');
        myDivDec.innerHTML = newContent_decPart;
        this.getDecPnlChild().appendChild(myDivDec);

        this.scrollToBottom();
    },

    logReadWriteEvent: function(decorator) {
        var breakCode = this.getBreakCode(),
            spaceCode = this.getSpaceCode(),
            newContent_hexPart,
            newContent_decPart,
            hexPart,
            formattedHexPart,
            decPart,
            formattedDecPart,
            i,
            j,
            max;

        if (this.getLinesLogged() >= this.getMaxLines()) {
            this.getHexPnlChild().removeChild(this.getHexPnlChild().childNodes[0]);
            this.getDecPnlChild().removeChild(this.getDecPnlChild().childNodes[0]);
        } else {
            this.setLinesLogged(this.getLinesLogged() + 1);
        }
        formattedHexPart = decorator.getDateAndTypePart() + breakCode;
        formattedDecPart = formattedHexPart;
        hexPart = decorator.getHex();
        decPart = decorator.getChar();
        for (i = 0, max = hexPart.length / 16; i < max; i++) {
            for (j = 0; j < 16; j++) {
                if (hexPart[16 * i + j] === undefined) {
                    formattedHexPart += breakCode;
                    formattedDecPart += breakCode;
                    break;
                }
                formattedHexPart += hexPart[16 * i + j];
                formattedDecPart += decPart[16 * i + j];
                if (j !== 15) {
                    formattedHexPart += spaceCode;
                } else {
                    formattedHexPart += breakCode;
                    formattedDecPart += breakCode;
                }
                if (j === 3 || j === 7 || j === 11) {
                    formattedHexPart += spaceCode;
                }
            }
        }
        newContent_hexPart = '<span class="bytes-read-write">' + formattedHexPart + '</span>' + breakCode;
        newContent_decPart = '<span class="bytes-read-write">' + formattedDecPart + '</span>' + breakCode;

        var myDivHex = document.createElement('div');
        myDivHex.innerHTML = newContent_hexPart;
        this.getHexPnlChild().appendChild(myDivHex);

        var myDivDec = document.createElement('div');
        myDivDec.innerHTML = newContent_decPart;
        this.getDecPnlChild().appendChild(myDivDec);

        this.scrollToBottom();
    },

    syncScrolling: function() {
        var me = this;
        this.getDecPnlBody().on('scroll', function(e, t) {
            me.getHexPnlBody().dom.scrollTop = t.scrollTop;
        });
        this.getHexPnlBody().on('scroll', function(e, t) {
            me.getDecPnlBody().dom.scrollTop = t.scrollTop;
        });
    },

    getHexPnlBody: function() {
        if (!this.hexPnlBody) {
            this.hexPnlBody = this.down('#hexPnl').body;
        }
        return this.hexPnlBody;
    },

    getHexPnlChild: function() {
        if (!this.hexPnlChild) {
            this.hexPnlChild = this.getHexPnlBody().dom.firstChild.firstChild;
        }
        return this.hexPnlChild;
    },

    getDecPnlBody: function() {
        if (!this.decPnlBody) {
            this.decPnlBody = this.down('#decimalPnl').body;
        }
        return this.decPnlBody;
    },

    getDecPnlChild: function() {
        if (!this.decPnlChild) {
            this.decPnlChild = this.getDecPnlBody().dom.firstChild.firstChild;
        }
        return this.decPnlChild;
    },

    scrollToBottom: function() {
        if (!this.getScrollLastExec()) {
            this.setScrollTask(Ext.TaskManager.newTask({
                interval: this.getScrollExecRate(),
                scope: this,
                run: this.scrollToBottom
            }));
            this.setScrollLastExec(new Date());
            this.getDecPnlBody().dom.scrollTop = 999999; // Scroll to the bottom
            return;
        }
        var d = new Date();
        if (d - this.getScrollLastExec() < this.getScrollExecRate()) {
            // This function has been called "too soon," (ie. before the allowed "rate")
            this.getScrollTask().stop();
            this.getScrollTask().start();
            return;
        }
        this.getScrollTask().stop();
        this.setScrollLastExec(new Date());
        // Only one scroll needed since both are synced:
        this.getDecPnlBody().dom.scrollTop = 999999; // Scroll to the bottom
    }

});
