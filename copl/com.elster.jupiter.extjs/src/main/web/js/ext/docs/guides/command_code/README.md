# Compiler-Friendly Code Guidelines

One of the major components in [Sencha Cmd](http://www.sencha.com/products/sencha-cmd/) 
is its compiler. This guide describes how to write code that gets the most out of the compiler
and prepares for future framework-aware optimizations.

## Prerequisites

The following guides are recommended reading before proceeding further:

  - [Introduction to Sencha Cmd](#!/guide/command).
  - [Using Sencha Cmd](#!/guide/command_app).

## What The Compiler Is Not

Sencha Cmd compiler is **not** a replacement for tools like these:

 * [YUI Compressor](http://developer.yahoo.com/yui/compressor/)
 * [Google Closure Compiler](https://developers.google.com/closure/compiler/)
 * [UglifyJS](https://github.com/mishoo/UglifyJS/)

These tools solve different problems for JavaScript developers and are
very good at the world of JavaScript, but have no understanding of Sencha framework features
such as `Ext.define` for declaring classes.

## Framework Awareness

The role of the Sencha Cmd compiler is to provide framework-aware optimizations and
diagnostics. Once code has passed through the Sencha Cmd compiler, it is ready 
for more general tools.

These kinds of optimizations have shown to significantly improve the "ingest" time of
JavaScript code by the browser, especially on legacy browsers.

For the compiler to provide these benefits, however, it is now important to look at the
coding conventions that the compiler can "understand" and therefore optimize for you.
Following the conventions described in this guide ensure that your code is positioned to
get the most from Sencha Cmd today and in the future.

## Code Organization

The dynamic loader and the previous JSBuilder have always made certain assumptions about
how classes are organized, but they were not seriously impacted by failure to follow those
guidelines. These guidelines are very similar to Java.

To recap, these guidelines are:

 * Each JavaScript source file should contain one `Ext.define` statement at global scope.
 * The name of a source file matches the last segment of the name of the defined type such as
 the name of the source file containing `Ext.define("MyApp.foo.bar.Thing", ...` is
 "Thing.js".
 * All source files are stored in a folder structure that is based on the namespace of the
 defined type. For example, given `Ext.define("MyApp.foo.bar.Thing", ...`, the source file
 is in a path ending with "/foo/bar".

Internally, the compiler views source files and classes as basically synonymous. It makes
no attempt to split up files to remove classes that are not required. Only complete files
are selected and included in the output. This means that if any class in a source file is
required, all classes in the file will be included in the output.

To give the compiler the freedom to select code at the class-level, it is essential to put
only one class in each file.

## Class Declaration

The Sencha Class System provides the `Ext.define` function to enable high-level, object
oriented programming. The compiler takes the view that `Ext.define` is really a form of
"declarative" programming and processes the "class declaration" accordingly.

Clearly if `Ext.define` is understood as a declaration, the content of the class body cannot
be constructed dynamically in code. While this practice is rare, it is valid JavaScript.
But as we shall see below in the code forms, this is antithetical to the compiler's
ability to understand the code it parses. Dynamic class declarations are often used to do
things that are better handled by other features of the compiler. For more on these
features, see the [Sencha Compiler Reference](#!/guide/command_compiler).

The compiler understands these "keywords" of this declarative language:

 * `requires`
 * `uses`
 * `extend`
 * `mixins`
 * `statics`
 * `alias`
 * `singleton`
 * `override`
 * `alternateClassName`
 * `xtype`

For the compiler to recognize your class declarations, they need to follow one of
the following forms.

### Standard Form

Most classes use simple declarations like this:

    Ext.define('Foo.bar.Thing', {
        // keywords go here ... such as:

        extend: '...',

        // ...
    });

The second argument is the class body which is processed by the compiler as the class
"declaration".

**Note** In all forms, call `Ext.define` at global scope. 

### Wrapped Function Forms

In some use cases the class declaration is wrapped in a function to create a closure scope
for the class methods. In all of the various forms, it is critical for the compiler that
the function end with a `return` statement that returns the class body as an object
literal. Other techniques are not recognized by the compiler.

#### Function Form

To streamline the older forms of this technique described below, `Ext.define` understands
that if given a function as its second argument, that it should invoke that function to
produce the class body. It also passes the reference to the class as the single argument
to facilitate access to static members via the closure scope. Internally to the framework,
this was the most common reason for the closure scope.

    Ext.define('Foo.bar.Thing', function (Thing) {

        return {
            // keywords go here ... such as:

            extend: '...',

            // ...
        };
    });

**Note:** This form is only supported in Ext JS 4.1.2 and later and Sencha Touch 2.1 and later.

#### Called Function Form

In previous releases, the "Function Form" was not supported, so the function was simply
invoked immediately:

    Ext.define('Foo.bar.Thing', function () {

        return {
            // keywords go here ... such as:

            extend: '...',

            // ...
        };
    }());

#### Called-Parenthesized Function Form

This form and the next are commonly used to appease tools like JSHint (or JSLint).

    Ext.define('Foo.bar.Thing', (function () {

        return {
            // keywords go here ... such as:

            extend: '...',

            // ...
        };
    })());

#### Parenthesized-Called Function Form

Another variation on immediately called "Function Form" to appease JSHint/JSLint.

    Ext.define('Foo.bar.Thing', (function () {

        return {
            // keywords go here ... such as:

            extend: '...',

            // ...
        };
    }()));

### Keywords

The class declaration in its many forms ultimately contains "keywords". Each keyword has
its own semantics, but there are many that have a common "shape".

#### Keywords using String

The `extend` and `override` keywords only accept a string literal.

These keywords are also mutually exclusive in that only one can be used in any declaration.

#### Keywords using String or String[]

The following keywords all have the same form:

 * `requires`
 * `uses`
 * `alias`
 * `alternateClassName`
 * `xtype`

The supported forms for these keywords are as follows.

Just a string:

    requires: 'Foo.thing.Bar',
    //...

An array of strings:

    requires: [ 'Foo.thing.Bar', 'Foo.other.Thing' ],
    //...

#### Forms of `mixins`

Using an object literal, the name given the mixin can be quoted or not:

    mixins: {
        name: 'Foo.bar.Mixin',
        'other': 'Foo.other.Mixin'
    },
    //...

Mixins can also be specified as a String[]:

    mixins: [
        'Foo.bar.Mixin',
        'Foo.other.Mixin'
    ],
    //...

This approach relies on the `mixinId` of the mixin class but also allows the receiving
class to control the mixin order. This is important if the mixins have overlapping
methods or properties and the receiving class wants to control which mixin supplies the
overlapping methods or properties.

#### The `statics` Keyword

This keyword places properties or methods on the class, as opposed to on each of
the instances. This must be an object literal.

    statics: {
        // members go here
    },
    // ...

#### The `singleton` Keyword

This keyword was historically only used with a boolean "true" value:

    singleton: true,

The following (redundant) use is also supported:

    singleton: false,

## Overrides

In Ext JS 4.1.0 and Sencha Touch 2.0, `Ext.define` gained the ability to manage overrides.
Historically, overrides have been used to patch code to work around bugs or add
enhancements. This use was complicated with the introduction of the dynamic loader because
of the timing required to execute the `Ext.override` method. Also, in large applications
with many overrides, not all overrides in the code base were needed by all pages or builds
(for example, if the target class was not required).

All this changed once the class system and loader understood overrides. This trend only
continues with Sencha Cmd. The compiler understands overrides and their dependency effects
and load-sequence issues.

In the future, the compiler will become even more aggressive at dead-code elimination of
methods replaced by an override. Using managed overrides as described below enables
this optimization of your code once it's available in Sencha Cmd.

### Standard Override Form

Below is the standard form of an override. The choice of namespace is somewhat arbitrary,
but see below for suggestions.

    Ext.define('MyApp.patches.grid.Panel', {
        override: 'Ext.grid.Panel',

        ...
    });

### Use Cases

With the ability to use `Ext.define` to manage overrides, new idioms have opened up and
are actively being leveraged. For example in the code generators of
[Sencha Architect](http://www.sencha.com/products/architect/) and internal to the framework,
that break apart large classes like `Ext.Element` into more manageable and cohesive pieces.

#### Overrides as Patches

Overrides as patches are the historical use case and hence the most common in practice today.

**Caution:** Take care when patching code. While the use of override itself is
supported, the end result of overriding framework methods is not supported. All overrides
should be carefully reviewed whenever upgrading to a new framework version.

That said, it is, at times, necessary to override framework methods. The most common case
for this to fix a bug. The Standard Override Form is ideal in this case. In fact, Sencha
Support will at times provide customer with patches in this form. Once provided, however,
managing such patches and removing them when no longer needed, is a matter for the review
process previously mentioned.

Naming Recommendation:

 * Organize patches in a namespace associated with the top-level namespace of the target.
 For example, "MyApp.patches" targets the "Ext" namespace. If third party code is involved
 then perhaps another level or namespace should be chosen to correspond to its top-level
 namespace. From there, name the override using a matching name and sub-namespace. In the
 previous example:

    (Ext -> MyApp.patches).grid.Panel

#### Overrides as Partial Classes

When dealing with code generation (as in Sencha Architect), it is common for a class to
consist of two parts: one machine generated and one human edited. In some languages, there
is formal support for the notion of a "partial class" or a class-in-two-parts.

Using an override, you can manage this cleanly:

In ./foo/bar/Thing.js:

    Ext.define('Foo.bar.Thing', {
        // NOTE: This class is generated - DO NOT EDIT...

        requires: [
            'Foo.bar.custom.Thing'
        ],

        method: function () {
            // some generated method
        },

        ...
    });

In ./foo/bar/custom/Thing.js:

    Ext.define('Foo.bar.custom.Thing', {
        override: 'Foo.bar.Thing',

        method: function () {
            this.callParent(); // calls generated method
            ...
        },

        ...
    });

Naming Recommendations:

 * Organize generated vs. hand-edited code by namespace.
 * If not by namespace, consider a common base name with a suffix on one or the other, such
 as "Foo.bar.ThingOverride" or "Foo.bar.ThingGenerated" so that the parts of a class
 collate together in listings.

#### Overrides as Aspects

A common problem for base classes in object-oriented designs is the "fat base class". This
happens because some behaviors apply across all classes. When these behaviors (or features)
are not needed, however, they cannot be readily removed if they are implemented as part of
some large base class.

Using overrides, these features can be collected in their own hierarchy and then `requires`
can be used to select these features when needed.

In ./foo/feature/Component.js:

    Ext.define('Foo.feature.Component', {
        override: 'Ext.Component',

        ...
    });

In ./foo/feature/grid/Panel.js:

    Ext.define('Foo.feature.grid.Panel', {
        override: 'Ext.grid.Panel',

        requires: [
            'Foo.feature.Component' // since overrides do not "extend" each other
        ],

        ...
    });

This feature can be used now by requiring it:

    ...
    requires: [
        'Foo.feature.grid.Panel'
    ]

Or with a proper "bootstrap" file (see 
[Workspaces in Sencha Cmd](#!/guide/command_workspace)
or [Single-Page Ext JS Apps](#!/guide/command_app_single)):

    ...
    requires: [
        'Foo.feature.*'
    ]

Naming Recommendation:

 * Organize generated vs. hand-edited code by namespace. This enables use of wildcards to
 bring in all aspects of the feature.

### Using `requires` and `uses` in an Override

These keywords are supported in overrides. Use of `requires` may limit the compiler's
ability to reorder the code of an override.

### Using `callParent` and `callSuper`

To support all of these new uses cases, `callParent` was enhanced in Ext JS 4.0 and Sencha
Touch 2.0 to "call the next method". The "next method" may be an overridden method or an
inherited method. As long as there is a next method, `callParent` will call it.

Another way to view this is that `callParent` works the same for all forms of `Ext.define`,
be they classes or overrides.

While this helped in some areas, it unfortunately made bypassing the original method (as a
patch or bug fix) more difficult. Ext JS 4.1 and later and Sencha Touch 2.1 and later
provides a method named `callSuper` that can bypass an overridden method.

In future releases, the compiler will use this semantic difference to perform dead-code
elimination of overridden methods.

### Override Compatibility

Starting in version 4.2.2, overrides can declare their `compatibility` based on the
framework version or on versions of other packages. This can be useful for selectively
applying patches that are safely ignored when they are incompatible with the target class
version.

The simplest use case is to test framework version for compatibility:

    Ext.define('App.overrides.grid.Panel', {
        override: 'Ext.grid.Panel',

        compatibility: '4.2.2', // only if framework version is 4.2.2

        //...
    });

An array is treated as an OR, so if any specs match, the override is compatible.

    Ext.define('App.overrides.some.Thing', {
        override: 'Foo.some.Thing',

        compatibility: [
            '4.2.2',
            'foo@1.0.1-1.0.2'
        ],

        //...
    });

To require that all specifications match, an object can be provided:
 
    Ext.define('App.overrides.some.Thing', {
        override: 'Foo.some.Thing',

        compatibility: {
            and: [
                '4.2.2',
                'foo@1.0.1-1.0.2'
            ]
        },

        //...
    });
 
Because the object form is just a recursive check, these can be nested:
 
    Ext.define('App.overrides.some.Thing', {
        override: 'Foo.some.Thing',

        compatibility: {
            and: [
                '4.2.2',  // exactly version 4.2.2 of the framework *AND*
                {
                    // either (or both) of these package specs:
                    or: [
                        'foo@1.0.1-1.0.2',
                        'bar@3.0+'
                    ]
                }
            ]
        },

        //...
    });

For details on version syntax, see the `checkVersion` method of `Ext.Version`.

## Conclusion

As Sencha Cmd continues to evolve, it continues to introduce new diagnostic messages
to help point out deviations from these guidelines.

A good place to start is to see how this information can help inform your own internal
code style guidelines and practices.

## Next Steps

 - [Sencha Compiler Reference](#!/guide/command_compiler)
 - [Advanced Sencha Cmd](#!/guide/command_advanced)
