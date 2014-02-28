Ext.define('Mtr.view.ext.button.SortItemButton', {
    extend: 'Ext.button.Split',
    alias: 'widget.sort-item-btn',
    name: 'sortitembtn',
    arrowCls: ' isu-icon-cancel-circled2 isu-button-close isu-icon-white',
    iconCls: 'isu-icon-up-big isu-icon-white',
    sortOrder: 'asc',
    width: 150,
    margin: '10 0 0 5',
    split: true,
    menu: {}
});
