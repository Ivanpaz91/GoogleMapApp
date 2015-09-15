package com.superiorinfotech.publicbuddy.events;

/**
 * Created by admin on 5/20/2015.
 */
public class SearchInputEvent {
    public String searchText;
    public SearchInputEvent(String newText) {
        this.searchText = newText;
    }
}
