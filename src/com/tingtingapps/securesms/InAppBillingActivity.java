package com.tingtingapps.securesms;

import com.tingtingapps.securesms.utilbilling.IabHelper;
import com.tingtingapps.securesms.utilbilling.IabResult;
import com.tingtingapps.securesms.utilbilling.Inventory;
import com.tingtingapps.securesms.utilbilling.Purchase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class InAppBillingActivity extends AppCompatActivity {
    private static final String TAG = "InAppBilling";
    IabHelper mHelper;
    static final String ITEM_SKU = "com.tingtingapps.securesms.noads"; // android.test.purchased
    private Button clickButton;
    private Button buyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_billing);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        buyButton = (Button) findViewById(R.id.buyButton);
        clickButton = (Button) findViewById(R.id.clickButton);
        clickButton.setEnabled(false);

        Button btn_1usd = (Button) findViewById(R.id.btn_1usd);
        btn_1usd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buyClick(v);
            }
        });
        Button btn_2usd = (Button) findViewById(R.id.btn_2usd);
        btn_2usd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buyClick(v);
            }
        });
        Button btn_5usd = (Button) findViewById(R.id.btn_5usd);
        btn_1usd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buyClick(v);
            }
        });
        Button btn_10usd = (Button) findViewById(R.id.btn_10usd);
        btn_10usd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buyClick(v);
            }
        });

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh+ySpq1lypL17etiXcTsEqBHvVE6WESGGgCol6RB7EA7CM4xi4Us09+CJAM25hgJtW9QAH/5lv86z+70tNwy2rQynWvlnxlU8Ll3gj+poKmyC+rybxLbYsWeHY24kRxcIYCGHdfW3UiuIhqwjZgVFptMqPNMugyXybq41n+e3Om2HMFAzXFhm2qMpU7r1kWPOUifu/cAOO44HW1gcNlu30SSFoD7arrefH58r8QdqyBzAwBvJR8mXX88nhvsWtcAk3JSzRZUl62ly0b6l12C84rW5lpZn3HB0RyScK5q0NZZ2XADlkJFVPWD78nYPwl3BqlgptJx9pSaGxbn93AGdwIDAQAB";
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new
                                   IabHelper.OnIabSetupFinishedListener() {
                                       public void onIabSetupFinished(IabResult result) {
                                           if (!result.isSuccess()) {
                                               Log.d(TAG, "In-app Billing setup failed: " + result);
                                           } else {
                                               Log.d(TAG, "In-app Billing is set up OK");
                                           }
                                       }
                                   });
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
                buyButton.setEnabled(false);
            }

        }
    };

    public void buttonClicked(View view) {
        clickButton.setEnabled(false);
        buyButton.setEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                // Handle failure
            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                        mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        clickButton.setEnabled(true);
                    } else {
                        // handle error
                    }
                }
            };


    public void buyClick(View view) {
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,
                mPurchaseFinishedListener, "mypurchasetoken");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_in_app_billing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
