Ext.define('Isu.view.ext.button.Action', {
    extend: 'Ext.button.Split',
    alias: 'widget.action-btn',
    cls: 'isu-action-button-inactive',
    menuActiveCls: 'isu-action-button-active',
    iconCls: 'isu-action-icon',
    menuAlign: 'tr-br?',
    listeners: {
        click: {
            fn: function (btn, e) {
                btn.showMenu();
            }
        },
        menushow: {
            fn: function (btn, menu) {
                btn.removeCls('isu-action-button-inactive');
            }
        },
        menuhide: {
            fn: function (btn, menu) {
                btn.addCls('isu-action-button-inactive');
            }
        }
    }
});