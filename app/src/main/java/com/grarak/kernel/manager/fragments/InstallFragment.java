package com.grarak.kernel.manager.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grarak.kernel.manager.R;
import com.grarak.kernel.manager.elements.CustomCard.CardButtonExpand;
import com.grarak.kernel.manager.elements.CustomCard.DescriptionCard;
import com.grarak.kernel.manager.elements.CustomCardArrayAdapter;
import com.grarak.kernel.manager.utils.Constants;
import com.grarak.kernel.manager.utils.Utils;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardListView;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by grarak on 14.10.14.
 */
public class InstallFragment extends Fragment implements Constants {

    private boolean refresh = false;

    private ArrayList<Card> cards = new ArrayList<Card>();
    private CustomCardArrayAdapter mCardArrayAdapter;
    private PullToRefreshLayout mPullToRefreshLayout;
    private CardListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card_listview, container, false);
        listView = (CardListView) view.findViewById(R.id.listview);

        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        refresh();
                    }
                })
                .setup(mPullToRefreshLayout);

        getActivity().runOnUiThread(run);

        return view;
    }

    private void refresh() {
        refresh = true;
        mPullToRefreshLayout.setRefreshing(true);
        getActivity().runOnUiThread(run);
        mPullToRefreshLayout.setRefreshComplete();
    }

    private final Runnable run = new Runnable() {
        @Override
        public void run() {

            cards.clear();

            if (containsFiles()) {
                for (int i = 0; i < files().length; i++) {
                    final DescriptionCard card = new DescriptionCard(getActivity());
                    card.setTitle(files()[i].replace(".zip", ""));
                    card.setDescription(getSize(i));
                    card.setId(i);
                    card.setCustomOnSwipeListener(new DescriptionCard.CustomOnSwipeListener() {
                        @Override
                        public void onSwipe(int id) {
                            mUtils.toast(getActivity(), getString(R.string.deleted, files()[id]));
                            new File(download_path + "/" + files()[id]).delete();
                            refresh();
                        }
                    });

                    CardButtonExpand expand = new CardButtonExpand(getActivity());
                    expand.setButtonText1(getString(R.string.reboot_recovery));
                    expand.setButtonText2(getString(R.string.flash_now));
                    expand.setOnButtonClickListener(new CardButtonExpand.OnButtonClickListener() {
                        @Override
                        public void onButton1Click() {
                            mUtils.confirm(getActivity(), null, getString(R.string.reboot_recovery_confirm),
                                    new Utils.OnConfirmListener() {
                                        @Override
                                        public void onConfirm() {
                                            if (RootTools.isAccessGiven())
                                                mRootUtils.runCommand("reboot recovery");
                                            else
                                                mUtils.toast(getActivity(), getString(R.string.no_root));
                                        }

                                        @Override
                                        public void onCancel() {
                                        }
                                    });
                        }

                        @Override
                        public void onButton2Click() {
                            mUtils.confirm(getActivity(), null, getString(R.string.flash_now_confirm),
                                    new Utils.OnConfirmListener() {
                                        @Override
                                        public void onConfirm() {
                                            if (RootTools.isAccessGiven()) {
                                                mRootUtils.openrecoveryscript("install " +
                                                        download_path.replace(sdcard, "/sdcard") + "/" +
                                                        files()[card.getCustomId()]);
                                                mRootUtils.runCommand("reboot recovery");
                                            } else
                                                mUtils.toast(getActivity(), getString(R.string.no_root));
                                        }

                                        @Override
                                        public void onCancel() {
                                        }
                                    });
                        }
                    });

                    card.setViewToClickToExpand(ViewToClickToExpand.builder().setupView(expand.getCardView()));
                    card.addCardExpand(expand);

                    cards.add(card);
                }
            } else {
                DescriptionCard card = new DescriptionCard(getActivity());
                card.setTitle(getString(R.string.no_install_files));
                card.setDescription(getString(R.string.no_install_files_summary));

                cards.add(card);
            }

            if (mCardArrayAdapter == null || !refresh)
                mCardArrayAdapter = new CustomCardArrayAdapter(getActivity(), cards);

            if (refresh) mCardArrayAdapter.notifyDataSetChanged(true);
            else if (listView != null) listView.setAdapter(mCardArrayAdapter);

            refresh = false;

        }
    };

    private String getSize(int position) {
        return getString(R.string.size) + ": " + (new File(download_path + "/" + files()[position]).length() / 1048576)
                + getString(R.string.mb);
    }

    private String[] files() {
        File[] fFiles = new File(download_path).listFiles();
        String[] filesAll = new String[fFiles.length];
        int count = 0;
        for (int i = 0; i < filesAll.length; i++)
            if (fFiles[i].getName().endsWith(".zip")) {
                filesAll[i] = fFiles[i].getName();
                count++;
            }

        String[] filesZip = new String[count];
        System.arraycopy(filesAll, 0, filesZip, 0, filesZip.length);

        return filesZip;
    }

    private boolean containsFiles() {
        return new File(download_path).exists() && new File(download_path).listFiles().length > 0;
    }

}
