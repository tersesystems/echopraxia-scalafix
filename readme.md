# Scalafix rules for Echopraxia

[Echopraxia](https://github.com/tersesystems/echopraxia-plusscala) has 

## Running

Most likely you will want to use the [sbt integration](https://scalacenter.github.io/scalafix/docs/users/installation.html) and do it from inside there, using the [external rules](https://scalacenter.github.io/scalafix/docs/rules/external-rules.html):

```
scalafixEnable
scalafix dependency:EchopraxiaWrapMethodWithLogger@com.tersesystems:echopraxia-scalafix:VERSION
```

## EchopraxiaWrapMethodWithLogger

This scalafix rule will wrap methods in a flow logger block, using a [flow or trace logger](https://github.com/tersesystems/echopraxia-plusscala#trace-and-flow-loggers).

Add the configuration to [`.scalafix.conf`](https://scalacenter.github.io/scalafix/docs/users/configuration.html)

```
// .scalafix.conf
rules = [
  EchopraxiaWrapMethodWithLogger
]

# The name of the logger variable to use, i.d. `flowLogger`
EchopraxiaWrapMethodWithLogger.loggerName = flowLogger

# The method call on the logger, i.e. `flowLogger.trace`
EchopraxiaWrapMethodWithLogger.loggerMethod = trace

# The access modifier to use for wrapping methods, by default only public methods are wrapped.
EchopraxiaWrapMethodWithLogger.methodAccess = public
```
