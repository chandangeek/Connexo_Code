Ext.define('Skyline.menu.NavigationMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.navigation-menu',
    cls: 'x-navigation-menu',
    defaults: {
        xtype: 'navigation-item'
    },
    floating: false,
    hidden: false,
    activeStep: 1,
    jumpBack: true,
    jumpForward: false,
    listeners: {
        add : function (menu, item, index) {
            item.renderData.index = item.index = ++index;
            this.updateItemCls(index)
        },
        click : function (menu, item) {
            item.index < menu.activeStep ?
                (menu.jumpBack ? menu.moveTo(item.index) : null) :
                (menu.jumpForward ? menu.moveTo(item.index) : null)
        }
    },

    updateItemCls: function (index) {
        var me = this,
            item = me.items.getAt(index - 1);
        item.removeCls(['step-completed', 'step-active', 'step-non-completed']);
        index < me.activeStep ? item.addCls('step-completed') :
            (index > me.activeStep ? item.addCls('step-non-completed') :
                item.addCls('step-active'));
    },

    moveTo: function (step) {
        var me = this;
        me.moveToStep(step);
        me.fireEvent('movetostep', me.activeStep)
    },

    moveToStep: function (step) {
        var me = this,
            stepCount = me.items.getCount();
        if ( 1 < step < stepCount) {
            me.activeStep = step;
            me.items.each(function (item) {
                var index = item.index;
                me.updateItemCls(index);
            });
        }
    },

    moveNextStep: function () {
        this.moveToStep(this.activeStep + 1);
    },

    movePrevStep: function () {
        this.moveToStep(this.activeStep - 1);
    }
});