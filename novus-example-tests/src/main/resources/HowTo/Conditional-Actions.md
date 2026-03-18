## Say you want to perform some actions if and only if a locator is present

* The user will perform
* actions1 actions2 and actions3
* only when the locator is present/visible on UI
* else it will ignore

```java
actor.attemptsTo(Perform.actions(actions1, actions2, actions3).iff(locator).isPresent())
```

## Say you want to perform an action only if the locator is visible

* The user will
* click on locator
* if the locator is visible on the UI
* else it will ignore the Click action on the locator

```java
actor.attemptsTo(Click.on(locator).ifDisplayed());
actor.attemptsTo(Enter.text("abc").on(locator).ifDisplayed());
actor.attemptsTo(Type.text("abc").on(locator).ifDisplayed());
```
