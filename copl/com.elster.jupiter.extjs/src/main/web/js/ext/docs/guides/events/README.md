# Using Events

The Components and Classes of Ext JS fire a broad range of events at various points 
in their lifecycle. Events allow your code to react to changes around your application, 
and are a key concept within Ext JS.

## What Are Events?

Events fire whenever something interesting happens to one of your Classes. 
For example, when a Component renders to the screen, Ext JS fires an 
event after the render completes. We can listen for that event 
by adding a simple `listeners` config:

    Ext.create('Ext.Panel', {
        html: 'My Panel',
        renderTo: Ext.getBody(),
        listeners: {
            afterrender: function() {
                Ext.Msg.alert('We have been rendered');
            }
        }
    });

In this example, when you click the <b>Live Preview</b> button, the Panel 
renders to the screen, followed by the defined alert message. All events 
fired by a class are listed in the class’s API page - for example,
`Ext.Panel` currently has 45 events.

## Listening to Events

While `afterrender` is useful in some cases, you may use other events 
more frequently. For instance, a `Button` fires click events 
when clicked:

    Ext.create('Ext.Button, {
        text: 'Click Me',
        renderTo: Ext.getBody(),
        listeners: {
            click: function() {
                Ext.Msg.alert('I was clicked!');
            }
        }
    });

A component may contain as many event listeners as needed. In the following example, 
we confound users by calling `this.hide()` inside our mouseover 
listener to hide a Button. We then display the button again a second later. 
When `this.hide()` is called, the Button is hidden and the `hide` 
event fires. The hide event triggers our `hide` listener, 
which waits one second and displays the Button again:

    Ext.create('Ext.Button', {
        renderTo: Ext.getBody(),
        text: 'My Button',
        listeners: {
            mouseover: function() {
                this.hide();
            },
            hide: function() {
                // Waits 1 second (1000ms), then shows the button again
                Ext.defer(function() {
                    this.show();
                }, 1000, this);
            }
        }
    });

Event listeners are called every time an event is fired, so you can continue hiding and 
showing the button for as long as you desire.

## Adding Listeners Later

In previous examples, we passed listeners to the component when the class was instantiated.
However, If we already have an instance, we can add listeners using the `.on` 
function:

    var button = Ext.create('Ext.Button', {
        renderTo: Ext.getBody(),
        text: 'My Button'
    });
    
    button.on('click', function() {
        Ext.Msg.alert('Event listener attached by .on');
    });

You can also specify multiple listeners by using the `.on` method, 
similar to using a listener configuration.  The following revisits 
the previous example that set the button’s visibility with a mouseover event:

    var button = Ext.create('Ext.Button', {
        renderTo: Ext.getBody(),
        text: 'My Button'
    });

    button.on({
        mouseover: function() {
            this.hide();
        },
        hide: function() {
            Ext.defer(function() {
                this.show();
            }, 1000, this);
        }
    });

## Removing Listeners

Just as we can add listeners at any time, we can also remove them. This time we use 
the `.un` function. To remove a listener, we need a reference to its function. 
In the previous examples, we passed a function into the listener’s 
object or the `.on` call. This time, we create the function earlier and link 
it into a variable called `doSomething`, which contains our custom function.

Because we initially pass the new `doSomething` function into our listeners 
object, the code begins as before. With the eventual addition of 
an `Ext.defer` function, clicking the button in the first 
3 seconds yields an alert message. However, after 3 
seconds the listener is removed so nothing happens:

    var doSomething = function() {
        Ext.Msg.alert('listener called');
    };

    var button = Ext.create('Ext.Button', {
        renderTo: Ext.getBody(),
        text: 'My Button',
        listeners: {
            click: doSomething,
        }
    });

    Ext.defer(function() {
        button.un('click', doSomething);
    }, 3000);

## Scope Listener Option

Scope sets the value of this inside your handler function. By default, this is set to the 
instance of the class firing the event.  This is often, but not always, the functionality 
that you want. This functionality allows us to 
call `this.hide()` to hide the button in the 
second example earlier in this guide.

In the following example, we create a Button and a Panel.  We then listen to the Button's 
click event with the handler running in Panel's scope. In order to do this, we need to 
pass in an object instead of a handler function.  This object contains the function AND 
the scope:

    var panel = Ext.create('Ext.Panel', {
        html: 'Panel HTML'
    });

    var button = Ext.create('Ext.Button', {
        renderTo: Ext.getBody(),
        text: 'Click Me'
    });

    button.on({
        click: {
            scope: panel,
            fn: function() {
                Ext.Msg.alert(this.getXType());
            }
        }
    });

When you run this example, the value of this in the click handler is the Panel. To see 
this illustrated, we  alert the xType of the scoped component. When the button is clicked, 
we should see the Panel xType being alerted.

## Listening to an Event Once

You may want to listen to one event only once. The event itself might fire any number of 
times, but we only want to listen to it once. The following codes illustrates this 
situation:

    var button = Ext.create('Ext.Button', {
        renderTo: Ext.getBody(),
        text: 'Click Me',
        listeners: {
            click: {
                single: true,
                fn: function() {
                    Ext.Msg.alert('I will say this only once');
                }
            }
        }
    });

## Using a Buffer Configuration

For events that fire many times in short succession, we can reduce the number of times our 
listener is called by using a buffer configuration. In this case our button's click 
listener is only invoked once every 2 seconds, regardless of how many times you click it:

    var button = Ext.create('Ext.Button', {
        renderTo: Ext.getBody(),
        text: 'Click Me',
        listeners: {
            click: {
                buffer: 200,
                fn: function() {
                    Ext.Msg.alert('I say this only once every 2 seconds');
                }
            }
        }
    });

## Firing Your Own Events

Firing your own events is done by calling `fireEvent` with 
an event name. In the following 
example we fire an event called myEvent that passes two arguments - the button itself and 
a random number between 1 and 100:

    var button = Ext.create('Ext.Button', {
        renderTo: Ext.getBody(),
        text: "Just wait 2 seconds",
        listeners: {
            myEvent: function(button, points) {
                Ext.Msg.alert('myEvent fired! You score ' + points + ' points');
            }
        }
    });

    Ext.defer(function() {
        var number = Math.ceil(Math.random() * 100);
    
        button.fireEvent('myEvent', button, number);
    }, 2000);

Once again we used `Ext.defer` to delay the function that 
fires our custom event, this time 
by 2 seconds. When the event fires, the `myEvent` listener 
picks up on it and displays the arguments we passed in.

## Listening for DOM Events

Any DOM element may have an event attached it to using `on` 
and targeting the component’s element.

Not every ExtJS component raises every event.  However, by targeting the 
container’s element, we can listen to DOM events very easily. In this
example, we want to listen the the `click` on a Component. But Components
don't provide a `click` event so we have to listen to its element named
"el".

    var component = Ext.create('Ext.Component', {
        renderTo: Ext.getBody(),
        html: 'Click Me!',
        listeners: {
            click: function(){
                Ext.Msg.alert('I have been clicked!');
            },
            element: 'el'
        }
    });
