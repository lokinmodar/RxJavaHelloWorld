import io.reactivex.*;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observers.ResourceObserver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;


/* Working with the codes from the book Learning RxJava by Thomas Nield
    Adding my comments where i see fit for learning purposes
 */
public class LauncherChapter2 {
    private static final CompositeDisposable disposables
            = new CompositeDisposable();
    private static int start = 1;
    private static int count = 5;

    public static void main(String[] args) {

        //using .create, we can pass the attributes to onNext() and finish with onComplete();
        //useful when pulling non-reactive items into the Observable
        Observable<String> source = Observable.create(emitter -> {
            emitter.onNext("Alpha");
            emitter.onNext("Beta");
            emitter.onNext("Gamma");
            emitter.onNext("Delta");
            emitter.onNext("Epsilon");
            emitter.onComplete();
        });
        source.subscribe(s -> System.out.println("RECEIVED " + s));

        //implementing error handling:

        Observable<String> newSource = Observable.create(emitter -> {
            try {
                emitter.onNext("Alpha");
                emitter.onNext("Beta");
                emitter.onNext("Gamma");
                emitter.onNext("Delta");
                emitter.onNext("Epsilon");
                emitter.onComplete();
            } catch (Throwable e) {
                emitter.onError(e);
            }
        });
        newSource.subscribe(s -> System.out.println("RECEIVED " + s),
                Throwable::printStackTrace); //prints or throws printing of the error
        //Observables can push to other operators serving as next step of the chain
        //using derivatives map() and filter() to manipulate the output data
        Observable<Integer> lengths = newSource.map(String::length);

        Observable<Integer> filtered = lengths.filter(i -> i >= 5);

        filtered.subscribe(s -> System.out.println("RECEIVED " + s));

        //we can avoid using two different variables to handle mapping and filtering
        //map and filter yield new Observables so it is possible to use one variable

        Observable<String> nextSource = Observable.create(emitter -> {
            try {
                emitter.onNext("Alpha");
                emitter.onNext("Beta");
                emitter.onNext("Gamma");
                emitter.onNext("Delta");
                emitter.onNext("Epsilon");
                emitter.onComplete();
            } catch (Throwable e) {
                emitter.onError(e);
            }
        });
        nextSource.map(String::length)
                .filter(i -> i >= 5)
                .subscribe(s -> System.out.println("RECEIVED " + s));


        //Instead of Observable.create(), we can use Observable.just() to pass all items when using Observables
        //we call onComplete() when all of them have been pushed

        Observable<String> reSource = Observable.just("Alpha", "Beta", "Gamma",
                "Delta", "Epsilon");

        reSource.map(String::length)
                .filter(i -> i >= 5)
                .subscribe(s -> System.out.println("RECEIVED " + s));

        //Observable.fromIterable() allows us to use values in any object that implements Iterable

        List<String> items = Arrays.asList("Alpha", "Beta", "Gamma",
                "Delta", "Epsilon");

        Observable<String> reNewSource = Observable.fromIterable(items);
        reNewSource.map(String::length)
                .filter(i -> i >= 5)
                .subscribe(s -> System.out.println("RECEIVED " + s));


        //Implementing Observer Interface methods

        //we can do any operation when receiving the items on onNext method
        //Treat the errors on onError()
        //Execute tasks on onComplete()... etc;
        Observable<String> newReSource = Observable.just("Alpha", "Beta", "Gamma",
                "Delta", "Epsilon");
        Observer<Integer> myObserver = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                //do nothing with Disposable for now
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("RECEBIDO: " + integer);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("Done!");
            }
        };

        newReSource.map(String::length).filter(i -> i >= 5)
                .subscribe(myObserver);

        //Implementing methods as lambdas!

        Consumer<Integer> onNext = i -> System.out.println("Recebido: " + i);
        Action onComplete = () -> System.out.println("Done!");
        Consumer<Throwable> onError = Throwable::printStackTrace;

        newReSource.map(String::length).filter(i -> i >= 5)
                .subscribe(onNext, onError, onComplete);

        //another way: putting the lambdas inside the subscribe()
        //much less boilerplate code
        newReSource.map(String::length).filter(i -> i >= 5)
                .subscribe(i -> System.out.println("Rec: " + i),
                        Throwable::printStackTrace,
                        () -> System.out.println("Done!"));

        //you can omit methods in subscribe()
        //here i omit onComplete()

        newReSource.map(String::length).filter(i -> i >= 5)
                .subscribe(i -> System.out.println("Recb: " + i),
                        Throwable::printStackTrace);
        //and here i omit both onError and onComplete:
        //omitting onError should be avoided!
        newReSource.map(String::length).filter(i -> i >= 5)
                .subscribe(i -> System.out.println("Rece: " + i));

        //Multiple observers! (i already used this approach above
        //cold observables allow more than one observer to subscribe
        // in general, cold observables are the ones with sources that emit finite datasets
        //the Observable will emit all items to the first Observer than will emit all items to the next and so on

        newReSource.subscribe(s -> System.out.println("Observer 1 received: " + s));
        newReSource.subscribe(s -> System.out.println("Observer 2 received: " + s));

        //if one observer uses transformation operators, it does not affect the second one
        //first observer
        newReSource.subscribe(s -> System.out.println("Observer 1 Received: " + s));
        //second observer
        newReSource.map(String::length).filter(i -> i >= 5)
                .subscribe(s -> System.out.println("Observer 2 Received: " +
                        s));

        //ConnectableObservable: makes all emissions to be played all at once to all the Observers
        //works with cold observables as well
        //The concept here is called Multicasting!

        ConnectableObservable<String> conSource =
                Observable.just("Alpha", "Beta", "Gamma",
                        "Delta", "Epsilon").publish();//property needed!!!
        //Setting up obeservers
        conSource.subscribe(s -> System.out.println("Observer 01 received: " + s));
        conSource.map(String::length)
                .subscribe(s -> System.out.println("Observer 02 Received: " +
                        s));
        //Firing the ConnectableObservable
        conSource.connect();

        //Observable.range() emits a consecutive range of integers
        Observable.range(1, 30)//(starting value, how many emissions)
                .subscribe(System.out::println);
        //There is also a  rangeLong() operator that workis in a similar way

        //Observable.interval emits according to a set interval

        Observable.interval(1, TimeUnit.SECONDS)
                .subscribe(s -> System.out.println(s + " Mississippi"));
        sleep(6000);
        //this runs on the computation Scheduler thread.
        //we need to delay the end of the execution to see the results in main thread
        //it is a cold observable as we can see by adding a second observer to it
        Observable<Long> seconds = Observable.interval(1, TimeUnit.SECONDS);
        seconds.subscribe(s -> System.out.println("Observer 01: " + s));
        sleep(6000);
        seconds.subscribe(s -> System.out.println("Observer 02: " + s));
        sleep(6000);

        //we can use ConnectableObservable to put them working over the same emissions:

        ConnectableObservable<Long> seSeconds =
                Observable.interval(1, TimeUnit.SECONDS).publish();
        //1st observer
        seSeconds.subscribe(a -> System.out.println("Observer 1: " + a));
        seSeconds.connect();

        //sleep 6 seconds
        sleep(6000);

        //2nd observer
        seSeconds.subscribe(b -> System.out.println("Observer 2: " + b));

        //sleep 6 seconds
        sleep(6000);

        //Observable.empty() are the RxJava concept of null.
        //it just goes to onComplete()

        Observable<String> empty = Observable.empty();
        empty.subscribe(System.out::println, Throwable::printStackTrace,
                () -> System.out.println("Done!"));

        //Observable.never() never calls onComplete() so it is always left open for observers
        //but never gives any emission
        //mostly used for testing

        Observable<String> never = Observable.never();
        never.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done!"));
        sleep(6000);

        //Observable.error() is mostly used for testing as it immediately calls onError()

/*        Observable.error(new Exception("Crashed!"))//providing exception through lambda creates separate exception instances
                // Observable.error(() -> new Exception("Crashed!"))
                .subscribe(i -> System.out.println("Received: "+ i),
                        Throwable::printStackTrace,
                        () -> System.out.println("Done!"));*/


        //Observable.defer() used as a factory, it is capable to create a separate state for each Observer
        //often used when the Observable source is not capturing changes to the things driving it
        //allows using each value emitted
        Observable<Integer> source2 =
                Observable.defer(() -> Observable.range(start, count));
        source2.subscribe(i -> System.out.println("Observer - 1: " + i));

        //modify count
        count = 10;

        source2.subscribe(i -> System.out.println("Observer - 2: " + i));

        //Observable.fromCallable() helps emmiting errors and other info up in the Observable chain
        //Treating errors this way does not crash your app!

        Observable.fromCallable(() -> 1 / 0)
                .subscribe(i -> System.out.println("RECEIVED: " + i),
                        e -> System.out.println("Error Captured: " + e));

        // Single, Completable and Maybe
        //Single is an Observable that only emits a single item
        //it has a SingleObserver interface
        //onSuccess() consolidates onNext ans onComplete
        //Subscribing to it allows us to provide the lambdas for onSuccess and an optional onError.

        Single.just("Hello")
                .map(String::length)
                .subscribe(System.out::println,
                        Throwable::printStackTrace);//toObservable() allow us to turn a Single into an Observable if we need it.

        // Maybe just like Single, but it allows no emission to occur
        //MaybeObserver has onSuccess() instead of onNext()
        //Maybe will emit 0 or 1 emissions

        // has emission
        Maybe<Integer> presentSource = Maybe.just(100);
        presentSource.subscribe(s -> System.out.println("Process 1 received: " + s),
                Throwable::printStackTrace,
                () -> System.out.println("Process 1 done!"));
        //no emission
        Maybe<Integer> emptySource = Maybe.empty();
        emptySource.subscribe(s -> System.out.println("Process 2 received: " + s),
                Throwable::printStackTrace,
                () -> System.out.println("Process 2 done!"));

        //.firstElement() yields a Maybe too

        Observable<String> string =
                Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");
        string.firstElement().subscribe(
                s -> System.out.println("RECEIVED " + s),
                Throwable::printStackTrace,
                () -> System.out.println("Done!"));


        //Completable almost never used. Only has onError and onComplete
        //Does not receive any emissions


        Completable.fromRunnable(LauncherChapter2::runProcess)
                .subscribe(() -> System.out.println("Done!"));


        //------ Disposing ------
        //All finite Observables dispose of the resources used when hitting onComplete
        //But we cannot trust fully when using long finite tasks or infinite ones in the garbage collector
        // so we use dispose() to prevent memory leaks
        // Also used to stop tasks when needed
        // the Disposable interface links Observable and Observer

        Observable<Long> newSeconds =
                Observable.interval(1, TimeUnit.SECONDS);
        Disposable disposable =
                newSeconds.subscribe(l -> System.out.println("Received: " + l));
        //sleep 5 seconds
        sleep(5000);
        //dispose and stop emissions
        disposable.dispose();
        //sleep 5 seconds to prove there are no more emissions
        sleep(5000);

        //Handling a Disposable within an Observer
        Observer<Integer> meuObserver = new Observer<Integer>() {
            private Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                this.disposable = disposable; //gives all other methods access to disposable
                //
            }

            @Override
            public void onNext(Integer integer) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };

        /*Note that passing an Observer to the subscribe() method will be void and not return a
        Disposable since it is assumed that the Observer will handle it. If you do not want to
        explicitly handle the Disposable and want RxJava to handle it for you (which is probably
        a good idea until you have reason to take control), you can extend ResourceObserver as
        your Observer, which uses a default Disposable handling. Pass this to subscribeWith()
        instead of subscribe(), and you will get the default Disposable returned:*/

        Observable<Long> obsource =
                Observable.interval(1, TimeUnit.SECONDS);
        ResourceObserver<Long> myObserver2 = new ResourceObserver<Long>() {
            @Override
            public void onNext(Long value) {
                System.out.println(value);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {

            }


        };

        //Capturing the Disposable
        Disposable disposable1 = obsource.subscribeWith(myObserver2);

        //CompositeSisposable - used when we have several susbcriptions and need to manage and dispose of them
        //Implements Disposable but hods a collection of disposables internally
        //we can add to it and dispose of them all at once (using add() or addAll())

        Observable<Long> reseconds =
                Observable.interval(1, TimeUnit.SECONDS);
        //subscribe and capture disposables
        Disposable disposable2 =
                seconds.subscribe(l -> System.out.println("Observer 1: " +
                        l));
        Disposable disposable3 =
                seconds.subscribe(l -> System.out.println("Observer 2: " +
                        l));
        //put both disposables into CompositeDisposable
        disposables.addAll(disposable1, disposable2);
        //sleep 5 seconds
        sleep(5000);
        //dispose all disposables
        disposables.dispose();
        //sleep 5 seconds to prove there are no more emissions
        sleep(5000);

        //Handling Disposal with Observable.create()
        //may be used when Observable.create() is returning a long-running or infinite Observable
        //we should check isDisposed() from ObservableEmitter to prevent work when subsctiption is no longer active


        Observable<Integer> origin =
                Observable.create(observableEmitter -> {
                    try {
                        for (int i = 0; i < 1000; i++) {
                            while (!observableEmitter.isDisposed()) {
                                observableEmitter.onNext(i);
                            }
                            if (observableEmitter.isDisposed())
                                return;
                        }
                        observableEmitter.onComplete();
                    } catch (Throwable e) {
                        observableEmitter.onError(e);
                    }
                });
        //TODO: get back on this topic more deeply later!

    }

    private static void runProcess() {
        //run process here
    }

    private static void sleep(long millis){
        try {
            Thread.sleep(millis);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }




}
