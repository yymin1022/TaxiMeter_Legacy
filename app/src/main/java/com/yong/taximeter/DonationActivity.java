package com.yong.taximeter;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DonationActivity extends AppCompatActivity implements PurchasesUpdatedListener{
    static final String SKU_AD_REMOVE = "ad_remove";
    static final String SKU_DONATE_1 = "donate_1000";
    static final String SKU_DONATE_2 = "donate_5000";
    static final String SKU_DONATE_3 = "donate_10000";
    static final String SKU_DONATE_4 = "donate_50000";

    String priceAdRemove;
    String priceDonate1;
    String priceDonate2;
    String priceDonate3;
    String priceDonate4;

    private BillingClient billingClient;
    private LinearLayout btnAdRemove;
    private LinearLayout btnDonate1;
    private LinearLayout btnDonate2;
    private LinearLayout btnDonate3;
    private LinearLayout btnDonate4;
    private LinearLayout btnDonateSelf;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);

        btnAdRemove = findViewById(R.id.btn_donation_ad);
        btnDonate1 = findViewById(R.id.btn_donation_1000);
        btnDonate2 = findViewById(R.id.btn_donation_5000);
        btnDonate3 = findViewById(R.id.btn_donation_10000);
        btnDonate4 = findViewById(R.id.btn_donation_50000);
        btnDonateSelf = findViewById(R.id.btn_donation_self);

        billingClient = BillingClient.newBuilder(DonationActivity.this).setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {
                if(responseCode == BillingClient.BillingResponse.OK){
                    //Start Quering to Google Play
                    queryPurchase();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //Try reconnect to Google Play using startConnection() method
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BillingFlowParams flowParams;
                switch(v.getId()){
                    case R.id.btn_donation_1000:
                        flowParams = BillingFlowParams.newBuilder()
                                .setSku(SKU_DONATE_1)
                                .setType(BillingClient.SkuType.INAPP)
                                .build();
                        billingClient.launchBillingFlow(DonationActivity.this, flowParams);
                        break;
                    case R.id.btn_donation_5000:
                        flowParams = BillingFlowParams.newBuilder()
                                .setSku(SKU_DONATE_2)
                                .setType(BillingClient.SkuType.INAPP)
                                .build();
                        billingClient.launchBillingFlow(DonationActivity.this, flowParams);
                        break;
                    case R.id.btn_donation_10000:
                        flowParams = BillingFlowParams.newBuilder()
                                .setSku(SKU_DONATE_3)
                                .setType(BillingClient.SkuType.INAPP)
                                .build();
                        billingClient.launchBillingFlow(DonationActivity.this, flowParams);
                        break;
                    case R.id.btn_donation_50000:
                        flowParams = BillingFlowParams.newBuilder()
                                .setSku(SKU_DONATE_4)
                                .setType(BillingClient.SkuType.INAPP)
                                .build();
                        billingClient.launchBillingFlow(DonationActivity.this, flowParams);
                        break;
                    case R.id.btn_donation_ad:
                        flowParams = BillingFlowParams.newBuilder()
                                .setSku(SKU_AD_REMOVE)
                                .setType(BillingClient.SkuType.INAPP)
                                .build();
                        billingClient.launchBillingFlow(DonationActivity.this, flowParams);
                        break;
                    case R.id.btn_donation_self:
                        AlertDialog.Builder builder = new AlertDialog.Builder(DonationActivity.this);
                        builder.setMessage("은행 계좌입금을 통해 원하시는 만큼 기부해주실 수 있습니다.\n\n우리은행 1002-357-339255\n카카오뱅크 3333-12-7882414");
                        builder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                        break;
                }
            }
        };

        btnAdRemove.setOnClickListener(onClickListener);
        btnDonate1.setOnClickListener(onClickListener);
        btnDonate2.setOnClickListener(onClickListener);
        btnDonate3.setOnClickListener(onClickListener);
        btnDonate4.setOnClickListener(onClickListener);
        btnDonateSelf.setOnClickListener(onClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        billingClient.queryPurchases(BillingClient.SkuType.INAPP);
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if(responseCode == BillingClient.BillingResponse.OK && purchases != null){
            //Successfully Purchased
            for(Purchase purchase : purchases){
                if(purchase.getSku().equals(SKU_AD_REMOVE)){
                    Toast.makeText(getApplicationContext(), "구매해주셔서 감사합니다. 애플리케이션을 다시 시작하시면 광고가 사라집니다.", Toast.LENGTH_LONG).show();

                    //Save SharedPreferences about Removing Advertisement
                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("androesPrefName", MODE_PRIVATE);
                    SharedPreferences.Editor ed = prefs.edit();
                    ed.remove("ad_removed");
                    ed.putBoolean("ad_removed", true);
                    ed.apply();
                }else{
                    Toast.makeText(getApplicationContext(), "Thanks for Donation!", Toast.LENGTH_LONG).show();

                    billingClient.consumeAsync(purchase.getPurchaseToken(), new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(int responseCode, String purchaseToken) {

                        }
                    });
                }
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED){
            //User Canceled Puchase
            Toast.makeText(getApplicationContext(), "사용자의 요청으로 취소되었습니디.", Toast.LENGTH_LONG).show();
        }else if(responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED){
            //Already Purchased Item
            Toast.makeText(getApplicationContext(), "이미 구매된 항목입니다.", Toast.LENGTH_LONG).show();
        }else{
            //Unknown Code
            Toast.makeText(getApplicationContext(),  String.format(Locale.getDefault(), "%d : 알 수 없는 에러입니다.", responseCode), Toast.LENGTH_LONG).show();
        }
    }

    public void queryPurchase(){
        List skuList = new ArrayList<>();
        skuList.add(SKU_AD_REMOVE);
        skuList.add(SKU_DONATE_1);
        skuList.add(SKU_DONATE_2);
        skuList.add(SKU_DONATE_3);
        skuList.add(SKU_DONATE_4);

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList);
        params.setType(BillingClient.SkuType.INAPP);

        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                        if(responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null){
                            for (Object skuDetailsObject : skuDetailsList) {
                                SkuDetails skuDetails = (SkuDetails)skuDetailsObject;
                                String sku = skuDetails.getSku();
                                String price = skuDetails.getPrice();
                                switch (sku){
                                    case SKU_AD_REMOVE:
                                        priceAdRemove = price;
                                        break;
                                    case SKU_DONATE_1:
                                        priceDonate1 = price;
                                        break;
                                    case SKU_DONATE_2:
                                        priceDonate2 = price;
                                        break;
                                    case SKU_DONATE_3:
                                        priceDonate3 = price;
                                        break;
                                    case SKU_DONATE_4:
                                        priceDonate4 = price;
                                        break;
                                }
                            }
                        }
                    }
                });
    }
}
