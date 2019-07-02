Ext.define('Imt.processes.view.InsightProcessesSortingMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.processes-sorting-menu',
    shadow: false,
    border: false,
    plain: true,
    totalNumberOfItems: 1,

    items: [
        {
            text: Uni.I18n.translate('imt.process.title.processId', 'IMT', 'Process ID'),
            name: 'processId'
        }
    ]
});