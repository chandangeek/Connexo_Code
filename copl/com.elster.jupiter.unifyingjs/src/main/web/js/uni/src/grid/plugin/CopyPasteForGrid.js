/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.grid.plugin.CopyPasteForGrid', {
    extend: 'Ext.AbstractPlugin',
    alias: 'plugin.gridviewcopypaste',
    requires: [],

    init: function (grid) {
        var me = this;
        grid.on("cellclick", function (grid, td, cellIndex, record, tr, rowIndex) {
            gRow = rowIndex;
        });

        grid.on("viewready", function (grid) {
            new Ext.KeyMap(grid.getEl(),
                [
                    {
                        key: "c",
                        ctrl: true,
                        fn: function (keyCode, e) {
                            me.copyToClipBoard(grid);
                        }
                    },
                    {
                        key: "v",
                        ctrl: true,
                        fn: function () {
                            me.pasteFromClipBoard(grid);
                        }
                    }
                ]
            );
        });
    },

    copyToClipBoard: function (grid) {
        var me = this, data;

        data = me.collectGridData(grid);
        if (window.clipboardData && clipboardData.setData) {
            clipboardData.setData("text", data);
        } else {
            var hiddentextarea = me.getHiddenTextArea(grid);
            hiddentextarea.dom.value = data;
            hiddentextarea.focus();
            hiddentextarea.dom.setSelectionRange(0, hiddentextarea.dom.value.length);
        }
    },

    pasteFromClipBoard: function (grid) {
        hiddentextarea = this.getHiddenTextArea(grid);
        hiddentextarea.dom.value = "";
        hiddentextarea.focus();
    },

    updateGridData: function (e, t, grid) {
        if (!e.ctrlKey) {
            return;
        }
        Ext.suspendLayouts();
        var me = this,
            eventEditFired = false,
            celleditingPlugin = grid.findPlugin('cellediting'),
            columns = _.filter(grid.columns, function (column) {
                return column.dataIndex == 'value' && column.hidden != false;
            }),
            column = (columns.length != 0) ? columns[0] : null,
            data = me.getHiddenTextArea(grid).getValue(),
            data = data.split("\n");


        if (grid.getSelectionModel().hasSelection()) {
            var selection = grid.getSelectionModel().getSelection()[0];
            for (var pos = 0; pos < data.length; pos++) {
                var value = data[pos].replace(RegExp('[^0-9\.]', 'g'), '');
                if (pos == data.length - 1 && value == '') { // ignore last empty row
                    continue;
                }

                var rec = grid.getStore().getAt(selection.index + pos);
                if (rec) {
                    if (rec.get(me.editColumnDataIndex) != value) {
                        rec.set(me.editColumnDataIndex, value);
                        grid.fireEvent('paste', grid, {record: rec, column: column});
                        !eventEditFired && celleditingPlugin.fireEvent('edit', celleditingPlugin, {record: rec, column: column});
                        eventEditFired = true;
                    }
                }
            }
        }
        Ext.resumeLayouts();
    },

    collectGridData: function (grid, rows) {
        var me = this, data = [];
        if (grid.getSelectionModel().hasSelection()) {
            var selectionModel = grid.getSelectionModel();
            grid.getStore().each(function (record) {
                if (selectionModel.isSelected(record)) {
                    data.push(record.get(me.editColumnDataIndex));
                }
            });
        }
        return data.join('\n');
    },

    getHiddenTextArea: function (grid) {
        var me = this,
            hiddentextarea = me.hiddentextarea;

        if (!hiddentextarea) {
            hiddentextarea = me.hiddentextarea = new Ext.Element(document.createElement('textarea'));
            hiddentextarea.setStyle('position', 'absolute');
            hiddentextarea.setStyle('z-index', '-1');
            hiddentextarea.setStyle('width', '100px');
            hiddentextarea.setStyle('height', '30px');
            hiddentextarea.addListener('keyup', me.updateGridData, this, grid);
            Ext.get(grid.columns[0].getEl().dom.firstChild).appendChild(hiddentextarea.dom);
        }
        return hiddentextarea;
    }
});
