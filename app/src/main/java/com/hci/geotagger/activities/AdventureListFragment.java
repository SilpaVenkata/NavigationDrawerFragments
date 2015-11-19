package com.hci.geotagger.activities;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.hci.geotagger.GeotaggerApplication;
import com.hci.geotagger.R;
import com.hci.geotagger.activities.dummy.DummyContent;
import com.hci.geotagger.dbhandler.DbHandlerConstants;
import com.hci.geotagger.dbhandler.DbHandlerResponse;
import com.hci.geotagger.gui.GenericEntryListAdapter;
import com.hci.geotagger.objects.Adventure;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class AdventureListFragment extends ListFragment implements DbHandlerResponse.DbMessageResponseInterface, AbsListView.OnItemClickListener {

    private static final String TAG = "AdventureListFragment";

    private ArrayList<Adventure> adventures = null;
    private GenericEntryListAdapter listAdapter;
    private Long userID;
    private int CONTEXT_DELETE_ID = 1;
    private int CONTEXT_ADD_ID = 2;
    private Adventure a;
    private int recordBeingDeleted;

    private int whichFilter = 0;

    private static DbHandlerResponse rspHandler;
    private GeotaggerApplication mApp = null;

    // saved preferences
    private static final String ADVLIST_PREFERENCES = "AdvListData";
    private static final String ADVLIST_FILTER = "AdvListFilter";


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static AdventureListFragment newInstance(String param1, String param2) {
        AdventureListFragment fragment = new AdventureListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdventureListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Silpa
        mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listfragment, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        mApp = (GeotaggerApplication)getActivity().getApplication();
        if (mApp != null) {
            rspHandler = new DbHandlerResponse(TAG, view.getContext(), this);
            mApp.addResponseHandler(rspHandler);

            getAdventures();
        }


        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        // initialize objects
        adventures = new ArrayList<Adventure>();
        listAdapter = new GenericEntryListAdapter(getActivity().getApplicationContext(), R.layout.row, adventures);
        setListAdapter(listAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    /**
     * This method will make the appropriate database call to get the
     * Adventures. This depends on the current setting of the
     * whichFilter variable.
     */
    private void getAdventures() {
        //setupProgress(getString(R.string.progress_retrieving_adventures));
        //showProgress();

        int action;

        if (whichFilter == 1) {
            // Get Adventures for this user
            action = DbHandlerConstants.DBMSG_GET_ADVENTURES_OWNEROF;
        } else if (whichFilter == 2) {
            // Get Adventures this user is a member of
            action = DbHandlerConstants.DBMSG_GET_ADVENTURES_MEMBEROF;
        } else {
            // Get all adventures
            action = DbHandlerConstants.DBMSG_GET_ALL_ADVENTURES;
        }
        mApp.sendMsgToDbHandler(rspHandler, getActivity().getApplicationContext(), action);
    }

    @Override
    public void DbMessageResponse_DBCallback(int action, int msgID, boolean success, boolean done, Object response) {
        Log.d(TAG, "Entered DBGetCallback");
        if (action == DbHandlerConstants.DBMSG_DELETE) {
            // once the tag is removed from the db, remove it
            // from the arraylist and update.
            listAdapter.remove(adventures.get(recordBeingDeleted));
            listAdapter.notifyDataSetChanged();
            adventures.remove(recordBeingDeleted);
            //stopProgress();
            Toast.makeText(getActivity(), getString(R.string.toast_adventure_deleted), Toast.LENGTH_SHORT).show();

        } else {
            if (success) {
                @SuppressWarnings("unchecked")
                ArrayList<Adventure> advResponse = (ArrayList<Adventure>)response;
                adventures = advResponse;

                listAdapter.notifyDataSetChanged();
                listAdapter.clear();
                for (int i = 0; i < adventures.size(); i++)
                    listAdapter.add(adventures.get(i));
                listAdapter.notifyDataSetChanged();
            }
            //stopProgress();
        }
    }

}
