package com.ames.books;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.ames.books.accessor.AsyncSearcher;
import com.ames.books.data.SearchBlock;
import com.ames.books.data.SearchResultListener;
import com.ames.books.presenter.BookListAdapter;
import com.ames.books.presenter.ShowDetailsListener;
import com.google.api.services.books.model.Volume;

/**
 * Fragment that shows the list of books.
 */
public class BookListFragment extends Fragment implements SearchResultListener, ShowDetailsListener {
  private static final String TAG = "books.Booklist";

  protected RecyclerView books;
  protected BookListAdapter adapter;
  protected Button search;
  protected EditText input;
  protected View searchProgress;

  protected AsyncSearcher searcher = new AsyncSearcher(this);

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_book_list, container, false);

    books = (RecyclerView) view.findViewById(R.id.books);
    adapter = new BookListAdapter(this);

    books.setAdapter(adapter);

    input = (EditText) view.findViewById(R.id.search);
    search = (Button) view.findViewById(R.id.search_button);
    searchProgress = view.findViewById(R.id.search_progress);

    search.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Hide the keyboard now
        CharSequence query = input.getText();
        if (query != null && query.length() > 0) {
          Log.d(TAG, "Query [" + query + "]");
          search.setEnabled(false);
          searchProgress.setVisibility(View.VISIBLE);
          searcher.doSearch(query.toString(), 0);
          hideKeyboard();
        }
      }
    });

    if (savedInstanceState != null) {
      adapter.setState(savedInstanceState.getSerializable("d.list.data"));
      hideKeyboardDelayed();
    }

    return view;
  }

  private void hideKeyboardDelayed() {
    // We do not want the keyboard immediately up if the state has been restored.
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        hideKeyboard();
      }
    }, 1000);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable("d.list.data", adapter.getState());
  }

  @Override
  public void onQueryResult(SearchBlock books, String query) {
    search.setEnabled(true);
    searchProgress.setVisibility(View.GONE);
    adapter.setBookList(books, query);
  }

  /**
   * Hide the software keyboard from the device screen. The keyboard appears again when tapped on the search box.
   */
  protected void hideKeyboard() {
    Activity activity = getActivity();
    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
    View view = activity.getCurrentFocus();
    if (view == null) {
      view = new View(activity);
    }
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  /**
   * We pass to activity first as the activity controls fragment visibility
   */
  @Override
  public void showDetails(Volume book, Drawable thumb) {
    hideKeyboard();
    searchProgress.setVisibility(View.GONE);
    Activity activity = getActivity();
    if (activity instanceof ShowDetailsListener) {
      ShowDetailsListener listener = (ShowDetailsListener) activity;
      listener.showDetails(book, thumb);
    }
  }
}
