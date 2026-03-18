## Say you want to perform some actions that is flaky it throws timeout error

## you want to retry that action max 3 times and if it fails, ignore that exception, perform some other action(not mandatory)

## and again retry

* Here the user attempts to perform
* action1 action2 and action3
* 3 times repetitively
* if one of the exceptions NovusActionException.class, TimeoutError.class occur, ignore them
* and perform someOtherAction1, someOtherAction2
* meanwhile wait for 10 seconds before trying action1 action2 and action3 again
* for more info check {@link Retry}

```java
Perform.actions(action1, action2, action3)
  .thrice()
  .ifExceptionOccurs(NovusActionException.class, TimeoutError.class) // if any other exceptions occur the loop will break
  .then(someOtherAction1, someOtherAction2)
  .meanwhile(() -> appUser.isWaitingFor(seconds(10)))
);
```