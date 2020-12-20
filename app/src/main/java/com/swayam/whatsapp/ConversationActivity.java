package com.swayam.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Inflater;

public class ConversationActivity extends AppCompatActivity {
    private TextView userText;
    private String openedUser;
    private RecyclerView recyclerView;
    private ConversationRecyclerViewAdapter adapter;
    private EditText textField;
    private boolean stopRetrievingMessages = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        openedUser = getIntent().getStringExtra("USER");

        userText = findViewById(R.id.username);
        userText.setText(openedUser);
        textField = findViewById(R.id.messageTextField);

        recyclerView = findViewById(R.id.recyclerview);
        adapter = new ConversationRecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean b) {
                recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
            }
        });

        getOldMessagesInBackground();

    }

    private void getOldMessagesInBackground(){
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait retrieving old messages");
        dialog.show();

        ParseUser parseUser = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> queryTo = ParseQuery.getQuery("Messages");
        queryTo.whereEqualTo("to",parseUser.getUsername());
        queryTo.whereEqualTo("from",openedUser);

        ParseQuery<ParseObject> queryFrom = ParseQuery.getQuery("Messages");
        queryFrom.whereEqualTo("to",openedUser);
        queryFrom.whereEqualTo("from",parseUser.getUsername());

        ArrayList<ParseQuery<ParseObject>> allQueries = new ArrayList<>();
        allQueries.add(queryTo);
        allQueries.add(queryFrom);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(allQueries);
        mainQuery.whereNotContainedIn("objectId",adapter.getObjectIds());

        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (ParseObject object : objects){
                    dialog.dismiss();
                    if (object.getString("from").equals(parseUser.getUsername())){
                        adapter.addMessage(object.getObjectId(),ConversationRecyclerViewAdapter.SELF,object.getString("message"));
                    }else {
                        adapter.addMessage(object.getObjectId(),ConversationRecyclerViewAdapter.OPP,object.getString("message"));
                    }
                    recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
                }
                startRetrievingMessages();
            }
        });
    }

    private void startRetrievingMessages(){
        ParseUser parseUser = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> queryTo = ParseQuery.getQuery("Messages");
        queryTo.whereEqualTo("to",parseUser.getUsername());
        queryTo.whereEqualTo("from",openedUser);

        ParseQuery<ParseObject> queryFrom = ParseQuery.getQuery("Messages");
        queryFrom.whereEqualTo("to",openedUser);
        queryFrom.whereEqualTo("from",parseUser.getUsername());

        ArrayList<ParseQuery<ParseObject>> allQueries = new ArrayList<>();
        allQueries.add(queryTo);
        allQueries.add(queryFrom);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(allQueries);
        mainQuery.whereNotContainedIn("objectId",adapter.getObjectIds());

        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (ParseObject object : objects){
                    if (object.getString("from").equals(parseUser.getUsername())){
                        adapter.addMessage(object.getObjectId(),ConversationRecyclerViewAdapter.SELF,object.getString("message"));
                    }else {
                        adapter.addMessage(object.getObjectId(),ConversationRecyclerViewAdapter.OPP,object.getString("message"));
                    }
                    recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
                }
                if (!stopRetrievingMessages){
                    startRetrievingMessages();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,Home.class));
        finish();
    }

    public void sendMessage(View view) {
        String message = textField.getText().toString();
        textField.setText("");
        if (message.equals("")){
            return;
        }

        ParseObject parseObject = new ParseObject("Messages");
        parseObject.put("from",ParseUser.getCurrentUser().getUsername());
        parseObject.put("to",openedUser);
        parseObject.put("message",message);
        parseObject.put("date",new Date());

        parseObject.saveInBackground();
    }

    public void emojiPressed(View view) {
    }

    public void backPressed(View view) {
        onBackPressed();
    }
}

class ConversationRecyclerViewAdapter extends RecyclerView.Adapter<ConversationRecyclerViewAdapter.Holder>{
    public static final String SELF = "SELF";
    public static final String OPP = "OPP";
    private ArrayList<String[]> messages;
    private ArrayList<String> objectIds;
    private Context mContext;

    public ConversationRecyclerViewAdapter(Context context){
        mContext = context;
        messages = new ArrayList<>();
        objectIds = new ArrayList<>();
    }

    public void addMessage(String objectId,String from,String message){
        objectIds.add(objectId);
        messages.add(new String[]{from,message});
        notifyDataSetChanged();
    }

    public ArrayList<String> getObjectIds(){
        return objectIds;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position)[0].equals(SELF)){
            return R.layout.right_chat_bubble;
        }
        return R.layout.left_chat_bubble;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String[] object = messages.get(position);
        holder.message.setText(object[1]);
        Log.i("TAG", "onBindViewHolder: "+position);
        if (position>0 && messages.get(position-1)[0].equals(messages.get(position)[0])){
            holder.triangle.setVisibility(View.INVISIBLE);
        }else {
            holder.triangle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        private TextView message;
        private View triangle;

        public Holder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            triangle = itemView.findViewById(R.id.triangle);
        }
    }
}