package hristostefanov.minirxdemo

import io.reactivex.Observable
import io.reactivex.Observer
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.mockito.BDDMockito.then
import org.mockito.BDDMockito.times

/**
 * Rules to check:
 *
 * When re-subscribing to an observable property:
 * * must re-emit the last value
 * * must not re-subscribe to the source
 */
fun <S> testViewModelKeepsStateOfProperty(
    sourceObservableSpy: Observable<S>,
    propertyObservable: Observable<*>,
    viewModelInit: () -> Unit
) {
    val observer1 = propertyObservable.test()
    viewModelInit()
    // await the default + new emission, assert the value count cause awaitCount may timeout
    observer1.awaitCount(2).assertValueCount(2)
    val newValue = observer1.values()[1]

    // re-subscribe
    observer1.dispose()
    val observer2 = propertyObservable.test()

    observer2.awaitCount(1).assertValueCount(1)
    MatcherAssert.assertThat(observer2.values()[0], CoreMatchers.equalTo(newValue))

    then(sourceObservableSpy).should(times(1)).subscribe(any<Observer<S>>())
}
