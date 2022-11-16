# Scalafix rules for Echopraxia

[Echopraxia](https://github.com/tersesystems/echopraxia-plusscala) is a structured logging framework that can organize arguments into key/value fields using Scala's implicit type safe mapping system, without any additional import tax.

These [scalafix](https://scalacenter.github.io/scalafix/) rules are useful for adding flow loggers to methods, and rewriting logging statements that use string interpolation to use Echopraxia's FieldBuilder API.

## Installation

Add scalafix to `project/plugins.sbt`:

```scala
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.4")
```

## Running

If you want to include the scalafix rules as part of the project to run automatically:

Add echopraxia-scalafix to `build.sbt` and enable semanticDB:

```
ThisBuild / scalafixDependencies += "com.tersesystems.echopraxia" %% "scalafix" % VERSION
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
```

And then add rules to `.scalafix.conf` as per [configuration](https://scalacenter.github.io/scalafix/docs/users/configuration.html):

```hocon
rules = [
  EchopraxiaRewriteToStructured
]
```

Most likely you will want to use the [sbt integration](https://scalacenter.github.io/scalafix/docs/users/installation.html) and do it from inside there, using the [external rules](https://scalacenter.github.io/scalafix/docs/rules/external-rules.html):

## EchopraxiaRewriteToStructured

This scalafix rule will rewrite statements that use string interpolation to structured arguments.

### Running

To run immediately (without `build.sbt` changes):

```
scalafixEnable
scalafix dependency:EchopraxiaRewriteToStructured@com.tersesystems.echopraxia:scalafix:$VERSION
```

### Usage

Given a logging statement that uses string interpolation, the `EchopraxiaRewriteToStructured` rule will rewrite:

```scala
private val logger = com.tersesystems.echopraxia.plusscala.LoggerFactory.getLogger

final def someMethod: Unit = {
  val world = "world"
  val count = 2
  logger.info(s"hello $world there are $count args")
}
```

would be rewritten as:

```scala
private val logger = com.tersesystems.echopraxia.plusscala.LoggerFactory.getLogger

final def someMethod: Unit = {
  val world = "world"
  val count = 2
  logger.info("hello {} there are {} args", fb => fb.list(fb.value("world", world), fb.value("count", count)))
}
```

You can change the class of the logger as appropriate, if you have a custom logger.

```
// .scalafix.conf
rules = [
  EchopraxiaRewriteToStructured
]

EchopraxiaRewriteToStructured.loggerClass = MyLoggerClass
```

## EchopraxiaWrapMethodWithLogger

This scalafix rule will wrap methods in a flow logger block, using a [flow or trace logger](https://github.com/tersesystems/echopraxia-plusscala#trace-and-flow-loggers).

### Running

To run immediately (without `build.sbt` changes):

```
scalafixEnable
scalafix dependency:EchopraxiaWrapMethodWithLogger@com.tersesystems.echopraxia:scalafix:VERSION
```

### Usage

The given method:

```scala
object Main {
  private val flowLogger = FlowLoggerFactory.getLogger(getClass)

  def add(first: Int, second: Int): Int = {
    first + second
  }
}
```

would be rewritten as:

```scala
object Main {
  private val flowLogger = FlowLoggerFactory.getLogger(getClass)

  def add(first: Int, second: Int): Int = flowLogger.trace {
    first + second
  }
}
```

Add the configuration to [`.scalafix.conf`](https://scalacenter.github.io/scalafix/docs/users/configuration.html)

```
// .scalafix.conf
rules = [
  EchopraxiaWrapMethodWithLogger
]

# The name of the logger variable to use, i.e. `flowLogger`
EchopraxiaWrapMethodWithLogger.loggerName = flowLogger

# The method call on the logger, i.e. `flowLogger.trace`
EchopraxiaWrapMethodWithLogger.loggerMethod = trace

# The access modifier to use for wrapping methods, by default only public methods are wrapped.
EchopraxiaWrapMethodWithLogger.methodAccess = public
```
