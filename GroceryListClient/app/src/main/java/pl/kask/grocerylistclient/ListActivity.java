package pl.kask.grocerylistclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.kask.grocerylistclient.dto.GroceryItemDto;

public class ListActivity extends AppCompatActivity {

    List<GroceryItemDto> groceryList;

    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ListView groceryItemsListView = (ListView) findViewById(R.id.groceryItemsListView);
        groceryList = new ArrayList<>(Arrays.asList(
                new GroceryItemDto("k.lyskawinski@gmail.com", "Apples", 2),
                new GroceryItemDto("k.lyskawinski@gmail.com", "Milk", 1),
                new GroceryItemDto("k.lyskawinski@gmail.com", "Bread", 1)
        ));
        final ArrayAdapter<GroceryItemDto> groceryListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groceryList);
        groceryList.add(new GroceryItemDto("k.lyskawinski@gmail.com", "Oranges", 3));
        groceryItemsListView.setAdapter(groceryListAdapter);
        groceryItemsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

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
                                // TODO post update
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
                                // TODO post update
                            }
                        });
                        builder.show();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.menu_remove:
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
                mActionMode = null;
            }
        };

        groceryItemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode == null) {
                    mActionMode = ListActivity.this.startActionMode(mActionModeCallback);
                    groceryItemsListView.setItemChecked(position, true);
                }
            }
        });

//        groceryItemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String name;
//                GroceryItemDto groceryItem = (GroceryItemDto) groceryItemsListView.getItemAtPosition(position);
//                name = groceryItem.getItemName();
//                Toast.makeText(ListActivity.this, name, Toast.LENGTH_SHORT).show();
//            }
//        });



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
                        groceryList.add(new GroceryItemDto("k.lyskawinski@gmail.com", name, 0));
                        groceryListAdapter.notifyDataSetChanged();
                        // TODO post update
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
    }

}
