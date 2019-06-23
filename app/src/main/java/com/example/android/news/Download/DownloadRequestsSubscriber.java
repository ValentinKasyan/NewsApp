package com.example.android.news.Download;


import com.example.android.news.Model.Emailed.EmailedResults;

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
    // TODO: 23.06.2019   использовал только EmailedResults
    public void emitNextItem(EmailedResults downloadableItem) {
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
                // TODO: 23.06.2019   использовал только EmailedResults
                if (!(o instanceof EmailedResults)) {
                    return;
                }
                itemDownloadCallback.onDownloadStarted((EmailedResults) o);
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
