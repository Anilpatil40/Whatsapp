package com.swayam.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.nikartm.button.FitButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.random.customdialog.CustomDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatRecyclerViewAdapter adapter;
    private FitButton newMessage;
    private SwipeRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = getView().findViewById(R.id.recyclerview);
        newMessage = getView().findViewById(R.id.newMessage);
        refreshLayout = getView().findViewById(R.id.refreshLayout);

        adapter = new ChatRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        newMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject object = new ParseObject("Messages");
                object.put("from","panil8993");
                object.put("message","this is me");
                object.put("date",new Date());
                adapter.addParseObject(object);
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshChatList();
            }
        });

        adapter.setOnItemSelectListener(new ChatRecyclerViewAdapter.OnItemSelectListener() {
            @Override
            public void selected(String username) {
                startActivity(new Intent(getContext(),ConversationActivity.class).putExtra("USER",username));
                getActivity().finish();
            }
        });

        refreshChatList();
    }

    private void refreshChatList(){
        refreshLayout.setRefreshing(true);

        ParseUser user = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> queryTO = ParseQuery.getQuery("Messages");
        queryTO.whereMatches("to",user.getUsername());
        ParseQuery<ParseObject> queryFrom = ParseQuery.getQuery("Messages");
        queryFrom.whereMatches("from",user.getUsername());

        ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(queryTO);
        queries.add(queryFrom);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);

        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                refreshLayout.setRefreshing(false);
                if (e == null){
                    for (ParseObject parseObject : objects){
                        adapter.addParseObject(parseObject);
                    }
                }else {

                }
            }
        });

    }
}

class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.Holder>{
    private ArrayList<String> usernames;
    private ArrayList<String> messages;
    private ArrayList<String> dates;
    private OnItemSelectListener onItemSelectListener;

    public ChatRecyclerViewAdapter(){
        usernames = new ArrayList<>();
        messages = new ArrayList<>();
        dates = new ArrayList<>();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_view,parent,false);
        return new Holder(view);
    }

    public void addParseObject(ParseObject parseObject){
        String username = ""+parseObject.get("from");
        if (username.equals(ParseUser.getCurrentUser().getUsername())){
            username = ""+parseObject.get("to");
        }
        if (usernames.contains(username)){
            int index = usernames.indexOf(username);
            usernames.remove(index);
            messages.remove(index);
            dates.remove(index);
        }
        usernames.add(0,username);
        messages.add(0,""+parseObject.get("message"));
        dates.add(0,""+parseObject.get("date"));
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String username = usernames.get(position);
        holder.username.setText(username);
        holder.message.setText(""+messages.get(position));
        holder.time.setText(dates.get(position).toString().split(" ")[3]);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemSelectListener != null)
                    onItemSelectListener.selected(username);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usernames.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        private TextView username;
        private TextView message;
        private TextView time;

        public Holder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
        }
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener;
    }

    interface OnItemSelectListener{
        void selected(String username);
    }
}