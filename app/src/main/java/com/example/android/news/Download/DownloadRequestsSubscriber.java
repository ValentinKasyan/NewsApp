package com.example.android.news.Download;


import com.example.android.news.Interface.ItemDownloadCallback;
import com.example.android.news.Model.Emailed.EmailedResults;
import com.example.android.news.Model.Shared.SharedResult;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

public class DownloadRequestsSubscriber {
    private Subscription downloadRequestsSubscription;
    private FlowableEmitter downloadsFlowableEmitter;
    private ItemDownloadCallback itemDownloadCallback;


    public DownloadRequestsSubscriber(ItemDownloadCallback itemDownloadCallback) {
        this.itemDownloadCallback = itemDownloadCallback;
        FlowableOnSubscribe flowableOnSubscribe = new FlowableOnSubscribe() {
            @Override
            public void subscribe(FlowableEmitter e) throws Exception {
                downloadsFlowableEmitter = e;
            }
        };

        final Flowable flowable = Flowable.create(flowableOnSubscribe, BackpressureStrategy.BUFFER);
        final Subscriber subscriber = getSubscriber();
        flowable.subscribeWith(subscriber);
    }

    public void requestSongs(int number) {
        downloadRequestsSubscription.request(number);
    }

    public void emitNextItem(SharedResult downloadableItem) {
        downloadsFlowableEmitter.onNext(downloadableItem);
    }

    private Subscriber getSubscriber() {
        return new Subscriber() {
            @Override
            public void onSubscribe(Subscription s) {
                downloadRequestsSubscription = s;
                downloadRequestsSubscription.request(Constants.MAX_COUNT_OF_SIMULTANEOUS_DOWNLOADS);
            }

            @Override
            public void onNext(Object o) {
                if (!(o instanceof SharedResult)) {
                    return;
                }
                itemDownloadCallback.onDownloadStarted((SharedResult)o);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        };
    }

    public void performCleanUp() {
        if (downloadRequestsSubscription != null) {
            downloadRequestsSubscription.cancel();
        }
    }
}
