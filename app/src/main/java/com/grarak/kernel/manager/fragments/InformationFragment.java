package com.grarak.kernel.manager.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grarak.kernel.manager.R;
import com.grarak.kernel.manager.elements.CustomCard.DescriptionCard;
import com.grarak.kernel.manager.elements.CustomCardArrayAdapter;
import com.grarak.kernel.manager.services.OTAService;
import com.grarak.kernel.manager.utils.Constants;
import com.grarak.kernel.manager.utils.JsonUtils.JsonDeviceArrays;

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

        getActivity().runOnUiThread(run);

        return listView;
    }

    private final Runnable run = new Runnable() {
        @Override
        public void run() {

            ArrayList<Card> cards = new ArrayList<Card>();

            mJsonDevicesArrays = new JsonDeviceArrays(mUtils.getDeviceAssetFile(getActivity()));

            DescriptionCard supportCard = new DescriptionCard(getActivity());
            supportCard.setTitle(getString(R.string.support));

            if (mJsonDevicesArrays.isSupported()) {
                supportCard.setDescription(getString(R.string.device_supported) + " (" + mUtils.getDeviceCodename() + ")");
                getActivity().startService(new Intent(getActivity(), OTAService.class));
            } else
                supportCard.setDescription(getString(R.string.device_not_supported) + " (" + mUtils.getDeviceCodename() + ")");

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
