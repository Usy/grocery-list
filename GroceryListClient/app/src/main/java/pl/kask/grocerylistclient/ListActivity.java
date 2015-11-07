package pl.kask.grocerylistclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
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
        ArrayAdapter<GroceryItemDto> groceryListAdapter = new ArrayAdapter<GroceryItemDto>(this, android.R.layout.simple_list_item_1, groceryList);
        groceryList.add(new GroceryItemDto("k.lyskawinski@gmail.com", "Oranges", 3));
        groceryItemsListView.setAdapter(groceryListAdapter);

        groceryItemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name;
                GroceryItemDto groceryItem = (GroceryItemDto) groceryItemsListView.getItemAtPosition(position);
                name = groceryItem.getItemName();
                Toast.makeText(ListActivity.this, name, Toast.LENGTH_SHORT).show();
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
                        groceryList.add(new GroceryItemDto("k.lyskawinski@gmail.com", name, 0));
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
