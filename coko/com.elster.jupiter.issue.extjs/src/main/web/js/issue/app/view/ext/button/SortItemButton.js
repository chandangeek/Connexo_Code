Ext.define('Isu.view.ext.button.SortItemButton', {
    extend: 'Ext.button.Split',
    alias: 'widget.sort-item-btn',
    name: 'sortitembtn',
    arrowCls: ' isu-icon-cancel isu-button-close isu-icon-white',
    iconCls: 'isu-icon-up-big isu-icon-white',
    sortOrder: 'asc',
    width: 150,
    split: true,
    menu: {}
});
