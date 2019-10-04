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
        if(actionBar != null){
            actionBar.setTitle(getString(R.string.info_title));
        }
        setContentView(R.layout.activity_info);
    }

    public void openGit(View V){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.info_dialog_github_text));
        builder.setPositiveButton(getString(R.string.info_dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yymin1022")));
            }
        });
        builder.show();
    }

    public void openBlog(View V){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.info_dialog_blog_text));
        builder.setPositiveButton(getString(R.string.info_dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.naver.com/yymin1022")));
            }
        });
        builder.show();
    }
}
