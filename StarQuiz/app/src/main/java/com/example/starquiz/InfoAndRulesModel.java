package com.example.starquiz;


import java.util.List;

public class InfoAndRulesModel {
    List<String> infoPages;
    int currentPageIndex;

    //Allows for pages to be stored in a list.
    public InfoAndRulesModel(List<String> infoPages) {
        this.infoPages = infoPages;
        currentPageIndex = 0;
    }

    //Helps keep track of which page to display
    public String getCurrentPage() {
        return infoPages.get(currentPageIndex);
    }

    public void goToNextPage() {
        if (currentPageIndex < infoPages.size() - 1) {
            currentPageIndex++;
        }
    }

    public void goToPreviousPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--;
        }
    }
}


