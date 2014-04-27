Ext.define('Isu.view.workspace.issues.SortingToolbar', {
    extend: 'Skyline.panel.FilterToolbar',
    requires: [
        'Isu.view.ext.button.SortItemButton',
        'Isu.view.workspace.issues.SortMenu'
    ],
    alias: 'widget.sorting-toolbar',
    title: 'Sort',
    name: 'sortitemspanel',
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
    ],
//
//    items: [
//        {
//            xtype: 'component',
//            html: 'Sort',
//            cls: 'isu-toolbar-label',
//            width: 55
//        },
//        {
//            xtype: 'container',
//            layout: {
//                type: 'hbox',
//                align: 'middle'
//            },
//            flex: 1,
//            items: [
//                {
//                    xtype: 'container',
//                    name: 'actions-toolbar',
//                    layout: {
//                        type: 'hbox',
//                        align: 'middle'
//                    },
//                    defaults: {
//                        xtype: 'sort-item-btn',
//                        margin: '0 5 0 0'
//                    },
//                    listeners: {
//                        beforeadd: {
//                            fn: function (actionsToolbar, button) {
//                                var likeButton = actionsToolbar.down('button[sortName=' + button.sortName + ']'),
//                                    sortToolbar = actionsToolbar.up('sorting-toolbar'),
//                                    menuItem = sortToolbar.down('issue-sort-menu [action=' + button.sortName + ']'),
//                                    clearAllButton = sortToolbar.down('button[action=clearSort]');
//
//                                if (!menuItem) {
//                                    return;
//                                }
//
//                                likeButton && likeButton.destroy();
//                                button.text = menuItem.text;
//                                button.iconCls = button.sortDirection == 'desc' ? 'isu-icon-down-big isu-icon-white' : 'isu-icon-up-big isu-icon-white';
//                                menuItem.hide();
//                                clearAllButton.setDisabled(false);
//                            }
//                        }
//                    }
//                },
//                {
//                    xtype: 'button',
//                    text: '+ Add sort',
//                    action: 'addSort',
//                    menu: {
//                        xtype: 'issue-sort-menu'
//                    },
//                    handler: function (button) {
//                        !button.down('menu [hidden=false]') && button.menu.hide();
//                    }
//                }
//            ]
//        },
//        {
//            xtype: 'button',
//            text: 'Clear all',
//            action: 'clearSort',
//            disabled: true
//        }
//    ],

    addSortButtons: function (sortModel) {
        var container = this.getContainer(),
            data = sortModel.getData();

        Ext.Object.each(data, function (key, value) {
            key != 'id' && value && container.add({
                sortName: key,
                sortDirection: value
            });
        });
    }
});