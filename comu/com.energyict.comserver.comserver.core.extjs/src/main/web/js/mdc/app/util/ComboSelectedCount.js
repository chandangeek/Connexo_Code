Ext.define('Mdc.util.ComboSelectedCount', {
    alias: 'plugin.selectedCount',
    init: function (combo) {
        var fl = combo.getFieldLabel(),
            allSelected = true,
            id = combo.getId() + '-toolbar-panel';

        Ext.apply(combo, {
            listConfig: {
                tpl: new Ext.XTemplate(
                    '<div id="' + id + '"></div><tpl for="."><div class="x-boundlist-item">{' + combo.displayField + '} <div class="chkbox"></div></div></tpl>'
                )
            }
        });
        var toolbar = Ext.create('Ext.toolbar.Toolbar', {
            items: [
                {
                    itemId: 'selectAll',
                    text: 'Deselect all',
                    handler: function (btn, e) {
                        if (!allSelected) {
                            combo.select(combo.getStore().getRange());
                            combo.setSelectedCount(combo.getStore().getRange().length);
                            btn.setText('Deselect all...');
                            allSelected = true;
                        } else {
                            combo.reset();
                            btn.setText('Select all...');
                            allSelected = false;
                        }
                        e.stopEvent();
                    }
                }
            ]
        });
        combo.on({
            select: function (me, records) {
                var len = records.length,
                    store = combo.getStore();
                combo.setSelectedCount(len);
            },
            change: function () {
                var addBtn = Ext.ComponentQuery.query('communication-tasks-edit communication-tasks-command button[action=addCommand]')[0],
                    saveBtn = Ext.ComponentQuery.query('communication-tasks-edit communication-tasks-command button[action=saveCommand]')[0];
                if (addBtn && addBtn.isDisabled()) {
                    addBtn.enable();
                }
                if (saveBtn && saveBtn.isDisabled()) {
                    saveBtn.enable();
                }
            },
            beforedeselect: function (me, record, index) {
                me.setFieldLabel(fl);
            },
            expand: {
                fn: function () {
                    var dropdown = Ext.get(id).dom.parentElement;
                    var container = Ext.DomHelper.insertBefore(dropdown, '<div id="' + id + '-container"></div>', true);
                    toolbar.render(container);
                },
                single: true
            }
        });
        combo.setSelectedCount = function (count) {
            combo.setFieldLabel(fl);
        }
    }
});

