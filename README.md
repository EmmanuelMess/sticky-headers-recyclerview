sticky-headers-recyclerview
===========================

This decorator allows you to easily create section headers for RecyclerViews using a
LinearLayoutManager in either vertical or horizontal orientation.

Credit to [Emil Sjölander](https://github.com/emilsjolander) for creating StickyListHeaders,
a library that many of us relied on for sticky headers in our listviews.

<a><p>
  <img src="http://i.imgur.com/I0ztoPw.gif" width="350"/>
  <img src="http://i.imgur.com/b5pJjtL.gif" height="350"/>
</p></a>


Download
--------

    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }

    dependencies {
  		compile 'com.github.emmanuelmess:sticky-headers-recyclerview:library:[latest.version.number]'
  	}

Updating from <0.5
------------------
```java
public interface StickyRecyclerHeadersAdapter<VH extends RecyclerView.ViewHolder> {
...
  int getNumColumns();//<--returns 1

  int getSpanSize(int position);//<--returns 1
}
```

Usage
-----

There are three main classes, `StickyRecyclerHeadersAdapter`, `StickyRecyclerHeadersDecoration`,
and `StickyRecyclerHeadersTouchListener`.

`StickyRecyclerHeadersAdapter` has a very similar interface to the `RecyclerView.Adapter`, and it
is recommended that you make your `RecyclerView.Adapter` implement `StickyRecyclerHeadersAdapter`.

There interface looks like this:

```java
public interface StickyRecyclerHeadersAdapter<VH extends RecyclerView.ViewHolder> {
  long getHeaderId(int position);

  VH onCreateHeaderViewHolder(ViewGroup parent);

  void onBindHeaderViewHolder(VH holder, int position);

  int getItemCount();

  int getNumColumns();//1 if you only want one column

  int getSpanSize(int position);//1 if you only want one column
}
```

The second class, `StickyRecyclerHeadersDecoration`, is where most of the magic happens, and does
not require any configuration on your end.  Here's an example from `onCreate()` in an activity:

```java
mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
mAdapter = new MyStickyRecyclerHeadersAdapter();
mRecyclerView.setAdapter(mAdapter);
mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
mRecyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(mAdapter));
```

`StickyRecyclerHeadersTouchListener` allows you to listen for clicks on header views.
Simply create an instance of `StickyRecyclerHeadersTouchListener`, set the `OnHeaderClickListener`,
and add the `StickyRecyclerHeadersTouchListener` as a touch listener to your `RecyclerView`.

```java
StickyRecyclerHeadersTouchListener touchListener =
    new StickyRecyclerHeadersTouchListener(recyclerView, headersDecor);
touchListener.setOnHeaderClickListener(
    new StickyRecyclerHeadersTouchListener.OnHeaderClickListener() {
      @Override
      public void onHeaderClick(View header, int position, long headerId) {
        Toast.makeText(MainActivity.this, "Header position: " + position + ", id: " + headerId,
            Toast.LENGTH_SHORT).show();
      }
    });
mRecyclerView.addOnItemTouchListener(touchListener);
```

The StickyHeaders aren't aware of your adapter so if you must notify them when your data set changes.

```java
    mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
      @Override public void onChanged() {
        headersDecor.invalidateHeaders();
      }
    });
```

If the Recyclerview's layout manager implements getExtraLayoutSpace (to preload more content then is
visible for performance reasons), you must implement ItemVisibilityAdapter and pass an instance as a
second argument to StickyRecyclerHeadersDecoration's constructor.
```java
    @Override
    public boolean isPositionVisible(final int position) {
        return layoutManager.findFirstVisibleItemPosition() <= position
            && layoutManager.findLastVisibleItemPosition() >= position;
    }
```


Item animators don't play nicely with RecyclerView decorations, so your mileage with that may vary.

Compatibility
-------------

API 11+

Known Issues
------------

* The header views aren't recycled at this time.  Contributions are most welcome.

* I haven't tested this with ItemAnimators yet.

* The header views are drawn to a canvas, and are not actually a part of the view hierarchy. As such, they can't have touch states, and you may run into issues if you try to load images into them asynchronously.
