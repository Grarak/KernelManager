package com.grarak.kernel.manager.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.grarak.kernel.manager.R;
import com.grarak.kernel.manager.elements.CustomCard;
import com.grarak.kernel.manager.elements.CustomCardArrayAdapter;
import com.grarak.kernel.manager.utils.Constants;
import com.grarak.kernel.manager.utils.JsonUtils.JsonDeviceArrays;
import com.grarak.kernel.manager.utils.Utils;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Created by grarak on 15.10.14.
 */
public class BackupFragment extends Fragment implements Constants {

    private JsonDeviceArrays mJsonDeviceArrays;

    private boolean refresh = false;

    private ArrayList<Card> cards = new ArrayList<Card>();
    private CustomCardArrayAdapter mCardArrayAdapter;
    private SwipeRefreshLayout refreshLayout;
    private CardListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mJsonDeviceArrays = new JsonDeviceArrays(mUtils.getDeviceAssetFile(getActivity()));

        View rootView = inflater.inflate(R.layout.backup_fragment, container, false);

        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_red_dark));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        listView = (CardListView) rootView.findViewById(R.id.card_listview);

        FloatingActionButton backupButton = (FloatingActionButton) rootView.findViewById(R.id.backup_button);
        backupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupAlert();
            }
        });

        getActivity().runOnUiThread(run);

        return rootView;
    }

    private void refresh() {
        refresh = true;
        refreshLayout.setRefreshing(true);
        getActivity().runOnUiThread(run);
        refreshLayout.setRefreshing(false);
    }

    private final Runnable run = new Runnable() {
        @Override
        public void run() {
            cards.clear();

            if (containsFiles()) for (int i = 0; i < files().length; i++) {
                final CustomCard.DescriptionCard card = new CustomCard.DescriptionCard(getActivity());
                card.setTitle(files()[i].replace(".img", ""));
                card.setDescription(getSize(i));
                card.setId(i);
                card.setCustomOnSwipeListener(new CustomCard.DescriptionCard.CustomOnSwipeListener() {
                    @Override
                    public void onSwipe(int id) {
                        mUtils.toast(getActivity(), getString(R.string.deleted, files()[id]));
                        new File(backup_path + "/" + files()[id]).delete();
                        refresh();
                    }
                });
                card.setOnClickListener(new CustomCard.DescriptionCard.CustomOnCardClickListener() {
                    @Override
                    public void onClick(final int id) {
                        mUtils.confirm(getActivity(), getString(R.string.restore_confirm, files()[id]), null,
                                new Utils.OnConfirmListener() {
                                    @Override
                                    public void onConfirm() {
                                        if (RootTools.isAccessGiven()) restoringBackup(files()[id]);
                                        else
                                            mUtils.toast(getActivity(), getString(R.string.no_root));
                                    }

                                    @Override
                                    public void onCancel() {
                                    }
                                });
                    }
                });

                cards.add(card);
            }
            else {
                CustomCard.DescriptionCard card = new CustomCard.DescriptionCard(getActivity());
                card.setTitle(getString(R.string.no_backup_files));
                card.setDescription(getString(R.string.no_backup_files_summary));

                cards.add(card);
            }

            if (mCardArrayAdapter == null || !refresh)
                mCardArrayAdapter = new CustomCardArrayAdapter(getActivity(), cards);

            if (refresh) mCardArrayAdapter.notifyDataSetChanged(true);
            else if (listView != null) listView.setAdapter(mCardArrayAdapter);

            refresh = false;
        }
    };

    private void backupAlert() {

        final EditText nameEdit = new EditText(getActivity());
        nameEdit.setTextColor(getResources().getColor(android.R.color.black));
        nameEdit.setHint(getString(R.string.name_backup));
        nameEdit.setText(mKernelUtils.getKernelLocalVersion());

        new MaterialDialog.Builder(getActivity())
                .customView(nameEdit)
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .callback(new MaterialDialog.Callback() {
                    @Override
                    public void onNegative(MaterialDialog materialDialog) {
                    }

                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        if (RootTools.isAccessGiven())
                            createBackup(nameEdit.getText().toString().isEmpty() ? mKernelUtils.getKernelLocalVersion()
                                    : nameEdit.getText().toString());
                        else mUtils.toast(getActivity(), getString(R.string.no_root));
                    }
                }).show();

    }

    private void createBackup(final String name) {
        new File(backup_path).mkdirs();

        final MaterialDialog dialogBuilder = new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.create_backup, name + ".img"))
                .customView(new ProgressBar(getActivity()))
                .show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mRootUtils.runCommand("rm -f " + TEMP_FILE);
                mRootUtils.runCommand("dd if=" + mJsonDeviceArrays.getBootPartition() + " of=" +
                        backup_path.replace(sdcard, "/sdcard") + "/" + name + ".img && touch " + TEMP_FILE);
                while (true) if (new File(TEMP_FILE).exists()) {
                    mRootUtils.runCommand("rm -f " + TEMP_FILE);
                    dialogBuilder.dismiss();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refresh();
                        }
                    });
                    break;
                }
            }
        }).start();
    }

    private void restoringBackup(final String imageName) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.restore_backup, imageName));
        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mRootUtils.runCommand("rm -f " + TEMP_FILE);
                mRootUtils.runCommand("dd if=" + backup_path.replace(sdcard, "/sdcard") + "/" + imageName
                        + " of=" + mJsonDeviceArrays.getBootPartition() + " && touch " + TEMP_FILE);
                while (true) if (new File(TEMP_FILE).exists()) {
                    mRootUtils.runCommand("rm -f " + TEMP_FILE);
                    dialog.dismiss();
                    break;
                }
            }
        }).start();
    }

    private String getSize(int position) {
        return getString(R.string.size) + ": " + (new File(backup_path + "/" + files()[position]).length() / 1048576)
                + getString(R.string.mb);
    }

    private String[] files() {
        File[] fFiles = new File(backup_path).listFiles();
        String[] filesAll = new String[fFiles.length];
        int count = 0;
        for (int i = 0; i < filesAll.length; i++)
            if (fFiles[i].getName().endsWith(".img")) {
                filesAll[i] = fFiles[i].getName();
                count++;
            }

        String[] filesZip = new String[count];
        System.arraycopy(filesAll, 0, filesZip, 0, filesZip.length);

        return filesZip;
    }

    private boolean containsFiles() {
        if (!new File(backup_path).exists()) return false;

        File[] files = new File(backup_path).listFiles();
        for (File file : files)
            if (file.getName().endsWith(".img")) return true;

        return false;
    }

}
