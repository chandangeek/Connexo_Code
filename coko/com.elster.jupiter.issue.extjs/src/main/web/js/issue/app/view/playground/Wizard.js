Ext.define('Mtr.view.playground.Wizard', {
    extend: 'Uni.view.window.Wizard',

    title: 'Wizard title',
    description: 'Short wizard description',

    steps: [
        {
            title: 'Step #1',
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Content 1</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 1</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 1</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 1</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 1</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 1</h3>'
                }
            ]
        },
        {
            title: 'Step #2',
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Content 2</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 2</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 2</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 2</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 2</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 2</h3>'
                }
            ]
        },
        {
            title: 'Step #3',
            xtype: 'container',
            layout: 'vbox',
            items: [
                {
                    xtype: 'component',
                    html: '<h3>Content 3</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 3</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 3</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 3</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 3</h3>'
                },
                {
                    xtype: 'component',
                    html: '<h3>Content 3</h3>'
                }
            ]
        }
    ]
});