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
import java.util.List;

import pl.kask.grocerylistclient.dto.GroceryItemDto;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ListActivity extends AppCompatActivity {

    private static final String TAG = ListActivity.class.getName();

    private List<GroceryItemDto> groceryList;
    private GroceryApi groceryApi;
    private ActionMode actionMode;
    private String accountId;
    private String idToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        accountId = intent.getStringExtra(MainActivity.LOGGED_USER_ACC_ID_TAG);
        idToken = intent.getStringExtra(MainActivity.LOGGED_USER_ID_TOKEN_TAG);

        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ListView groceryItemsListView = (ListView) findViewById(R.id.groceryItemsListView);
        groceryList = new ArrayList<>();
        final ArrayAdapter<GroceryItemDto> groceryListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groceryList);
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
                                groceryListAdapter.notifyDataSetChanged();
                                updateItem(groceryItemDto);
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
                                groceryListAdapter.notifyDataSetChanged();
                                updateItem(groceryItemDto);
                            }
                        });
                        builder.show();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.menu_remove:
                        final GroceryItemDto groceryItemDto = groceryList.get(checkedItemPosition);
                        groceryList.remove(checkedItemPosition);
                        groceryListAdapter.notifyDataSetChanged();
                        removeItem(groceryItemDto.getItemName());
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
                        final GroceryItemDto groceryItemDto = new GroceryItemDto(accountId, name, 0);
                        groceryList.add(groceryItemDto);
                        groceryListAdapter.notifyDataSetChanged();
                        createItem(groceryItemDto);
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

        SharedPreferences settings = getSharedPreferences("AppSettings", Activity.MODE_PRIVATE);
        String ip = settings.getString("ip", "192.168.1.101:8080");
        String endpoint = "http://" + ip + "/GroceryList/rest/grocery";
        Log.d(TAG, endpoint);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .build();

        groceryApi = restAdapter.create(GroceryApi.class);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                List<GroceryItemDto> result = groceryApi.fetchItems(accountId, idToken);
                groceryList.clear();
                groceryList.addAll(result);
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

    private void updateItem(final GroceryItemDto groceryItemDto) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                groceryApi.updateItem(groceryItemDto, idToken, new Callback<Response>() {
                    @Override
                    public void success(Response r, Response response) {
                        Log.i(TAG, "Updating element finished successfully " + response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "There was a problem during update: " + error);
                    }
                });
                return null;
            }
        }.execute();
    }

    private void createItem(final GroceryItemDto groceryItemDto) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                groceryApi.addItem(groceryItemDto, idToken, new Callback<Response>() {
                    @Override
                    public void success(Response r, Response response) {
                        Log.i(TAG, "Adding element finished successfully " + response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "There was a problem during creation: " + error);
                    }
                });
                return null;
            }
        }.execute();
    }

    private void removeItem(final String itemName) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                groceryApi.deleteItem(accountId, itemName, idToken, new Callback<Response>() {
                    @Override
                    public void success(Response r, Response response) {
                        Log.i(TAG, "Item deleted successfully");
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "There was a problem during deletion " + error);
                    }
                });
                return null;
            }
        }.execute();
    }
}
