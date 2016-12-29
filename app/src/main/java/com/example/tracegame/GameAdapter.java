package com.example.tracegame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tracegame.database.GameDao;
import com.example.tracegame.models.GameModel;
import com.example.tracegame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/25.
 */
public class GameAdapter extends BaseExpandableListAdapter{

    private List<GameModel> gameModelList = new ArrayList<>();
    private Context context;
    private boolean isEditing = false;

    public GameAdapter(Context context){
        this.context = context;
    }

    public void setList(List<GameModel> list){
        this.gameModelList = list;
    }

    public void setEditing(boolean b){
        isEditing = b;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return gameModelList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }


    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder;
        if(convertView == null){
            groupHolder = new GroupHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_title_layout,null);
            groupHolder.name = (TextView)convertView.findViewById(R.id.item_title_name);
            groupHolder.marker = (ImageView) convertView.findViewById(R.id.item_title_marker);
            groupHolder.delete = (ImageView)convertView.findViewById(R.id.item_title_delete);
            convertView.setTag(groupHolder);
        }else {
            groupHolder = (GroupHolder)convertView.getTag();
        }

        int type = gameModelList.get(groupPosition).getType();

        if(type == Constants.ENEMIES){
            groupHolder.name.setTextColor(Color.RED);
            groupHolder.marker.setImageResource(R.mipmap.enemy_marker);
        }else {
            groupHolder.name.setTextColor(Color.GREEN);
            groupHolder.marker.setImageResource(R.mipmap.friend_marker);
        }

        if(isEditing){
            groupHolder.delete.setVisibility(View.VISIBLE);
        }else {
            groupHolder.delete.setVisibility(View.GONE);
        }

        groupHolder.name.setText(gameModelList.get(groupPosition).getPhoneNum());
        groupHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setTitle("提示！")
                        .setMessage("确认删除号码为： "+ gameModelList.get(groupPosition).getPhoneNum()+" 的"
                                + getTipMessage(gameModelList.get(groupPosition).getType()))
                        .setNegativeButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GameDao.getInstance(context).delete(gameModelList.get(groupPosition).getPhoneNum());
                                gameModelList.remove(groupPosition);
                                Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                notifyDataSetChanged();
                            }
                        }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder;
        if(convertView == null){
            childHolder = new ChildHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_content_layout,null);
            childHolder.phoneNum = (TextView)convertView.findViewById(R.id.item_content_phone_number);
            childHolder.location = (TextView) convertView.findViewById(R.id.item_content_location);
            childHolder.layout = (ImageView) convertView.findViewById(R.id.item_content_layout);
            convertView.setTag(childHolder);
        }else {
            childHolder = (ChildHolder)convertView.getTag();
        }

        int type = gameModelList.get(groupPosition).getType();
        if(type == Constants.ENEMIES){
            childHolder.layout.setBackgroundResource(R.mipmap.enemy_detail_centre_background);
        }else {
            childHolder.layout.setBackgroundResource(R.mipmap.friend_detail_centre_background);
        }

        double latitude = gameModelList.get(groupPosition).getLatitude();
        latitude = ((int)(latitude*100))/100;
        double longitude = gameModelList.get(groupPosition).getLongitude();
        longitude = ((int)(longitude*100))/100;
        childHolder.phoneNum.setText(gameModelList.get(groupPosition).getPhoneNum());
        childHolder.location.setText(latitude+"N / "+longitude+"E");

        return convertView;
    }

    private String getTipMessage(int type){
        if(type == Constants.FRIENDS){
            return "朋友";
        }else {
            return "敌人";
        }
    }

    private class GroupHolder{
        ImageView marker;
        TextView name;
        ImageView delete;
    }

    private class ChildHolder{
        TextView phoneNum;
        TextView location;
        ImageView layout;
    }
}
