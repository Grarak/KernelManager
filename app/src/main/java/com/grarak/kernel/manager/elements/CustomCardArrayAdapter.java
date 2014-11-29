package com.grarak.kernel.manager.elements;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardView;

/**
 * Created by grarak on 12.10.14.
 */
public class CustomCardArrayAdapter extends CardArrayAdapter {

    private int count = 0;

    public CustomCardArrayAdapter(Context context, List<Card> cards) {
        super(context, cards);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        CardView mCardView = (CardView) view.findViewById(it.gmariotti.cardslib.library.R.id.list_cardId);

        if (count < getCount()) {
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

            Animation animation = new TranslateAnimation(0, 0, metrics.heightPixels / 3, 0);
            animation.setDuration(250 * (count + 1));
            mCardView.startAnimation(animation);
            count++;
        }

        return view;
    }

    public void notifyDataSetChanged(boolean reset) {
        if (reset) count = 0;
        super.notifyDataSetChanged();
    }

}
