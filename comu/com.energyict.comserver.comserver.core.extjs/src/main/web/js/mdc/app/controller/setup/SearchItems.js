Ext.define('Mdc.controller.setup.SearchItems', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Ext.ux.window.Notification'
    ],

    views: [
        'setup.searchitems.SearchItems',
        'setup.searchitems.ContentFilter',
        'setup.searchitems.ContentLayout',
        'setup.searchitems.SideFilter'
    ],

    stores: [
        //'Devices'
    ],

    refs: [
        //{ref: 'registerTypeGrid', selector: '#registertypegrid'}
    ],

    init: function () {
        this.control({
            '#searchItems breadcrumbTrail': {
                afterrender: this.showBreadCrumb
            },
            'items-sort-menu': {
                click: this.chooseSort
            },
            'button[name=clearitemssortbtn]': {
                click: this.clearSort
            },
            'button[name=clearitemsfilterbtn]': {
                click: this.clearCriteria
            },
            'button[name=sortbymridbtn]': {
                click: this.switchSort
            },
            'button[name=sortbysnbtn]': {
                click: this.switchSort
            },
            '#clearAllItems[action=clearfilter]': {
                click: this.clearAllItems
            },
            '#searchAllItems[action=applyfilter]': {
                click: this.searchAllItems
            }
        });
    },

    showSearchItems : function () {
        var widget = Ext.widget('searchItems');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },

    showBreadCrumb: function (breadcrumbs) {
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('searchItems.searchItems', 'MDC', 'Search items'),
            href: 'searchitems'
        });

        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        breadcrumbParent.setChild(breadcrumbChild);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    chooseSort: function (menu, item) {
        var sortBtnsPanel = menu.up('panel[name=sortpanel]').down('panel[name=sortitemsbtns]'),
            action = item.action;
        switch (action) {
            case 'sortbymrid':
                var button = sortBtnsPanel.down('button[name=sortbymridbtn]');
                if (Ext.isEmpty(button)) {
                    button = new Ext.Button({
                        text: item.text,
                        name: 'sortbymridbtn',
                        arrowCls: ' isu-icon-cancel isu-button-close isu-icon-white', //todo: remove isu classes form this btn
                        iconCls: 'isu-icon-up-big isu-icon-white',
                        sortOrder: '',
                        width: 150,
                        split: true,
                        menu: {}
                    });
                    sortBtnsPanel.add(button);
                    sortBtnsPanel.up('panel[name=sortpanel]').down('button[name=clearitemssortbtn]').setDisabled(false);
                }
                break;
            case 'sortbysn':
                var button = sortBtnsPanel.down('button[name=sortbysnbtn]');
                if (Ext.isEmpty(button)) {
                    button = new Ext.Button({
                        text: item.text,
                        name: 'sortbysnbtn',
                        arrowCls: ' isu-icon-cancel isu-button-close isu-icon-white', //todo: remove isu classes form this btn
                        iconCls: 'isu-icon-up-big isu-icon-white',
                        sortOrder: '',
                        width: 150,
                        split: true,
                        menu: {}
                    });
                    sortBtnsPanel.add(button);
                    sortBtnsPanel.up('panel[name=sortpanel]').down('button[name=clearitemssortbtn]').setDisabled(false);
                }
                break;
        }

    },

    clearSort: function (btn) {
        var sortBtnsPanel = btn.up('panel[name=sortpanel]').down('panel[name=sortitemsbtns]');
        sortBtnsPanel.items.each(function (btns) {
            btns.destroy();
        });
        btn.setDisabled(true);
    },

    clearCriteria: function (btn) {
        var searchItems = Ext.getCmp('search-items-id'),
            criteriaContainer = searchItems.down('container[name=filter]');
        criteriaContainer.items.each(function (btns) {
            btns.destroy();
        });
        btn.setDisabled(true);
    },

    switchSort: function (btn) {
        console.log('switch items sorting'); // todo: switch sort
    },

    clearAllItems: function(btn) {
        var searchItems = Ext.getCmp('search-items-id');
        searchItems.down('#mrid').setValue("");
        searchItems.down('#sn').setValue("");
        this.clearSort(searchItems.down('button[name=clearitemssortbtn]'));
        this.clearCriteria(searchItems.down('button[name=clearitemsfilterbtn]'));
    },

    searchAllItems: function(btn) {
        var searchItems = Ext.getCmp('search-items-id'),
            criteriaContainer = searchItems.down('container[name=filter]'),
            isCriteriaSet = false;
        if(searchItems.down('#mrid').getValue() != "") {
            var button = searchItems.down('button[name=criteriaMRIDbtn]');
            button = this.createCriteriaButton(button, 'criteriaMRIDbtn', Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID')+': '+searchItems.down('#mrid').getValue());
            criteriaContainer.add(button);
            isCriteriaSet = true;
        }
        if(searchItems.down('#sn').getValue() != "") {
            var button = searchItems.down('button[name=criteriaSerialNumberbtn]');
            button = this.createCriteriaButton(button, 'criteriaSerialNumberbtn', Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number')+': '+searchItems.down('#sn').getValue());
            criteriaContainer.add(button);
            isCriteriaSet = true;
        }
        if (isCriteriaSet == true) {
            searchItems.down('button[name=clearitemsfilterbtn]').setDisabled(false);
        }
    },

    createCriteriaButton: function(button, name, text) {
        if (Ext.isEmpty(button)) {
            button = new Ext.Button({
                text: text,
                name: name,
                arrowCls: ' isu-icon-cancel isu-button-close isu-icon-white', //todo: remove isu classes form this btn
                iconCls: 'isu-icon-up-big isu-icon-white',
                sortOrder: '',
                width: 150,
                split: true
            });
        } else {
            button.setText(text);
        }
        return button;
    }

});