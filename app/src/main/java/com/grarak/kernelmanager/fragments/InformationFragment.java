package com.grarak.kernelmanager.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grarak.kernelmanager.R;
import com.grarak.kernelmanager.elements.CustomCard.DescriptionCard;
import com.grarak.kernelmanager.elements.CustomCardArrayAdapter;
import com.grarak.kernelmanager.utils.Constants;
import com.grarak.kernelmanager.utils.JsonUtils.JsonDeviceArrays;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Created by grarak on 11.10.14.
 */
public class InformationFragment extends Fragment implements Constants {

    private CardListView listView;
    private JsonDeviceArrays mJsonDevicesArrays;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        listView = new CardListView(getActivity());
        mJsonDevicesArrays = new JsonDeviceArrays(mUtils.getDeviceAssetFile(getActivity()));

        if (isAdded()) new Handler().postDelayed(run, 100);

        return listView;
    }

    private final Runnable run = new Runnable() {
        @Override
        public void run() {

            ArrayList<Card> cards = new ArrayList<Card>();

            DescriptionCard supportCard = new DescriptionCard(getActivity());
            supportCard.setTitle(getString(R.string.support));
            supportCard.setDescription(mJsonDevicesArrays.isSupported() ?
                    getString(R.string.device_supported) + " (" + mUtils.getDeviceCodename() + ")" :
                    getString(R.string.device_not_supported) + " (" + mUtils.getDeviceCodename() + ")");
            cards.add(supportCard);

            DescriptionCard kernelCard = new DescriptionCard(getActivity());
            kernelCard.setTitle(getString(R.string.kernel));
            kernelCard.setDescription(mKernelUtils.getKernelVersion());
            cards.add(kernelCard);

            DescriptionCard CpuCard = new DescriptionCard(getActivity());
            CpuCard.setTitle(getString(R.string.cpu));
            CpuCard.setDescription(mKernelUtils.getCpuInfo());
            cards.add(CpuCard);

            CustomCardArrayAdapter mCardArrayAdapter = new CustomCardArrayAdapter(getActivity(), cards);

            if (listView != null) listView.setAdapter(mCardArrayAdapter);

        }
    };

}
