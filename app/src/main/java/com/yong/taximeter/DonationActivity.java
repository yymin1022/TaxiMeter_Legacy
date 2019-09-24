package com.yong.taximeter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private Button btnAdRemove;
    private Button btnDonate1;
    private Button btnDonate2;
    private Button btnDonate3;
    private Button btnDonate4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);

        btnAdRemove = findViewById(R.id.ad_remove);
        btnDonate1 = findViewById(R.id.donate1);
        btnDonate2 = findViewById(R.id.donate2);
        btnDonate3 = findViewById(R.id.donate3);
        btnDonate4 = findViewById(R.id.donate4);

        btnAdRemove.setEnabled(false);
        btnAdRemove.setText("잠시만 기다려주세요,");
        btnDonate1.setEnabled(false);
        btnDonate1.setText("잠시만 기다려주세요,");
        btnDonate2.setEnabled(false);
        btnDonate2.setText("잠시만 기다려주세요,");
        btnDonate3.setEnabled(false);
        btnDonate3.setText("잠시만 기다려주세요,");
        btnDonate4.setEnabled(false);
        btnDonate4.setText("잠시만 기다려주세요,");

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
                    Toast.makeText(getApplicationContext(), "Thanks for purchasing! Your AD will be hidden!", Toast.LENGTH_LONG).show();

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
        btnAdRemove.setEnabled(true);
        btnAdRemove.setText("구매하려면 누르세요.");
        btnDonate1.setEnabled(true);
        btnDonate1.setText("구매하려면 누르세요.");
        btnDonate2.setEnabled(true);
        btnDonate2.setText("구매하려면 누르세요.");
        btnDonate3.setEnabled(true);
        btnDonate3.setText("구매하려면 누르세요.");
        btnDonate4.setEnabled(true);
        btnDonate4.setText("구매하려면 누르세요.");
    }

    public void ad_remove(View v){
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(SKU_AD_REMOVE)
                .setType(BillingClient.SkuType.INAPP)
                .build();
        billingClient.launchBillingFlow(this, flowParams);
    }

    public void donate1(View v){
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(SKU_DONATE_1)
                .setType(BillingClient.SkuType.INAPP)
                .build();
        billingClient.launchBillingFlow(this, flowParams);
    }

    public void donate2(View v){
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(SKU_DONATE_2)
                .setType(BillingClient.SkuType.INAPP)
                .build();
        billingClient.launchBillingFlow(this, flowParams);
    }

    public void donate3(View v){
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(SKU_DONATE_3)
                .setType(BillingClient.SkuType.INAPP)
                .build();
        billingClient.launchBillingFlow(this, flowParams);
    }

    public void donate4(View v){
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(SKU_DONATE_4)
                .setType(BillingClient.SkuType.INAPP)
                .build();
        billingClient.launchBillingFlow(this, flowParams);
    }
}
