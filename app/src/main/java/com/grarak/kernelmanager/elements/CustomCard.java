package com.grarak.kernelmanager.elements;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.grarak.kernelmanager.R;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by grarak on 11.10.14.
 */
public class CustomCard {

    public static class DescriptionCard extends Card {

        private CardHeaderBackground header;
        private int id = 0;

        private TextView descriptionView;
        private String mDescription;

        public DescriptionCard(Context context) {
            super(context, R.layout.description_inner_card);

            header = new CardHeaderBackground(context);
            addCardHeader(header);
        }

        public void setTitle(String title) {
            if (header != null) header.setHeader(title);
        }

        public void setDescription(String description) {
            mDescription = description;
            if (descriptionView != null && mDescription != null)
                descriptionView.setText(mDescription);
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getCustomId() {
            return this.id;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            super.setupInnerViewElements(parent, view);

            descriptionView = (TextView) view.findViewById(R.id.card_description_text);
            if (mDescription != null) descriptionView.setText(mDescription);
        }

        public void setOnClickListener(final CustomOnCardClickListener onCardClickListener) {
            setOnClickListener(new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    if (onCardClickListener != null) onCardClickListener.onClick(id);
                }
            });
        }

        public void setCustomOnSwipeListener(final CustomOnSwipeListener onSwipeListener) {
            setOnSwipeListener(new OnSwipeListener() {
                @Override
                public void onSwipe(Card card) {
                    onSwipeListener.onSwipe(id);
                }
            });
        }

        public interface CustomOnCardClickListener {
            public void onClick(int id);
        }

        public interface CustomOnSwipeListener {
            public void onSwipe(int id);
        }

        @Override
        public void addCardExpand(CardExpand cardExpand) {
            super.addCardExpand(cardExpand);

            setOnClickListener(new CustomOnCardClickListener() {
                @Override
                public void onClick(int id) {
                    doToogleExpand();
                }
            });
        }
    }

    private static class CardHeaderBackground extends CardHeader {

        private TextView headerText;
        private String mHeader;

        public CardHeaderBackground(Context context) {
            super(context, R.layout.header_background_card);
        }

        public void setHeader(String header) {
            mHeader = header;
            if (headerText != null) headerText.setText(mHeader);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            headerText = (TextView) view.findViewById(R.id.card_header_text);
            if (mHeader != null) headerText.setText(mHeader);
        }
    }

    public static class CardButtonExpand extends CardExpand {

        private OnButtonClickListener onButtonClickListener;
        private Button mBtn1;
        private Button mBtn2;
        private String mBtnText1;
        private String mBtnText2;

        public CardButtonExpand(Context context) {
            super(context, R.layout.expand_button_card);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            mBtn1 = (Button) view.findViewById(R.id.card_expand_button1);
            mBtn2 = (Button) view.findViewById(R.id.card_expand_button2);

            if (mBtnText1 != null) mBtn1.setText(mBtnText1);
            if (mBtnText2 != null) mBtn2.setText(mBtnText2);

            setListener();
        }

        public void setButtonText1(String btn) {
            mBtnText1 = btn;
            if (mBtn1 != null && mBtnText1 != null) mBtn1.setText(mBtnText1);
        }

        public void setButtonText2(String btn) {
            mBtnText2 = btn;
            if (mBtn2 != null && mBtnText2 != null) mBtn2.setText(mBtnText2);
        }

        public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
            this.onButtonClickListener = onButtonClickListener;
            setListener();
        }

        private void setListener() {
            if (onButtonClickListener != null) {
                if (mBtn1 != null)
                    mBtn1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onButtonClickListener.onButton1Click();
                        }
                    });

                if (mBtn2 != null)
                    mBtn2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onButtonClickListener.onButton2Click();
                        }
                    });
            }
        }

        public interface OnButtonClickListener {
            public void onButton1Click();

            public void onButton2Click();
        }
    }

}
