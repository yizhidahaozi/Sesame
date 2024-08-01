package io.github.lazyimmortal.sesame.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.Uri;
import android.os.Build;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.github.lazyimmortal.sesame.hook.ApplicationHook;
import io.github.lazyimmortal.sesame.R;
import io.github.lazyimmortal.sesame.util.*;

public class ExtendActivity extends BaseActivity {

    Button btnGetTreeItems;
    Button btnGetNewTreeItems;
    Button btnQueryAreaTrees;
    Button btnGetUnlockTreeItems;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_extend);
        btnGetTreeItems = findViewById(R.id.get_tree_items);
        btnGetNewTreeItems = findViewById(R.id.get_newTree_items);
        btnQueryAreaTrees = findViewById(R.id.query_area_trees);
        btnGetUnlockTreeItems = findViewById(R.id.get_unlock_treeItems);

        setBaseTitle("扩展功能");

        btnGetTreeItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                sendItemsBroadcast("getTreeItems");
                Toast.makeText(ExtendActivity.this, "已发送查询请求，请在森林日志查看结果！", Toast.LENGTH_SHORT).show();
            }
        });

        btnGetNewTreeItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                sendItemsBroadcast("getNewTreeItems");
                Toast.makeText(ExtendActivity.this, "已发送查询请求，请在森林日志查看结果！", Toast.LENGTH_SHORT).show();
            }
        });

        btnQueryAreaTrees.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                sendItemsBroadcast("queryAreaTrees");
                Toast.makeText(ExtendActivity.this, "已发送查询请求，请在森林日志查看结果！", Toast.LENGTH_SHORT).show();
            }
        });

        btnGetUnlockTreeItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                sendItemsBroadcast("getUnlockTreeItems");
                Toast.makeText(ExtendActivity.this, "已发送查询请求，请在森林日志查看结果！", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendItemsBroadcast(String type) {
        Intent intent = new Intent("com.eg.android.AlipayGphone.sesame.rpctest");
        intent.putExtra("method", "");
        intent.putExtra("data", "");
        intent.putExtra("type", type);
        sendBroadcast(intent);
    }

}
