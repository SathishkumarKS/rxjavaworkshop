package com.ora;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;

import org.junit.Test;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CreatingBasicObservableTest {

    @Test
    public void testBasicObservable() {
        Observable<Integer> observable =
                Observable.create(e -> {
                    e.onNext(40);
                    e.onNext(50);
                    e.onComplete();
                });

        //1000s of lines code
        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("On Next:" + integer);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("On Error:" + e.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("On Complete!");
            }
        });
    }

    @Test
    public void testBasicObservableListeningWithFunctions() {
        Observable<Integer> observable =
                Observable.create(e -> {
                    e.onNext(40);
                    e.onNext(50);
                    e.onError(new RuntimeException("Another Q"));
                    e.onNext(70);
                    e.onComplete();
                });

        observable.subscribe(
                x -> {
                    System.out.println("onNext = " + x);

//                    if (x == 50) {
//                        throw new RuntimeException("Nooo");
//                    } else {
//                        System.out.println("x = " + x);
//                    }
                },
                Throwable::printStackTrace,
                () -> System.out.println("On Completed!"));
    }


    @Test
    public void testBasicObservableIfWeDontListenToErrors() {
        Observable<Integer> observable =
                Observable.create(e -> {
                    e.onNext(40);
                    e.onNext(50);
                    e.onError(new RuntimeException("Runtime Exception"));
                    e.onNext(70);
                    e.onComplete();
                });

        observable.subscribe(integer -> System.out.println("integer = " + integer));
    }

    @Test
    public void testUsingJust() {
        Observable.just(40, 50, 70)
                  .subscribe(System.out::println,
                          Throwable::printStackTrace,
                          () -> System.out.println("On Completed"));
    }

    @Test
    public void testInterval() throws InterruptedException {
        Observable.interval(1, TimeUnit.SECONDS)
                  .filter(aLong -> aLong % 2 == 0)
                  .map(aLong -> "Foo:" + (aLong + 2))
                  .subscribe(System.out::println,
                          Throwable::printStackTrace,
                          () -> System.out.println("On Completed"));
        Thread.sleep(10000);
    }


    @Test
    public void testFlatMap() throws InterruptedException {
        Observable<Integer> o1 = Observable.just(1, 4, 10);
        Observable<Integer> map = o1.flatMap(x -> Observable.just(-x, x, x+1));
        map.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("On Completed"));
        Thread.sleep(2000);
        System.out.println("-----");
        map.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("On Completed"));
        Thread.sleep(10000);
    }

    @Test
    public void testRealWorldScenario() {
        String[] arrayStocks = new String[]{"MSFT", "ORCL", "YHOO", "GOOG", "CRM"};
        Observable<String> stockObservable = Observable.fromArray(arrayStocks)
                                                       .startWith(Observable.fromArray("AAPL", "TWTR"));
        TickerPriceFinder tickerPriceFinder = TickerPriceFinder.create();
        Observable<Double> observable =
                stockObservable
                        .flatMap(stk ->  Observable.fromFuture(tickerPriceFinder.getPrice(stk)));
        observable.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("On Completed"));
    }

    @Test
    public void testReduce() throws InterruptedException {
        Maybe<Integer> result = Observable.range(1, 10)
                                          .doOnNext(System.out::println)
                                          .reduce(
                new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer total, Integer next) throws Exception {
                        System.out.format("total: %d, next: %d\r\n", total, next);
                        return total * next;
                    }
                });

        TestObserver test = result.test();

        result.subscribe(test);
        test.assertValues(3628800);
        Thread.sleep(4000);
    }

}




