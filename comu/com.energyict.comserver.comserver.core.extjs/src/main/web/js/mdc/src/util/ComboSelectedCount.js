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
                    text: Uni.I18n.translate('general.deselectAll','MDC','Deselect all'),
                    handler: function (btn, e) {
                        if (!allSelected) {
                            combo.select(combo.getStore().getRange());
                            combo.setSelectedCount(combo.getStore().getRange().length);
                            btn.setText('Deselect all');
                            allSelected = true;
                        } else {
                            combo.reset();
                            btn.setText('Select all');
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
            change: function (me) {
                var addBtn = Ext.ComponentQuery.query('comtaskCreateEdit comtaskCommand button[action=addCommand]')[0],
                    saveBtn = Ext.ComponentQuery.query('comtaskCreateEdit comtaskCommand button[action=saveCommand]')[0],
                    toolbarBtns = Ext.ComponentQuery.query('#selectAll'),
                    toolbarBtn;
                if (toolbarBtns.length > 1) {
                    toolbarBtns[0].destroy();
                    toolbarBtns.splice(0, 1);
                }
                toolbarBtn = toolbarBtns[0];
                if (addBtn && addBtn.isDisabled()) {
                    addBtn.enable();
                }
                if (saveBtn && saveBtn.isDisabled()) {
                    saveBtn.enable();
                }
                if (Ext.isEmpty(me.getValue())) {
                    toolbarBtn.setText('Select all');
                    allSelected = false;
                    if (addBtn) {
                        addBtn.disable();
                    }
                    if (saveBtn) {
                        saveBtn.disable();
                    }
                } else {
                    toolbarBtn.setText('Deselect all');
                    allSelected = true;
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

