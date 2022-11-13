# Scalafix rules for Echopraxia

[Echopraxia](https://github.com/tersesystems/echopraxia-plusscala) has 

## Running

Most likely you will want to use the [sbt integration](https://scalacenter.github.io/scalafix/docs/users/installation.html) and do it from inside there, using the [external rules](https://scalacenter.github.io/scalafix/docs/rules/external-rules.html):

## EchopraxiaRewriteToStructured

### Running

```
scalafixEnable
scalafix dependency:EchopraxiaRewriteToStructured@com.tersesystems.echopraxia:scalafix:VERSION
```

### Usage

This scalafix rule will rewrite statements that use string interpolation to structured arguments.

For example,

```scala
final def someMethod: Unit = {
  val world = "world"
  val count = 2
  logger.info(s"hello $world there are $count args")
}
```

to

```scala
final def someMethod: Unit = {
  val world = "world"
  val count = 2
  logger.info("hello {} there are {} args", fb => fb.list(fb.value("world", world), fb.value("count", count)))
}
```

You can change the name of the logger as appropriate:

```
// .scalafix.conf
rules = [
  EchopraxiaRewriteToStructured
]

# The name of the logger variable to use, i.e. `logger`
EchopraxiaRewriteToStructured.loggerName = log
```

## EchopraxiaWrapMethodWithLogger

### Running

```
scalafixEnable
scalafix dependency:EchopraxiaWrapMethodWithLogger@com.tersesystems.echopraxia:scalafix:VERSION
```

### Usage

This scalafix rule will wrap methods in a flow logger block, using a [flow or trace logger](https://github.com/tersesystems/echopraxia-plusscala#trace-and-flow-loggers).

For example, 

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
