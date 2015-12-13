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
import pl.kask.grocerylistclient.dto.SynchronizationRequest;
import pl.kask.grocerylistclient.dto.SynchronizationResponse;
import retrofit.RestAdapter;

public class ListActivity extends AppCompatActivity {

    private static final String TAG = ListActivity.class.getName();

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
        groceryListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groceryList);
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
                        itemsToRemove.add(groceryItemDto.getItemName());
                        removeGroceryItem(groceryItemDto.getItemName());
                        groceryList.remove(checkedItemPosition);
                        groceryListAdapter.notifyDataSetChanged();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
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
                        itemsToAdd.add(name);
                        final GroceryItemDto groceryItemDto = new GroceryItemDto(accountId, name, 0, 0);
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

        String ip = settings.getString("settings.ip", "192.168.1.101:8080");
        String endpoint = "http://" + ip + "/GroceryList/rest/grocery";
        Log.d(TAG, endpoint);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .build();

        groceryApi = restAdapter.create(GroceryApi.class);

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
                }
                request.setProductsToAdd(itemsToAdd);
                request.setProductsToRemove(itemsToRemove);

                SynchronizationResponse response;
                try {
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

                for (String productName : response.getProductsToAdd()) {
                    GroceryItemDto newItem = new GroceryItemDto(accountId, productName, 0, 0);
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
                    if (totalAmount != null) {
                        groceryItem.setAmount(totalAmount);
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
        persistList("grocery.itemsToAdd", itemsToAdd);
    }

    private void loadItemsToAdd() {
        SharedPreferences settings = getSharedPreferences("AppSettings", Activity.MODE_PRIVATE);
        Set<String> items = settings.getStringSet("grocery.itemsToAdd", new HashSet<String>());
        itemsToAdd = new ArrayList<>(items);
    }

    private void persistItemsToRemove() {
        persistList("grocery.itemsToRemove", itemsToRemove);
    }

    private void loadItemsToRemove() {
        SharedPreferences settings = getSharedPreferences("AppSettings", Activity.MODE_PRIVATE);
        Set<String> items = settings.getStringSet("grocery.itemsToRemove", new HashSet<String>());
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
        editor.putInt("grocery.total." + groceryItem.getItemName(), groceryItem.getAmount());
        editor.putInt("grocery.local." + groceryItem.getItemName(), groceryItem.getLocalAmount());
        editor.commit();
    }

    private void removeGroceryItem(String itemName) {
        SharedPreferences settings = getSharedPreferences("AppSettings", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("grocery.total." + itemName);
        editor.remove("grocery.local." + itemName);
        editor.commit();
    }

    private void loadGroceryItems() {
        SharedPreferences settings = getSharedPreferences("AppSettings", Activity.MODE_PRIVATE);
        Set<String> keys = settings.getAll().keySet();
        Pattern pattern = Pattern.compile("grocery\\.total\\.(.*)");
        groceryList.clear();
        for (String key : keys) {
            Matcher matcher = pattern.matcher(key);
            if (matcher.find()) {
                String itemName = matcher.group(1);
                Log.d(TAG, "Found: " + itemName);
                int totalAmount = settings.getInt("grocery.total." + itemName, 0);
                int localAmount = settings.getInt("grocery.local." + itemName, 0);

                groceryList.add(new GroceryItemDto(accountId, itemName, totalAmount, localAmount));
            }
        }
        Collections.sort(groceryList);
    }
}
