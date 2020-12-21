package com.swayam.whatsapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.nikartm.button.FitButton;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.random.customdialog.CustomDialog;
import com.swayam.imagepicker.ImagePicker;

import java.util.ArrayList;
import java.util.List;

public class StatusFragment extends Fragment {
    private FitButton newStatus;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private StatusRecyclerViewAdapter adapter;
    public static final int IMAGE_PICKER_REQUEST_CODE = 1;
    private ArrayList<String> objectIds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newStatus = getView().findViewById(R.id.newStatus);
        refreshLayout = getView().findViewById(R.id.refreshLayout);
        recyclerView = getView().findViewById(R.id.recyclerview);
        adapter = new StatusRecyclerViewAdapter();
        adapter.setOnItemSelectListener(new StatusRecyclerViewAdapter.OnItemSelectListener() {
            @Override
            public void selectedItem(ParseObject object) {
                startActivity(new Intent(getContext(),StatusViewerActivity.class).putExtra("status",object.getObjectId()));
            }
        });
        recyclerView.setAdapter(adapter);
        newStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity().checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
                startActivityForResult(new Intent(getContext(), ImagePicker.class),IMAGE_PICKER_REQUEST_CODE);
                else
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            1);

            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getStatuses();
            }
        });
        getStatuses();

    }

    private void getStatuses(){
        refreshLayout.setRefreshing(true);
        ParseQuery<ParseUser> idQueries = ParseUser.getQuery();
        idQueries.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());

        idQueries.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null){
                    adapter.removeAll();
                    ParseUser parseUser = objects.get(0);
                    objectIds = (ArrayList<String>) parseUser.get("status_viewed");
                    getAllData();
                }
            }
        });


    }

    private void getAllData(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Statuses");
        if (objectIds != null)
            query.whereNotContainedIn("objectId",objectIds);
        for (ParseObject object : adapter.getStatuses()){
            query.whereNotEqualTo("objectId",object.getObjectId());
        }
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                refreshLayout.setRefreshing(false);
                if (e == null){
                    for (ParseObject object : objects){
                        adapter.addStatus(object);
                    }
                }
            }
        });
    }

    private void uploadStatus(String comment,byte[] data){
        ImageView imageView = getView().findViewById(R.id.image_view);
        TextView textView = getView().findViewById(R.id.message);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);

        if ((comment == null || comment.equals("")) && (data == null || data.length == 0))
            return;

        ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.setMessage("uploading...");
        dialog.show();

        ParseObject parseObject = new ParseObject("Statuses");
        parseObject.put("username", ParseUser.getCurrentUser().getUsername());
        parseObject.put("tag",comment);
        ParseFile parseFile = new ParseFile(data);
        parseObject.put("image",parseFile);

        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                if (e != null){
                    showErrorMessage("status not upload. please try again");
                }
            }
        });

    }

    private void showErrorMessage(String message){
        CustomDialog dialog = new CustomDialog(getContext(),CustomDialog.FAILURE);
        dialog.setTitle("FAILED");
        dialog.setContentText(message);
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startActivityForResult(new Intent(getContext(), ImagePicker.class),IMAGE_PICKER_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICKER_REQUEST_CODE){
            if (resultCode == getActivity().RESULT_OK){
                uploadStatus(data.getStringExtra("comment"),data.getByteArrayExtra("image"));
            }
        }
    }

}

class StatusRecyclerViewAdapter extends RecyclerView.Adapter<StatusRecyclerViewAdapter.Holder> {
    private ArrayList<ParseObject> statuses;
    private OnItemSelectListener listener;

    public StatusRecyclerViewAdapter(){
        statuses = new ArrayList<>();
    }

    public ArrayList<ParseObject> getStatuses(){
        return statuses;
    }

    public void addStatus(ParseObject parseObject){
        statuses.add(parseObject);
        notifyDataSetChanged();
    }

    public void removeAll(){
        statuses = new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.status_item_view,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ParseObject object = statuses.get(position);
        holder.username.setText(object.get("username").toString());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.selectedItem(object);
            }
        });
    }

    @Override
    public int getItemCount() {
        return statuses.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        private TextView username;

        public Holder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
        }
    }

    interface OnItemSelectListener{
        void selectedItem(ParseObject object);
    }

    public void setOnItemSelectListener(OnItemSelectListener listener){
        this.listener = listener;
    }
}
