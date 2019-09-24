package com.yong.taximeter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("개발자 정보");
        setContentView(R.layout.activity_info);
    }

    public void openGit(View V){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("개발자의 Github로 이동합니다.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yymin1022")));
            }
        });
        builder.show();
    }

    public void openBlog(View V){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("개발자의 블로그로 이동합니다.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.naver.com/yymin1022")));
            }
        });
        builder.show();
    }
}
