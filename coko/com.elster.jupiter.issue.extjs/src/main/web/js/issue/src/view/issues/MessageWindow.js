Ext.define('Isu.view.issues.MessageWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.message-window',
    autoShow: true,
    resizable: false,
    bodyBorder: false,
    shadow: false,
    y: 10,
    animCollapse: true,
    border: false,
    cls: 'isu-msg-window',
    header: false,
    layout: {
        type: 'vbox',
        align: 'center'
    }
});