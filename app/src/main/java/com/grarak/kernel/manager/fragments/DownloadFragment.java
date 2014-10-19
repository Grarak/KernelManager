package com.grarak.kernel.manager.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.grarak.kernel.manager.DownloadActivity;
import com.grarak.kernel.manager.MoreActivity;
import com.grarak.kernel.manager.R;
import com.grarak.kernel.manager.elements.CustomCard.CardButtonExpand;
import com.grarak.kernel.manager.elements.CustomCard.DescriptionCard;
import com.grarak.kernel.manager.elements.CustomCardArrayAdapter;
import com.grarak.kernel.manager.utils.Constants;
import com.grarak.kernel.manager.utils.JsonUtils.JsonDeviceArrays;
import com.grarak.kernel.manager.utils.JsonUtils.JsonListArrays;
import com.grarak.kernel.manager.utils.WebpageReaderTask;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Created by grarak on 12.10.14.
 */
public class DownloadFragment extends Fragment implements Constants {

    private JsonDeviceArrays mJsonDeviceArrays;

    private CardListView listView;
    private List<String> kernels = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        listView = new CardListView(getActivity());
        mJsonDeviceArrays = new JsonDeviceArrays(mUtils.getDeviceAssetFile(getActivity()));

        getActivity().runOnUiThread(run);

        return listView;
    }

    private final Runnable run = new Runnable() {
        @Override
        public void run() {

            ArrayList<Card> cards = new ArrayList<Card>();

            for (int i = 0; i < mJsonDeviceArrays.getDeviceKernels().length; i++) {
                kernels.add(mJsonDeviceArrays.getDeviceKernels()[i]);

                final DescriptionCard card = new DescriptionCard(getActivity());
                card.setTitle(mJsonDeviceArrays.getDeviceKernels()[i]);
                String description = mJsonDeviceArrays.getKernelDescription(mJsonDeviceArrays.getDeviceKernels()[i]);
                card.setDescription(description == null ? getString(R.string.no_description) : description);
                card.setId(i);

                CardButtonExpand cardExpand = new CardButtonExpand(getActivity());
                cardExpand.setButtonText1(getString(R.string.download));
                cardExpand.setButtonText2(getString(R.string.more));
                cardExpand.setOnButtonClickListener(new CardButtonExpand.OnButtonClickListener() {
                    @Override
                    public void onButton1Click() {
                        downloadJson(true, card.getCustomId());
                    }

                    @Override
                    public void onButton2Click() {
                        downloadJson(false, card.getCustomId());
                    }
                });
                card.setViewToClickToExpand(ViewToClickToExpand.builder().setupView(cardExpand.getCardView()));
                card.addCardExpand(cardExpand);

                cards.add(card);

            }

            CustomCardArrayAdapter mCardArrayAdapter = new CustomCardArrayAdapter(getActivity(), cards);

            if (listView != null) listView.setAdapter(mCardArrayAdapter);

        }
    };

    private void downloadJson(final boolean download, final int id) {

        final ProgressBar progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 24));
        progressBar.setIndeterminate(true);

        final FrameLayout decorView = (FrameLayout) getActivity().getWindow().getDecorView();
        decorView.addView(progressBar);

        final ViewTreeObserver observer = progressBar.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                progressBar.setY(decorView.findViewById(android.R.id.content).getY() - 14);
            }
        });

        new WebpageReaderTask(new WebpageReaderTask.WebpageReaderInterface() {
            @Override
            public void webpageResult(String raw, String html) {
                decorView.removeView(progressBar);
                if (download)
                    startDownload(mJsonDeviceArrays.getDeviceKernels()[id], raw, mJsonDeviceArrays.getKernelJson(kernels.get(id)));
                else startLog(raw, mJsonDeviceArrays.getKernelJson(kernels.get(id)));
            }
        }).execute(mJsonDeviceArrays.getKernelJson(kernels.get(id)));
    }

    private void startDownload(String kernel, String json, String link) {

        if (json.isEmpty()) {
            mUtils.toast(getActivity(), getString(R.string.no_connection));
            return;
        }

        Intent i = new Intent(getActivity(),
                DownloadActivity.class);
        Bundle args = new Bundle();
        args.putString(DownloadActivity.ARG_KERNEL_NAME, kernel);
        args.putString(DownloadActivity.ARG_JSON, json);
        args.putString(DownloadActivity.ARG_JSON_LINK, link);
        i.putExtras(args);

        startActivity(i);
    }

    private void startLog(String json, String link) {
        if (!new JsonListArrays(json).isUseable()) {
            mUtils.toast(getActivity(), getString(R.string.no_info_found));
            return;
        }

        if (json.isEmpty()) {
            mUtils.toast(getActivity(), getString(R.string.no_connection));
            return;
        }

        Intent i = new Intent(getActivity(),
                MoreActivity.class);
        Bundle args = new Bundle();
        args.putString(MoreActivity.ARG_JSON, json);
        args.putString(MoreActivity.ARG_JSON_LINK, link);
        i.putExtras(args);

        startActivity(i);
    }

}
