package com.yong.taximeter;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static android.view.View.GONE;

public class DonationActivity extends AppCompatActivity implements PurchasesUpdatedListener{
    static final String SKU_AD_REMOVE = "ad_remove";
    static final String SKU_DONATE_1 = "donation_1000";
    static final String SKU_DONATE_2 = "donation_5000";
    static final String SKU_DONATE_3 = "donation_10000";
    static final String SKU_DONATE_4 = "donation_50000";

    String priceAdRemove;
    String priceDonate1;
    String priceDonate2;
    String priceDonate3;
    String priceDonate4;

    BillingClient billingClient;
    List<SkuDetails> skuDetailsList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);

        LinearLayout btnAdRemove = findViewById(R.id.btn_donation_ad);
        LinearLayout btnDonate1 = findViewById(R.id.btn_donation_1000);
        LinearLayout btnDonate2 = findViewById(R.id.btn_donation_5000);
        LinearLayout btnDonate3 = findViewById(R.id.btn_donation_10000);
        LinearLayout btnDonate4 = findViewById(R.id.btn_donation_50000);
        LinearLayout btnDonateSelf = findViewById(R.id.btn_donation_self);

        billingClient = BillingClient.newBuilder(DonationActivity.this)
                .enablePendingPurchases()
                .setListener(this)
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
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
                if(v.getId() == R.id.btn_donation_self){
                    AlertDialog.Builder builder = new AlertDialog.Builder(DonationActivity.this);
                    builder.setMessage("은행 계좌입금을 통해 원하시는 만큼 기부해주실 수 있습니다.\n\n우리은행 1002-357-339255\n카카오뱅크 3333-12-7882414");
                    builder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }else{
                    String strSKU = "";
                    switch(v.getId()){
                        case R.id.btn_donation_1000:
                            strSKU = SKU_DONATE_1;
                            break;
                        case R.id.btn_donation_5000:
                            strSKU = SKU_DONATE_2;
                            break;
                        case R.id.btn_donation_10000:
                            strSKU = SKU_DONATE_3;
                            break;
                        case R.id.btn_donation_50000:
                            strSKU = SKU_DONATE_4;
                            break;
                        case R.id.btn_donation_ad:
                            strSKU = SKU_AD_REMOVE;
                            break;
                    }

                    for(SkuDetails skuDetails : skuDetailsList){
                        if(skuDetails.getSku().equals(strSKU)){
                            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build();
                            billingClient.launchBillingFlow(DonationActivity.this, flowParams);

                            break;
                        }
                    }
                }
            }
        };

        btnAdRemove.setOnClickListener(onClickListener);
        btnDonate1.setOnClickListener(onClickListener);
        btnDonate2.setOnClickListener(onClickListener);
        btnDonate3.setOnClickListener(onClickListener);
        btnDonate4.setOnClickListener(onClickListener);
        btnDonateSelf.setOnClickListener(onClickListener);

        if(!Locale.getDefault().toString().equals("ko_KR")){
            btnDonateSelf.setVisibility(GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        billingClient.queryPurchases(BillingClient.SkuType.INAPP);
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null){
            //Successfully Purchased
            for(Purchase purchase : purchases){
                if(purchase.getSku().equals(SKU_AD_REMOVE)){
                    Toast.makeText(getApplicationContext(), getString(R.string.donation_toast_purchase_adremove), Toast.LENGTH_SHORT).show();

                    //Save SharedPreferences about Removing Advertisement
                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE);
                    SharedPreferences.Editor ed = prefs.edit();
                    ed.remove("ad_removed");
                    ed.putBoolean("ad_removed", true);
                    ed.apply();

                    AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                        @Override
                        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.donation_toast_purchase_thanks), Toast.LENGTH_LONG).show();

                    ConsumeParams consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

                    billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {

                        }
                    });
                }
            }
        } else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED){
            //User Canceled Puchase
            Toast.makeText(getApplicationContext(), getString(R.string.donation_toast_purchase_canceled), Toast.LENGTH_LONG).show();
        }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
            //Already Purchased Item
            Toast.makeText(getApplicationContext(), getString(R.string.donation_toast_purchase_alreadyowned), Toast.LENGTH_LONG).show();

            for(Purchase purchase : purchases){
                if(purchase.getSku().equals(SKU_AD_REMOVE)){
                    Toast.makeText(getApplicationContext(), "AAA", Toast.LENGTH_LONG).show();

                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE);
                    SharedPreferences.Editor ed = prefs.edit();
                    ed.remove("ad_removed");
                    ed.putBoolean("ad_removed", true);
                    ed.apply();
                }
            }
        }else{
            //Unknown Code
            Toast.makeText(getApplicationContext(),  String.format(Locale.getDefault(), getString(R.string.donation_toast_purchase_unknown_error), billingResult.getResponseCode()), Toast.LENGTH_LONG).show();
        }
    }

    public void queryPurchase(){
        List<String> skuList = new ArrayList<>();
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
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> skuDetailsList) {
                        DonationActivity.this.skuDetailsList = skuDetailsList;
                        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null){
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
