# Sencha Compiler Reference

One of the major components in [Sencha Cmd](http://www.sencha.com/products/sencha-cmd/)
is its compiler, which provides a JavaScript-to-JavaScript, framework-aware optimizer. 
The optimizer "understands" your high-level Ext JS and Sencha Touch code and produces the
smallest, most efficient code possible to support these high-level abstractions.

## Prerequisites

The following guides are recommended reading before proceeding further:

  - [Introduction to Sencha Cmd](#!/guide/command).
  - [Compiler-Friendly Code Guidelines](#!/guide/command_code)
  - [Using Sencha Cmd](#!/guide/command_app).

## Sets And The Current Set

Under the covers, the compiler manages a set of source files and analyzes these files to
determine their dependencies. The set of all files is determined by the `classpath`:

    sencha compile -classpath=common,app ...

In this example, the compiler recursively loads `"*.js"` from the specified list of folders.
This set of all files defines the basis for all operations to follow (that is, it defines
its "universe").

The default `classpath` used by the compiler comes from these configuration properties:

    ${framework.classpath},${workspace.classpath},${app.classpath}

When running high-level commands like `sencha app build`, Sencha Cmd will know the SDK in
use and load the appropriate configuration as well as include the `"src"` folder of that
framework in the `classpath`. When using the compiler outside of these contexts you will
probably need to use the `-sdk` switch:

    sencha -sdk /path/to/sdk compile -classpath=common,app ...

The compiler's output commands (for example, `concat` and `metadata`) operate on the set
of files called the "current set". The current set starts out equal to the universe of all
files, but this can be manipulated using the many commands provided to perform set
operations.

**Note** With the compiler, you often see rather long command lines using the `and` command
chaining mechanism. Also, in practical use cases, for long command lines, you should
consider using [Ant](#!/guide/command_ant) or a "response file". See
[Advanced Sencha Cmd](#!/guide/command_advanced). 

In this guide, all command lines are complete (and potentially long) to keep the examples
as clear as possible.

### Frameworks and The Classpath

Ultimately the compiler needs to reach the `"src"` folder of the desired SDK, but simply
adding this to the `-classpath` will not produce the optimal result. Instead, as shown in
the example above, the `-sdk` switch should be used to inform the compiler which Sencha
framework is in use. This will enable the compiler to understand framework-specific things
like config properties that imply class dependencies (such as the `model` config on an
`Ext.data.Store`).

## Generating Output with `concat`

A compiler ultimately is all about writing useful output given some number of inputs. The
`concat` command concatenates the source for the current set of files in the
appropriate dependency order.

The one required parameter is `-out`, which indicates the name of the output file. 
Other options affect the generated file. You can pick one of the
following options for compression:

 * `-compress` - Compresses the generated file using the default compressor. Currently this
 is the same as `-yui`.
 * `-closure` - Compresses the generated file using the [Google Closure Compiler](https://developers.google.com/closure/compiler/).
 * `-yui` - Compresses the source file using the [YUI Compressor](http://developer.yahoo.com/yui/compressor/).
 * `-strip` - Strips comments from the output file, but preserves whitespace. This 
 option converts "ext-all-debug-w-comments.js" into "ext-all-debug.js".

The following command produces three flavors of output from a single read of the source.

    sencha -sdk sdk compile \
        exclude -namespace Ext.chart and \
        concat ext-all-nocharts-debug-w-comments.js and \
        -debug=true \
        concat -strip ext-all-nocharts-debug.js and \
        -debug=false \
        concat -yui ext-all-nocharts.js

### Generating Metadata

The compiler can also generate metadata in many useful ways, for example, the names of
all source files, the set of files in dependency order, etc. To see what is available,
see the [Generating Metadata](#!/guide/command_compiler_meta) guide.

## Saving And Restoring Sets

When you need to produce multiple output files, it can be very helpful to save the
current set for later use:

    sencha -sdk sdk compile \
        exclude -namespace Ext.chart and \
        save nocharts and \
        ...
        restore nocharts and \
        ...

`The `save` command takes a snapshot of the current set and stores it under the given name
(`nocharts` in this example).

The simplest use of a saved set is with the `restore` command, which does the reverse and
restores the current set to its state at the time of the `save`.

## Set Operations

Many of the commands provided by the compiler are classified as set operations, which are
operations that work on and produce sets. In the case of the compiler, this means sets of
files or classes. Let's first take a look at set terminology.

### A Little Set Theory

There are three classic set operations:

 * Intersection - The intersection of two sets is a set containing only what was in both
 sets.
 {@img set-intersect.png}

 * Union - The union of two sets is a set containing whatever was in either of the sets.
{@img set-union.png}

 * Difference - The difference of two sets is the set of all things in the first set that
 are not in the second set.
 {@img set-difference.png}

### Set `include` and `exclude`

These two set operations are probably the most common (and flexible) set operations. Both
support these basic switches:

 * `-namespace` - Matches files that define types in the specified namespace.
 * `-class` - Matches a specific defined type.
 * `-file` - Matches filenames and/or folder names using Ant-style glob patterns (a "*"
 matches only filename characters, where "**" matches folders).
 * `-tag` - Matches any files with the specified tag(s) (see below).
 * `-set` - The files that are present in any of the specified named sets.

In all of these cases, the next command line argument is a list of match criteria
separated by commas. Also, a single `exclude` or `include` can have as many switch/value
pairs as needed.

So, let's start with a simple example and build an `"ext-all-no-charts-debug-w-comments.js"`.

    sencha -sdk sdk compile \
        exclude -namespace Ext.chart and \
        ...

What is happening here is that we started with only the Ext JS sources (in `"sdk/src"`) and
they were all part of the "current set". We then performed a set difference by excluding
all files in the `Ext.chart` namespace. The current set was then equivalent to `"ext-all.js"`
but without use of the Chart package.

### Negating `include` and `exclude` with `-not`

Both `include` and `exclude` support a rich set of matching criteria. This is rounded out
by the `-not` switch, which negates the matching criteria that follows it. This means that
the files included or excluded are all those that do not match the criteria.

For example:

    sencha -sdk sdk compile -classpath=js \
        ... \
        exclude -not -namespace Ext and \
        ...

The `exclude` command excludes from the current set any classes that are not in
the `Ext` namespace.

### The `all` Set

In some cases, it is very handy to restore the current set to all files or to the empty
set. To do this, simply use `include` or `exclude` with the `-all` switch. To build
on the previous example:

    sencha -sdk sdk compile -classpath=js \
        ... \
        include -all and \
        ... \
        exclude -all and \
        ...

After the `include -all`, the current set is all files. After `exclude -all` it is the
empty set.

### Union

As shown already, the `include` command is a form of set union: it performs a union of
the current set with the set of matching files. Sometimes it is desirable to not include
the current set in the union and only include those file matching the desired criteria. This is
what the `union` command does.

The `union` command has all of the options of `include`. Consider this `union` command:

    sencha -sdk sdk compile -classpath=js ... and \
        union -namespace Ext.grid,Ext.chart and \
        ...

It is exactly equivalent to this pair of `exclude` and `include` commands:

    sencha -sdk sdk compile -classpath=js ... and \
        exclude -all and \
        include -namespace Ext.grid,Ext.chart and \
        ...

### Transitivity/Recursive Union

One of the most important set operations is the union of all files explicitly specified
and all of the files they require. This is the core of a build process, since this is
how you select only the set of files you need. So, if you have a small set of top-level
files to start the process, say the class `MyApp.App`, you can use:

    sencha -sdk sdk compile -classpath=app \
        union -r -class MyApp.App and \
        ...

The `union` command starts with no current set, includes only the class `MyApp.App` and
then proceeds to include all the things it needs recursively. The resulting current set
is all files needed by the application.

### Intersect (Strict)

The `intersect` command is a bit less flexible in the criteria it supports: it only
accepts named sets (using `-set`).

    sencha -sdk sdk compile -classpath=common,page1/src,page2/src \
        ... \
        intersect -set page1,page2 and \
        ... \

This command intersects the two page sets and produces their intersection as the
current set.

### Intersect (Fuzzy)

When dealing with more than two sets, `intersect` has an option called `-min` that sets
the threshold for membership in the current set. This option is discussed in more detail
in [Multi-Page Ext JS Apps](#!/guide/command_app_multi).

For example,

    sencha compile ... \
        intersect -min=2 -set page1,page2,page3 and \
        ...

This use of `intersect` produces in the current set all files that are found in two of
the three sets specified.

## Compiler Directives

In many situations, it is helpful to embed metadata in files that only the compiler will
pick up. To do this, the compiler recognizes special line comments as directives. These
directives are single-line comments starting with a @-prefix word. For example:

    // @define Foo.bar.Thing

The list of directives is:

 * `// @charset`
 * `// @tag`
 * `// @define`
 * `// @require`

### Character Encoding

There is no standard way to specify the character encoding of a particular JS file. The
Sencha Cmd compiler, therefore, understands the following directive:

    // @charset ISO-9959-1

This must be the first line of the JS file. The value to the right of `charset` can be any
valid [Java charset](http://docs.oracle.com/javase/7/docs/api/java/nio/charset/Charset.html)
name. The default is "UTF-8".

The `charset` directive is used to describe the encoding of an input file to the compiler.
This does not affect the encoding of the output file. The content of the input file is
converted to Unicode internally.

### Tagging

In an ideal world, a namespace would be sufficient to define a set of interest. Sometimes,
however, a set can be quite arbitrary and even cross namespace boundaries. Rather than
move this issue to the command-line level, the compiler can track arbitrary tags in files.

Consider the example:

    // @tag foo,bar

This assigns the tags `foo` and `bar` to the file. These tags can be used in the `include`,
`exclude` and `union` commands with their `-tag` option.

### Dealing with "Other" JavaScript Files

In some cases, JavaScript files define classes or objects and require classes or objects
that are not expressed in terms of `Ext.define` and `requires` or `Ext.require`. Using
`Ext.define` you can still say that a class `requires` such things and the dynamic loader
will not complain so long as those things exist (if they do not exist, the loader will
try to load them, which will most likely fail).

To support arbitrary JavaScript approaches to defining and requiring types, the compiler
also provides these directives:

    // @define Foo.bar.Thing
    // @requires Bar.foo.Stuff

These directives set up the same basic metadata in the compiler that tracks what file
defines a type and what types that a file requires. In most ways, then, these directives
accomplish the same thing as an `Ext.define` with a `requires` property.

You can use either of these directives in a file without using the other.

## Conditional Compilation

The compiler supports conditional compilation directives, such as:

    foo: function () {
        //<debug>
        if (sometest) {
            Ext.log.warn("Something is wrong...");
        }
        //</debug>

        this.bar();
    }

This may be the most useful of the conditional directives, which you can use for code that
you want to run in a development environment but not in production. 

**Important** When you use conditional compilation, remember that unless you always run
compiled code, the directives are just comments and the conditional code will be "live"
during development.

### The debug directive

When compiling, by default, none of the preprocessor statements are examined. So in this
case, the result is development mode. If we switch on `-debug` we get a very similar
result, but with the preprocessor active. In fact, the only difference is that the
preprocessor directives are removed.

For example, this command:

    sencha compile -classpath=... \
        -debug \
        ...

Generates code like this:

    foo: function () {
        if (sometest) {
            Ext.log.warn("Something is wrong...");
        }

        this.bar();
    }

However, this command:

    sencha compile -classpath=... \
        -debug=false \
        ...

Generates code like this:

    foo: function () {
        this.bar();
    }

You can see that the `if` test and the log statement are both removed.

### The if directive

The most general directive is `if`. The `if` directive tests one or more configured
options against the attributes of the directive and removes the code in the block
if any are false.

For example:

    //<if debug>
    //</if>

This is equivalent to the `<debug>` directive.
