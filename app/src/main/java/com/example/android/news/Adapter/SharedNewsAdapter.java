package com.example.android.news.Adapter;


import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.news.Download.Constants;
import com.example.android.news.Download.DownloadItemHelper;
import com.example.android.news.Download.DownloadRequestsSubscriber;
import com.example.android.news.Download.DownloadingStatus;
import com.example.android.news.Download.ItemDownloadPercentObserver;
import com.example.android.news.Download.RxDownloadManagerHelper;
import com.example.android.news.Download.SongDownloaderIconView;
import com.example.android.news.Interface.ItemClickListener;
import com.example.android.news.Interface.ItemDownloadCallback;
import com.example.android.news.Interface.ItemPercentCallback;
import com.example.android.news.Model.Emailed.EmailedResults;
import com.example.android.news.Model.Shared.SharedResult;
import com.example.android.news.R;
import com.example.android.news.Remote.DetailArticle;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

class SharedNewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private ItemClickListener itemClickListener;
    TextView article_title_shared;
    private SongDownloaderIconView imageDownloadIcon;
    RelativeTimeTextView article_time_shared;
    CircleImageView article_image_shared;
    CardView cardViewShared;
    private ItemDownloadCallback callback;
    private Context context;
    private SharedResult downloadableItem;
    private static final String DEBUG = "DebuggingLogs";

    public SharedNewsViewHolder(View itemView,Context context,ItemDownloadCallback callback) {
        super(itemView);
        if (itemView == null) {
            return;
        }
        article_image_shared = (CircleImageView) itemView.findViewById(R.id.article_image_shared);
        article_title_shared = (TextView) itemView.findViewById(R.id.article_title_shared);
        article_time_shared = (RelativeTimeTextView) itemView.findViewById(R.id.article_time_shared);
        cardViewShared = (CardView) itemView.findViewById(R.id.cardViewShared);
        imageDownloadIcon = (SongDownloaderIconView) itemView.findViewById(R.id.icon_image_download);
        imageDownloadIcon.init();
        imageDownloadIcon.setOnClickListener(this);
        this.context=context;
        this.callback=callback;
        itemView.setOnClickListener(this);
    }

    void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        Log.d(DEBUG,"class SharedNewsViewHolder - onClick() "+
                "imageDownloadIcon");
        DownloadingStatus downloadingStatus = imageDownloadIcon.getDownloadingStatus();
        //Only when the icon is in not downloaded state, then do the following.
        if (downloadingStatus == DownloadingStatus.NOT_DOWNLOADED) {
            setImageToWaitingState(downloadableItem.getPositionId());
            callback.onDownloadEnqueued(downloadableItem);
        }
    }

    public void updateImageDetails(SharedResult downloadableItem) {
        Log.d(DEBUG,"class SharedNewsViewHolder - updateImageDetails() ");
        this.downloadableItem = downloadableItem;
//        imageName.setText(downloadableItem.getItemTitle());
//        itemCoverIcon.setImageResource(downloadableItem.getItemCoverId());
        imageDownloadIcon.setItemId(downloadableItem.getPositionId());
        imageDownloadIcon.updateDownloadingStatus(downloadableItem.getDownloadingStatus());

        if (downloadableItem.getDownloadingStatus() == DownloadingStatus.DOWNLOADED) {
            setImageToCompletedState(downloadableItem.getPositionId());
        } else if (downloadableItem.getDownloadingStatus() == DownloadingStatus.IN_PROGRESS &&
                downloadableItem.getItemDownloadPercent()
                        == Constants.DOWNLOAD_COMPLETE_PERCENT) {
            setImageToCompletedState(downloadableItem.getPositionId());
            callback.onDownloadComplete();
        } else if (downloadableItem.getDownloadingStatus() == DownloadingStatus.IN_PROGRESS) {
            setImageInProgressState(downloadableItem.getItemDownloadPercent(), downloadableItem.getPositionId());
        }
    }

    public void setImageToWaitingState(String itemId) {
        if (!downloadableItem.getPositionId().equalsIgnoreCase(itemId)) {
            return;
        }
        Log.d(DEBUG,"class SharedNewsViewHolder - setImageToWaitingState() ");
        imageDownloadIcon.updateDownloadingStatus(DownloadingStatus.WAITING);
    }

    public void setImageToCompletedState(String itemId) {
        if (!downloadableItem.getPositionId().equalsIgnoreCase(itemId)) {
            return;
        }
        Log.d(DEBUG,"class SharedNewsViewHolder -setImageToCompletedState ");
        imageDownloadIcon.updateDownloadingStatus(DownloadingStatus.DOWNLOADED);
    }

    public void setImageInProgressState(int progress, String itemId) {
        if (!downloadableItem.getPositionId().equalsIgnoreCase(itemId)) {
            return;
        }
        Log.d(DEBUG,"class SharedNewsViewHolder -setImageInProgressState ");
        imageDownloadIcon.updateProgress(context, progress);
        imageDownloadIcon.updateDownloadingStatus(DownloadingStatus.IN_PROGRESS);
    }
}

public class SharedNewsAdapter extends RecyclerView.Adapter<SharedNewsViewHolder> implements ItemDownloadCallback,ItemPercentCallback {
    private List<SharedResult> itemsList;
    private int currentDownloadsCount = 0;
    private DownloadManager downloadManager;
    private static final String TAG = SharedNewsAdapter.class.getSimpleName();
    private static final String DEBUG = "DebuggingLogs";
    private ItemDownloadPercentObserver mItemDownloadPercentObserver;
    private DownloadRequestsSubscriber mDownloadRequestsSubscriber;
    private WeakReference<Context>contextWeakReference;
    private RecyclerView recyclerView;

    public SharedNewsAdapter(Context context, List<SharedResult> downloadableItemList, RecyclerView recyclerView) {
        this.itemsList = downloadableItemList;
        this.downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.contextWeakReference = new WeakReference(context);
        this.recyclerView = recyclerView;

        //Observable for percent of individual downloads.
        mItemDownloadPercentObserver = new ItemDownloadPercentObserver(this);
        //Observable for download request
        mDownloadRequestsSubscriber = new DownloadRequestsSubscriber(this);
    }

    @Override
    public SharedNewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.news_layout_shared, parent, false);
        SharedNewsViewHolder itemDetailsViewHolder=new SharedNewsViewHolder(itemView,contextWeakReference.get(),this);
        return itemDetailsViewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(SharedNewsViewHolder holder, int position) {
        if(!(holder instanceof SharedNewsViewHolder)){
            return;
        }
        SharedResult downloadableItem = DownloadItemHelper.getItem(contextWeakReference.get(),itemsList.get(position));
        ((SharedNewsViewHolder) holder).updateImageDetails(downloadableItem);
        Picasso.get()
                .load(itemsList
                        .get(position).getMedia()
                        .get(0).getMediaMetadata()
                        .get(2).getUrl())
                .into(holder.article_image_shared);


        holder.article_title_shared.setText(itemsList.get(position).getTitle());

        String date = itemsList.get(position).getPublishedDate();
        holder.article_time_shared.setText(date);

        //set event click
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                Intent detail = new Intent(contextWeakReference.get(), DetailArticle.class);
                detail.putExtra("webURL", itemsList.get(position).getUrl());
                detail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                contextWeakReference.get().startActivity(detail);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (itemsList == null) {
            return 0;
        }
        return itemsList.size();
    }
//
//    public String getItemTitleTransaction(int position) {
//        return articleList.get(position).getTitle();
//    }
//
//    public String getItemImageUrlTransaction(int position) {
//        return articleList.get(position).getMedia()
//                .get(0).getMediaMetadata()
//                .get(2).getUrl();
//    }
//
//    public String getItemArticleUrlTransaction(int position) {
//        return articleList.get(position).getUrl();
//    }

//This callback is called when the user clicks on any item for download.
    @Override
    public void onDownloadEnqueued(SharedResult downloadableItem) {
        Log.d(DEBUG,"class SharedNewsAdapter - onDownloadEnqueued() "+
        "This callback is called when the user clicks on any item for download.");
        mDownloadRequestsSubscriber.emitNextItem(downloadableItem);
    }
//This callback is called when the item starts getting downloaded.
    @Override
    public void onDownloadStarted(SharedResult downloadableItem) {
        Log.d(DEBUG,"class SharedNewsAdapter - onDownloadStarted() "+
                "This callback is called when the item starts getting downloaded. "+
        "Increment the current number of downloads by 1");
        //Increment the current number of downloads by 1
        currentDownloadsCount++;
        String downloadUrl = downloadableItem.getUrl();
        long downloadId =
                RxDownloadManagerHelper.enqueueDownload(downloadManager, downloadUrl);
        if (downloadId == Constants.INVLALID_ID) {
            return;
        }
        downloadableItem.setDownloadId(downloadId);
        downloadableItem.setDownloadingStatus(DownloadingStatus.IN_PROGRESS);
        updateDownloadableItem(downloadableItem);
        RxDownloadManagerHelper.queryDownloadPercents(downloadManager, downloadableItem,
                mItemDownloadPercentObserver.getPercentageObservableEmitter());
    }

    @Override
    public void onDownloadComplete() {
        Log.d(DEBUG,"class SharedNewsAdapter - onDownloadComplete() "+
                "This callback is called when the item starts getting downloaded. "+
        "Decrement the current number of downloads by 1");
        currentDownloadsCount--;
        mDownloadRequestsSubscriber.requestSongs(Constants.MAX_COUNT_OF_SIMULTANEOUS_DOWNLOADS -
                currentDownloadsCount);

    }

    @Override
    public void updateDownloadableItem(SharedResult downloadableItem) {
        if (downloadableItem == null || contextWeakReference.get() == null) {
            return;
        }
        Log.d(DEBUG,"class SharedNewsAdapter - updateDownloadableItem() ");
        DownloadItemHelper.persistItemState(contextWeakReference.get(), downloadableItem);

        int position = Integer.parseInt(downloadableItem.getPositionId()) - 1;
        SharedNewsViewHolder itemDetailsViewHolder = (SharedNewsViewHolder)
                recyclerView.findViewHolderForLayoutPosition(position);

        //It means that the viewholder is not currently displayed.
        if (itemDetailsViewHolder == null) {
            if (downloadableItem.getItemDownloadPercent() == Constants.DOWNLOAD_COMPLETE_PERCENT) {
                downloadableItem.setDownloadingStatus(DownloadingStatus.DOWNLOADED);
                DownloadItemHelper.persistItemState(contextWeakReference.get(), downloadableItem);
                onDownloadComplete();
            }
            return;
        }
        itemDetailsViewHolder.updateImageDetails(downloadableItem);
    }
}
