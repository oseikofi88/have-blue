package works.wima.Routes;

import java.util.ArrayList;

public class SearchResults {
    ArrayList<TwitterUsers> twitterUsers;

    public SearchResults(ArrayList<TwitterUsers> twitterUsers) {
        this.twitterUsers = twitterUsers;
    }

    public ArrayList<TwitterUsers> getTwitterUsers() {
        return twitterUsers;
    }

    public void setTwitterUsers(ArrayList<TwitterUsers> twitterUsers) {
        this.twitterUsers = twitterUsers;
    }

    public static class TwitterUsers {
       String  username;

        public TwitterUsers(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
