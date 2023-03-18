package com.oreilly.rxjava.ch2;

import com.oreilly.rxjava.util.Sleeper;
import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;


public class Chapter2 {

    @Test
    public void sample_6() throws Exception {
        Observable<Tweet> tweets = Observable.empty(); //...

        tweets.subscribe((Tweet tweet) -> System.out.println(tweet));
    }

    @Test
    public void sample_17() throws Exception {
        Observable<Tweet> tweets = Observable.empty(); //...

        tweets.subscribe(
                (Tweet tweet) -> {
                    System.out.println(tweet);
                },
                (Throwable t) -> {
                    t.printStackTrace();
                }
        );
    }

    @Test
    public void sample_27() throws Exception {
        Observable<Tweet> tweets = Observable.empty(); //...

        tweets.subscribe(
                (Tweet tweet) -> {
                    System.out.println(tweet);
                },
                (Throwable t) -> {
                    t.printStackTrace();
                },
                () -> {
                    this.noMore();
                }
        );
    }

    @Test
    public void sample_38() throws Exception {
        Observable<Tweet> tweets = Observable.empty(); //...

        tweets.subscribe(
                System.out::println,
                Throwable::printStackTrace,
                this::noMore);
    }

    private void noMore() {
    }

    @Test
    public void sample_51() throws Exception {
        Observable<Tweet> tweets = Observable.empty(); //...

        Observer<Tweet> observer = new Observer<Tweet>() {
            @Override
            public void onNext(Tweet tweet) {
                System.out.println(tweet);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onCompleted() {
                noMore();
            }
        };

        //...

        tweets.subscribe(observer);
    }

    @Test
    public void sample_78() throws Exception {
        Observable<Tweet> tweets = Observable.empty(); //...

        Subscription subscription = tweets.subscribe(System.out::println);

        //...

        subscription.unsubscribe();
    }

    @Test
    public void sample_91() throws Exception {
        Observable<Tweet> tweets = Observable.empty(); //...

        Subscriber<Tweet> subscriber = new Subscriber<Tweet>() {
            @Override
            public void onNext(Tweet tweet) {
                if (tweet.getText().contains("Java")) {
                    unsubscribe();
                }
            }

            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };
        tweets.subscribe(subscriber);
    }

    private static void log(Object msg) {
        System.out.println(Thread.currentThread().getName() + ": " + msg);
    }

    @Test
    public void sample_117() throws Exception {
        log("Before");
        Observable
                .range(5, 3)
                .subscribe(Chapter2::log);
        log("After");

    }

    @Test
    public void sample_135() throws Exception {
        Observable<Integer> ints = Observable
                .create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        log("Create");
                        subscriber.onNext(5);
                        subscriber.onNext(6);
                        subscriber.onNext(7);
                        subscriber.onCompleted();
                        log("Completed");
                    }
                });
        log("Starting");
        ints.subscribe(i -> log("Element: " + i));
        log("Exit");
    }

    static <T> Observable<T> just(T x) {
        return Observable.create(subscriber -> {
                    subscriber.onNext(x);
                    subscriber.onCompleted();
                }
        );
    }

    @Test
    public void sample_162() throws Exception {
        Observable<Integer> ints =
                Observable.create(subscriber -> {
                            log("Create");
                            subscriber.onNext(42);
                            subscriber.onCompleted();
                        }
                );
        log("Starting");
        ints.subscribe(i -> log("Element A: " + i));
        ints.subscribe(i -> log("Element B: " + i));
        log("Exit");
    }

    @Test
    public void sample_177() throws Exception {
        Observable<Integer> ints =
                Observable.<Integer>create(subscriber -> {
                                    //...
                                }
                        )
                        .cache();
    }

    @Test
    public void sample_187() throws Exception {
        //BROKEN! Don't do this
        Observable<BigInteger> naturalNumbers = Observable.create(
                subscriber -> {
                    BigInteger i = ZERO;
                    while (true) {  //don't do this!
                        subscriber.onNext(i);
                        log(i);
                        i = i.add(ONE);
                    }
                });
        naturalNumbers.subscribe(x -> log(x));
    }

    // andrew added this method
    @Test
    public void sample_189() throws Exception {
        Observable<BigInteger> naturalNumbers = Observable.create(
                subscriber -> {
                    Runnable r = () -> {
                        BigInteger i = ZERO;
                        while (i.intValue() < 10) {
                            subscriber.onNext(i);
                            i = i.add(ONE);
                        }
                        subscriber.onCompleted();
                    };
                    new Thread(r).start();
                });
        naturalNumbers.subscribe(Chapter2::log);
        Sleeper.sleep(Duration.ofNanos(100));
        naturalNumbers.subscribe(Chapter2::log);
        Sleeper.sleep(Duration.ofMillis(1000));
        log(1000000000);
    }

    private Observable<BigInteger> naturalNumbers() {
        Observable<BigInteger> naturalNumbers = Observable.create(
                subscriber -> {
                    Runnable r = () -> {
                        BigInteger i = ZERO;
                        while (i.intValue() < 10) {
                            subscriber.onNext(i);
                            i = i.add(ONE);
                        }
                        log(Long.MIN_VALUE);
                        subscriber.onCompleted();
                    };
                    new Thread(r).start();
                });
        return naturalNumbers;
    }

    @Test
    public void sample_221() throws Exception {
        final Observable<BigInteger> naturalNumbers = naturalNumbers();
        naturalNumbers.subscribe(Chapter2::log);
//        Subscription subscription1 = naturalNumbers.subscribe(Chapter2::log);
//        Sleeper.sleep(Duration.ofNanos(10));
//        subscription1.unsubscribe();
        naturalNumbers.subscribe(Chapter2::log);
//        Subscription subscription2 = naturalNumbers.subscribe(Chapter2::log);
//        Sleeper.sleep(Duration.ofNanos(10));
//        subscription2.unsubscribe();
//        Sleeper.sleep(Duration.ofNanos(10));


        Sleeper.sleep(Duration.ofMillis(1000));
        log(Long.MAX_VALUE);
    }


    @Test
    public void sample_222() throws Exception {
        final Observable<Integer> delayed1 = delayed1(1);
        final Observable<Integer> delayed2 = delayed2(2);
        delayed1.subscribe(Chapter2::log);
        delayed2.subscribe(Chapter2::log);

        Sleeper.sleep(Duration.ofMillis(3000));
        log(Long.MAX_VALUE);
    }

    static <T> Observable<T> delayed1(T x) {
        return Observable.create(
                subscriber -> {
                    Runnable r = () -> {
                        sleep(2, SECONDS);
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(x);
                            subscriber.onCompleted();
                        }
                    };
                    new Thread(r).start();
                });
    }

    static void sleep(int timeout, TimeUnit unit) {
        try {
            long t1 = System.nanoTime();
            unit.sleep(timeout);
            long t2 = System.nanoTime();
            log("slept: " + (t2 - t1));
        } catch (InterruptedException ignored) {
            //intentionally ignored
        }
    }

    static <T> Observable<T> delayed2(T x) {
        return Observable.create(
                subscriber -> {
                    Runnable r = () -> {
                        sleep(2, SECONDS);
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(x);
                            subscriber.onCompleted();
                        }
                    };
                    final Thread thread = new Thread(r);
                    thread.start();
                    subscriber.add(Subscriptions.create(thread::interrupt)); // seems no effect
                });
    }

    Observable<Data> loadAll(Collection<Integer> ids) {
        return Observable.create(subscriber -> {
            ExecutorService pool = Executors.newFixedThreadPool(10);
            AtomicInteger countDown = new AtomicInteger(ids.size());
            //DANGER, violates Rx contract. Don't do this!
            ids.forEach(id -> pool.submit(() -> {
                final Data data = load(id);
                subscriber.onNext(data);
                if (countDown.decrementAndGet() == 0) {
                    pool.shutdownNow();
                    subscriber.onCompleted();
                }
            }));
        });
    }

    private Data load(Integer id) {
        return new Data();
    }

    Observable<Data> rxLoad(int id) {
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(load(id));
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    Observable<Data> rxLoad2(int id) {
        return Observable.fromCallable(() ->
                load(id));
    }

    @Test
    public void sample_304() throws Exception {
        Observable
                .timer(1, TimeUnit.SECONDS)
                .subscribe((Long zero) -> log(zero));
        Sleeper.sleep(Duration.ofSeconds(2));
    }

    @Test
    public void sample_311() throws Exception {
        Observable
                .interval(1_000_000 / 60, MICROSECONDS)
                .subscribe((Long i) -> log(i));
        Sleeper.sleep(Duration.ofSeconds(2));
    }
}