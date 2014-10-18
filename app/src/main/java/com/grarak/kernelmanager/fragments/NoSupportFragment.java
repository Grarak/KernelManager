package com.grarak.kernelmanager.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grarak.kernelmanager.R;
import com.grarak.kernelmanager.elements.CustomCard.DescriptionCard;
import com.grarak.kernelmanager.elements.CustomCardArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Created by grarak on 16.10.14.
 */
public class NoSupportFragment extends Fragment {

    private CardListView listView;
    private final List<Card> cards = new ArrayList<Card>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        listView = new CardListView(getActivity());

        getActivity().runOnUiThread(run);

        return listView;
    }

    private final Runnable run = new Runnable() {
        @Override
        public void run() {
            cards.clear();

            DescriptionCard card = new DescriptionCard(getActivity());
            card.setTitle(getString(R.string.device_not_supported));
            card.setDescription(getString(R.string.device_not_supported_summary));

            cards.add(card);

            CustomCardArrayAdapter adapter = new CustomCardArrayAdapter(getActivity(), cards);
            listView.setAdapter(adapter);
        }
    };

}
