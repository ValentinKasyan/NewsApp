package com.example.android.news.Download;


import com.example.android.news.Model.Emailed.EmailedResults;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class ItemDownloadPercentObserver {
    private ObservableEmitter percentageObservableEmitter;
    private Disposable downloadPercentDisposable;
    private final ItemPercentCallback callback;

    public ItemDownloadPercentObserver(ItemPercentCallback callback) {
        this.callback=callback;
        ObservableOnSubscribe observableOnSubscribe = new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                percentageObservableEmitter = e;
            }
        };

        final Observable observable = Observable.create(observableOnSubscribe);

        final Observer subscriber = getObserver();
        observable.subscribeWith(subscriber);
    }

    public ObservableEmitter getPercentageObservableEmitter() {
        return percentageObservableEmitter;
    }

    private Observer getObserver() {
        return new Observer() {
            @Override
            public void onSubscribe(Disposable d) {
                downloadPercentDisposable = d;
            }

            @Override
            public void onNext(Object value) {
                // TODO: 23.06.2019   использовал только EmailedResults
                if (!(value instanceof EmailedResults)) {
                    return;
                }
                // TODO: 23.06.2019   использовал только EmailedResults
                callback.updateDownloadableItem((EmailedResults) value);
            }

            @Override
            public void onError(Throwable e) {
                if (downloadPercentDisposable != null) {
                    downloadPercentDisposable.dispose();
                }
            }

            @Override
            public void onComplete() {
                if (downloadPercentDisposable != null) {
                    downloadPercentDisposable.dispose();
                }
            }
        };
    }

    public void performCleanUp() {
        if (downloadPercentDisposable != null) {
            downloadPercentDisposable.dispose();
        }
    }
}
