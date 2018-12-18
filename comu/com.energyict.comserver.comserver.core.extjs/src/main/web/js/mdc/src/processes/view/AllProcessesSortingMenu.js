Ext.define('Mdc.processes.view.AllProcessesSortingMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.processes-sorting-menu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            text: Uni.I18n.translate('mdc.process.title.processId', 'MDC', 'Process ID'),
            name: 'processId'
        }
    ]
});