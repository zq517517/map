package com.example.tracegame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tracegame.database.GameDao;
import com.example.tracegame.models.GameModel;
import com.example.tracegame.utils.Constants;

import java.util.List;

/**
 * Created by Administrator on 2016/12/25.
 */
public class FriendActivity extends Activity implements View.OnClickListener{

    private List<GameModel> gameModelList;
    private ExpandableListView listView;
    private TextView noDataView;
    private GameAdapter gameAdapter;
    private Button toRadar;
    private Button toEnemy;
    private ImageView add;
    private ImageView edit;
    private Dialog addDialog;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_page);
        toRadar = (Button)findViewById(R.id.friend_radar);
        toRadar.setOnClickListener(this);
        toEnemy = (Button)findViewById(R.id.friend_enemies);
        toEnemy.setOnClickListener(this);
        add = (ImageView) findViewById(R.id.friend_add);
        add.setOnClickListener(this);
        edit = (ImageView)findViewById(R.id.friend_edit);
        edit.setOnClickListener(this);

        noDataView = (TextView)findViewById(R.id.no_data);
        listView = (ExpandableListView)findViewById(R.id.friend_list);
        listView.setGroupIndicator(null);
        gameAdapter = new GameAdapter(this);
        listView.setAdapter(gameAdapter);
        initAddDialog();
        loadData();
    }

    private void loadData(){
        gameModelList = GameDao.getInstance(this).getPerson(Constants.FRIENDS);
        gameAdapter.setList(gameModelList);
        gameAdapter.notifyDataSetChanged();
        checkHasData();
    }

    private void checkHasData(){
        if(gameModelList == null || gameModelList.size() <= 0){
            noDataView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            isEditing = false;
        }else {
            noDataView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.friend_radar:
                finish();
                break;

            case R.id.friend_enemies:
                Intent intent = new Intent(this, EnemyActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.friend_add:
                addDialog.show();
                break;

            case R.id.friend_edit:

                if(isEditing){
                    edit.setImageResource(R.mipmap.button_friends_list_edit);
                    isEditing = false;
                    gameAdapter.setEditing(false);
                    gameAdapter.notifyDataSetChanged();
                }else {
                    edit.setImageResource(R.mipmap.button_friends_list_done);
                    isEditing = true;
                    gameAdapter.setEditing(true);
                    gameAdapter.notifyDataSetChanged();
                }
                checkHasData();

                break;
        }
    }

    private void initAddDialog(){
        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_dialog_layout,null);
        final EditText editText = (EditText)dialogView.findViewById(R.id.add_dialog_edit);
        addDialog = new AlertDialog.Builder(this).setTitle("添加好友")
                .setView(dialogView)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String number = editText.getText().toString();
                        if(number.length() < 11){
                            Toast.makeText(FriendActivity.this,"号码不足11位",Toast.LENGTH_SHORT).show();
                        }else {
                            GameModel gameModel = new GameModel();
                            gameModel.setType(Constants.FRIENDS);
                            gameModel.setPhoneNum(number);
                            gameModel.setLatitude(-1);
                            gameModel.setLongitude(-1);
                            if(!isExist(number))
                                GameDao.getInstance(FriendActivity.this).insert(gameModel);
                            else
                                GameDao.getInstance(FriendActivity.this).update(gameModel);
                            Toast.makeText(FriendActivity.this,"添加成功",Toast.LENGTH_SHORT).show();
                            loadData();
                            dialog.dismiss();
                        }
                    }
                }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }

    private boolean isExist(String number){
        for (GameModel gameModel : gameModelList){
            if(gameModel.getPhoneNum().equals(number))
                return true;
        }
        return false;
    }
}
