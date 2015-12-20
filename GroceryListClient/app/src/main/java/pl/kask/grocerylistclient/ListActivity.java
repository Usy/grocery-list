package pl.kask.grocerylistclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.kask.grocerylistclient.dto.GroceryItemDto;
import pl.kask.grocerylistclient.dto.ShopNameDto;
import pl.kask.grocerylistclient.dto.SynchronizationRequest;
import pl.kask.grocerylistclient.dto.SynchronizationResponse;
import retrofit.RestAdapter;

public class ListActivity extends AppCompatActivity {

    private static final String TAG = ListActivity.class.getName();

    public static final String LOCAL_PREFIX = ".grocery.local.";
    public static final String TOTAL_PREFIX = ".grocery.total.";
    public static final String SHOP_NAME_PREFIX = ".grocery.shop.";
    public static final String TIMESTAMP_PREFIX = ".grocery.timestamp.";
    public static final String TO_ADD_PREFIX = ".grocery.itemsToAdd";
    public static final String TO_REMOVE_PREFIX = ".grocery.itemsToRemove";

    private List<GroceryItemDto> groceryList;
    private ArrayAdapter<GroceryItemDto> groceryListAdapter;
    private GroceryApi groceryApi;
    private ActionMode actionMode;
    private String accountId;
    private String idToken;
    private String deviceId;
    private List<String> itemsToAdd = new ArrayList<>();
    private List<String> itemsToRemove = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_synchronize:
                Toast.makeText(ListActivity.this, "Synchronization started.", Toast.LENGTH_LONG).show();
                synchronize();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        accountId = intent.getStringExtra(MainActivity.LOGGED_USER_ACC_ID_TAG);
        idToken = intent.getStringExtra(MainActivity.LOGGED_USER_ID_TOKEN_TAG);
        SharedPreferences settings = getSharedPreferences("AppSettings", Activity.MODE_PRIVATE);
        if (settings.contains("settings.deviceId")) {
            deviceId = settings.getString("settings.deviceId", "");
        } else {
            SharedPreferences.Editor editor = settings.edit();
            deviceId = UUID.randomUUID().toString();
            editor.putString("settings.deviceId", deviceId);
            editor.commit();
        }

        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ListView groceryItemsListView = (ListView) findViewById(R.id.groceryItemsListView);
        groceryList = new ArrayList<>();
        groceryListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, groceryList);
        groceryItemsListView.setAdapter(groceryListAdapter);
        groceryItemsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // Return false if nothing is done
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                final int checkedItemPosition = groceryItemsListView.getCheckedItemPosition();
                String name = groceryListAdapter.getItem(checkedItemPosition).getItemName();
                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                final EditText input = new EditText(ListActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                switch (item.getItemId()) {
                    case R.id.menu_inc:
                        builder.setTitle(name + ": increase amount");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GroceryItemDto groceryItemDto = groceryList.get(checkedItemPosition);
                                int difference = Integer.parseInt(input.getText().toString());
                                int amount = groceryItemDto.getAmount();
                                amount += difference;
                                groceryItemDto.setAmount(amount);
                                int localAmount = groceryItemDto.getLocalAmount();
                                groceryItemDto.setLocalAmount(localAmount + difference);
                                persistGroceryItem(groceryItemDto);
                                groceryListAdapter.notifyDataSetChanged();
                            }
                        });
                        builder.show();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.menu_dec:
                        builder.setTitle(name + ": decrease amount");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GroceryItemDto groceryItemDto = groceryList.get(checkedItemPosition);
                                int difference = Integer.parseInt(input.getText().toString());
                                int amount = groceryItemDto.getAmount();
                                amount -= difference;
                                if (amount < 0) {
                                    Toast.makeText(ListActivity.this, "The amount is too high.", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                groceryItemDto.setAmount(amount);
                                int localAmount = groceryItemDto.getLocalAmount();
                                groceryItemDto.setLocalAmount(localAmount - difference);
                                persistGroceryItem(groceryItemDto);
                                groceryListAdapter.notifyDataSetChanged();
                            }
                        });
                        builder.show();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.menu_remove:
                        final GroceryItemDto groceryItemDto = groceryList.get(checkedItemPosition);
                        String itemName = groceryItemDto.getItemName();
                        itemsToRemove.add(itemName);
                        if (itemsToAdd.contains(itemName)) {
                            itemsToAdd.remove(itemName);
                            persistItemsToAdd();
                        }
                        persistItemsToRemove();
                        removeGroceryItem(groceryItemDto.getItemName());
                        groceryList.remove(checkedItemPosition);
                        groceryListAdapter.notifyDataSetChanged();
                        mode.finish();
                        return true;
                    case R.id.menu_edit:
                        AlertDialog.Builder editBuilder = new AlertDialog.Builder(ListActivity.this);
                        final EditText shopNameInput = new EditText(ListActivity.this);
                        editBuilder.setView(shopNameInput);
                        editBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        editBuilder.setTitle(name + ": edit shop name");
                        editBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GroceryItemDto groceryItemDto = groceryList.get(checkedItemPosition);
                                String shopName = shopNameInput.getText().toString();
                                long timestamp = System.currentTimeMillis();
                                groceryItemDto.setShopName(new ShopNameDto(shopName, timestamp));
                                persistGroceryItem(groceryItemDto);
                                groceryListAdapter.notifyDataSetChanged();
                            }
                        });
                        editBuilder.show();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                int position = groceryItemsListView.getCheckedItemPosition();
                groceryItemsListView.setItemChecked(position, false);
                actionMode = null;
            }
        };

        groceryItemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (actionMode == null) {
                    actionMode = ListActivity.this.startActionMode(actionModeCallback);
                    groceryItemsListView.setItemChecked(position, true);
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                builder.setTitle("New product");

                final EditText input = new EditText(ListActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();
                        for (GroceryItemDto groceryItem : groceryList) {
                            if (groceryItem.getItemName().equals(name)) {
                                Toast.makeText(ListActivity.this, name + " already exists.", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        itemsToAdd.add(name);
                        if (itemsToRemove.contains(name)) {
                            itemsToRemove.remove(name);
                            persistItemsToRemove();
                        }
                        persistItemsToAdd();
                        final GroceryItemDto groceryItemDto = new GroceryItemDto(accountId, name, 0, 0, new ShopNameDto());
                        persistGroceryItem(groceryItemDto);
                        groceryList.add(groceryItemDto);
                        Collections.sort(groceryList);
                        groceryListAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String ip = settings.getString("settings.ip", "mono-organizer.cloudapp.net:8080");
        String endpoint = "http://" + ip + "/GroceryList/rest/grocery";
        Log.d(TAG, endpoint);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .build();

        groceryApi = restAdapter.create(GroceryApi.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Loading items...");
        loadGroceryItems();
        loadItemsToAdd();
        loadItemsToRemove();
        groceryListAdapter.notifyDataSetChanged();
    }

    private void synchronize() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                SynchronizationRequest request = new SynchronizationRequest();
                for (GroceryItemDto groceryItem : groceryList) {
                    request.getSubSums().put(groceryItem.getItemName(), groceryItem.getLocalAmount());
                    request.getShopNames().put(groceryItem.getItemName(), groceryItem.getShopName());
                }
                request.setProductsToAdd(itemsToAdd);
                request.setProductsToRemove(itemsToRemove);

                SynchronizationResponse response;
                try {
                    Log.d(TAG, "Sending request: " + request);
                    response = groceryApi.synchronize(accountId, request, idToken, deviceId);
                    Log.d(TAG, "Got response: " + response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ListActivity.this, "Synchronization finished.", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Throwable t) {
                    Log.w(TAG, t.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ListActivity.this, "Synchronization failed.", Toast.LENGTH_LONG).show();
                        }
                    });
                    return null;
                }

                itemsToAdd.clear();
                persistItemsToAdd();
                itemsToRemove.clear();
                persistItemsToRemove();

                outer:
                for (String productName : response.getProductsToAdd()) {
                    for (GroceryItemDto groceryItem : groceryList) {
                        if (groceryItem.getItemName().equals(productName)) {
                            continue outer;
                        }
                    }
                    GroceryItemDto newItem = new GroceryItemDto(accountId, productName, 0, 0, new ShopNameDto());
                    persistGroceryItem(newItem);
                    groceryList.add(newItem);
                }
                for (String productName : response.getProductsToRemove()) {
                    List<GroceryItemDto> itemsToRemove = new ArrayList<>();
                    for (GroceryItemDto groceryItem : groceryList) {
                        if (groceryItem.getItemName().equals(productName)) {
                            itemsToRemove.add(groceryItem);
                            removeGroceryItem(productName);
                        }
                    }
                    groceryList.removeAll(itemsToRemove);
                }
                for (GroceryItemDto groceryItem : groceryList) {
                    Integer totalAmount = response.getTotalAmounts().get(groceryItem.getItemName());
                    ShopNameDto shopName = response.getShopNames().get(groceryItem.getItemName());
                    if (totalAmount != null && shopName != null) {
                        groceryItem.setAmount(totalAmount);
                        groceryItem.setShopName(shopName);
                        persistGroceryItem(groceryItem);
                    }
                }
                Collections.sort(groceryList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        groceryListAdapter.notifyDataSetChanged();
                    }
                });
                return null;
            }
        }.execute();
    }

    private void persistItemsToAdd() {
        persistList(accountId + TO_ADD_PREFIX, itemsToAdd);
    }

    private void loadItemsToAdd() {
        SharedPreferences settings = getSharedPreferences("AppSettings", Activity.MODE_PRIVATE);
        Set<String> items = settings.getStringSet(accountId + TO_ADD_PREFIX, new HashSet<String>());
        itemsToAdd = new ArrayList<>(items);
    }

    private void persistItemsToRemove() {
        persistList(accountId + TO_REMOVE_PREFIX, itemsToRemove);
    }

    private void loadItemsToRemove() {
        SharedPreferences settings = getSharedPreferences("AppSettings", Activity.MODE_PRIVATE);
        Set<String> items = settings.getStringSet(accountId + TO_REMOVE_PREFIX, new HashSet<String>());
        itemsToRemove = new ArrayList<>(items);
    }

    private void persistList(String key, List<String> values) {
        SharedPreferences settings = getSharedPreferences("AppSettings", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(key, new HashSet<>(values));
        editor.commit();
    }

    private void persistGroceryItem(GroceryItemDto groceryItem) {
        SharedPreferences settings = getSharedPreferences("AppSettings", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(accountId + TOTAL_PREFIX + groceryItem.getItemName(), groceryItem.getAmount());
        editor.putInt(accountId + LOCAL_PREFIX + groceryItem.getItemName(), groceryItem.getLocalAmount());
        editor.putString(accountId + SHOP_NAME_PREFIX + groceryItem.getItemName(), groceryItem.getShopName().getName());
        editor.putLong(accountId + TIMESTAMP_PREFIX + groceryItem.getItemName(), groceryItem.getShopName().getTimestamp());
        editor.commit();
    }

    private void removeGroceryItem(String itemName) {
        SharedPreferences settings = getSharedPreferences("AppSettings", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(accountId + TOTAL_PREFIX + itemName);
        editor.remove(accountId + LOCAL_PREFIX + itemName);
        editor.remove(accountId + SHOP_NAME_PREFIX + itemName);
        editor.commit();
    }

    private void loadGroceryItems() {
        SharedPreferences settings = getSharedPreferences("AppSettings", Activity.MODE_PRIVATE);
        Set<String> keys = settings.getAll().keySet();
        Pattern pattern = Pattern.compile(accountId + "\\.grocery\\.total\\.(.*)");
        groceryList.clear();
        for (String key : keys) {
            Matcher matcher = pattern.matcher(key);
            Log.d(TAG, "Key: " + key);
            if (matcher.find()) {
                String itemName = matcher.group(1);
                Log.d(TAG, "Found: " + itemName);
                int totalAmount = settings.getInt(accountId + TOTAL_PREFIX + itemName, 0);
                int localAmount = settings.getInt(accountId + LOCAL_PREFIX + itemName, 0);
                String shopName = settings.getString(accountId + SHOP_NAME_PREFIX + itemName, "");
                long timestamp = settings.getLong(accountId + TIMESTAMP_PREFIX + itemName, 0);

                groceryList.add(new GroceryItemDto(accountId, itemName, totalAmount, localAmount, new ShopNameDto(shopName, timestamp)));
            }
        }
        Collections.sort(groceryList);
    }
}
