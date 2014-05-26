Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeSorting', {
    extend: 'Skyline.panel.FilterToolbar',
    alias: 'widget.loadProfileTypeSorting',
    title: 'Sort',
    name: 'sortitemspanel',
    height: 40,
    emptyText: 'None',
    tools: [
        {
            xtype: 'button',
            action: 'addSort',
            text: 'Add sort',
            menu: {
                name: 'addsortitemmenu'
            }
        }
    ]
});