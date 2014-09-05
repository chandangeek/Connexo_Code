Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeSorting', {
    extend: 'Uni.view.panel.FilterToolbar',
    alias: 'widget.loadProfileTypeSorting',
    itemId: 'LoadProfileTypeSorting',
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